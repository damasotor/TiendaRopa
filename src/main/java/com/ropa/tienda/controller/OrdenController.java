package com.ropa.tienda.controller;

import com.ropa.tienda.model.Carrito;
import com.ropa.tienda.model.Orden;
import com.ropa.tienda.model.Producto;
import com.ropa.tienda.model.Usuario;
import com.ropa.tienda.repository.CarritoRepository;
import com.ropa.tienda.repository.OrdenRepository;
import com.ropa.tienda.repository.ProductoRepository;
import com.ropa.tienda.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// DTOs para las peticiones
record CrearOrdenRequest(
    String visitanteId,
    String metodoPago,
    String sucursalId,  // null si es envío a domicilio
    Orden.DireccionEnvio direccionEnvio  // null si es retiro en sucursal
) {}

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UserRepository userRepository;

    // Método helper para obtener email del usuario desde el token
    private String obtenerEmailDeToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return AuthController.activeTokens.get(token);
        }
        return null;
    }

    // Crear orden desde carrito
    @PostMapping
    public ResponseEntity<?> crearOrden(@RequestBody CrearOrdenRequest request,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== CREAR ORDEN ===");
            System.out.println("Request recibido: " + request);
            System.out.println("Auth header recibido: " + authHeader);
            
            String userEmail = obtenerEmailDeToken(authHeader);
            System.out.println("Email extraído del token: " + userEmail);
            Carrito carrito = null;
            
            if (userEmail != null) {
                // Usuario autenticado: buscar carrito por email
                System.out.println("Buscando carrito por email: " + userEmail);
                Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioEmail(userEmail);
                if (carritoOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Carrito no encontrado para el usuario");
                }
                carrito = carritoOpt.get();
            } else {
                // Visitante: buscar carrito por visitanteId
                System.out.println("Buscando carrito por visitanteId: " + request.visitanteId());
                Optional<Carrito> carritoOpt = carritoRepository.findByVisitanteId(request.visitanteId());
                if (carritoOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Carrito no encontrado");
                }
                carrito = carritoOpt.get();
            }

            if (carrito.getItems().isEmpty()) {
                System.out.println("Carrito vacío");
                return ResponseEntity.badRequest().body("El carrito está vacío");
            }

            System.out.println("Carrito encontrado con " + carrito.getItems().size() + " items");
            System.out.println("Total del carrito: " + carrito.getTotal());

            // Crear orden a partir del carrito
            Orden nuevaOrden = new Orden();
            
            if (userEmail != null) {
                // Usuario autenticado
                nuevaOrden.setUsuarioEmail(userEmail);
            } else {
                // Visitante
                nuevaOrden.setVisitanteId(request.visitanteId());
            }
            
            nuevaOrden.setTotal(carrito.getTotal());
            nuevaOrden.setMetodoPago(request.metodoPago());
            nuevaOrden.setEstado("confirmada"); // Cambiar de "pendiente" a "confirmada"
            nuevaOrden.setFechaPedido(LocalDateTime.now());
            nuevaOrden.setFechaActualizacion(LocalDateTime.now());

            // Configurar sucursal o dirección de envío
            if (request.sucursalId() != null && !request.sucursalId().isEmpty()) {
                nuevaOrden.setSucursalId(new ObjectId(request.sucursalId()));
            } else {
                nuevaOrden.setDireccionEnvio(request.direccionEnvio());
            }

            // Convertir items del carrito a items de orden (incluyendo sucursalId específico)
            List<Orden.ItemOrden> itemsOrden = carrito.getItems().stream()
                .map(itemCarrito -> new Orden.ItemOrden(
                    itemCarrito.getArticuloId(),
                    itemCarrito.getCantidad(),
                    itemCarrito.getSucursalId(), // Usar la sucursal específica del item
                    itemCarrito.getPrecioUnitario(),
                    itemCarrito.getSubtotal()
                )).toList();

            nuevaOrden.setItems(itemsOrden);

            // *** NUEVA FUNCIONALIDAD: Reducir stock de productos ***
            System.out.println("Actualizando stock de productos...");
            boolean stockSuficiente = true;
            StringBuilder errorMessage = new StringBuilder();

            // Verificar que hay stock suficiente para todos los productos
            for (Orden.ItemOrden item : itemsOrden) {
                Optional<Producto> productoOpt = productoRepository.findById(item.getArticuloId());
                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();
                    int stockDisponible = producto.getStockTotal(); // Usar stock total del inventario
                    if (stockDisponible < item.getCantidad()) {
                        stockSuficiente = false;
                        errorMessage.append("Stock insuficiente para ").append(producto.getNombre())
                                  .append(". Disponible: ").append(stockDisponible)
                                  .append(", solicitado: ").append(item.getCantidad()).append(". ");
                    }
                } else {
                    stockSuficiente = false;
                    errorMessage.append("Producto no encontrado: ").append(item.getArticuloId()).append(". ");
                }
            }

            if (!stockSuficiente) {
                System.out.println("Error de stock: " + errorMessage.toString());
                return ResponseEntity.badRequest().body(errorMessage.toString());
            }

            // Si hay stock suficiente, proceder a reducirlo
            for (Orden.ItemOrden item : itemsOrden) {
                Optional<Producto> productoOpt = productoRepository.findById(item.getArticuloId());
                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();
                    producto.setActualizadoEn(LocalDateTime.now());
                    
                    // Actualizar el inventario por sucursal específica del item
                    if (producto.getInventario() != null && !producto.getInventario().isEmpty()) {
                        int cantidadRestante = item.getCantidad();
                        boolean stockReducido = false;
                        
                        // Usar la sucursal específica de cada item
                        String sucursalItemId = item.getSucursalId();
                        if (sucursalItemId != null && !sucursalItemId.isEmpty()) {
                            System.out.println("Reduciendo stock de sucursal específica del item: " + sucursalItemId);
                            
                            for (Producto.Inventario inv : producto.getInventario()) {
                                // Verificar si esta es la sucursal del item específico
                                if (sucursalItemId.equals(inv.getSucursalId().toString())) {
                                    int stockDisponible = inv.getStock();
                                    if (stockDisponible >= cantidadRestante) {
                                        inv.setStock(stockDisponible - cantidadRestante);
                                        System.out.println("Reducido stock en sucursal " + inv.getSucursalId() + 
                                                         " para producto " + producto.getNombre() + 
                                                         ": " + stockDisponible + " -> " + inv.getStock());
                                        stockReducido = true;
                                        cantidadRestante = 0; // Marcamos que ya se redujo toda la cantidad
                                    } else {
                                        System.out.println("Stock insuficiente en sucursal del item: " + inv.getSucursalId());
                                    }
                                    break;
                                }
                            }
                        } 
                        
                        // Si no se pudo reducir del item específico o no tiene sucursal, usar cualquier disponible
                        if (!stockReducido) {
                            System.out.println("Fallback: reduciendo del primer stock disponible");
                            
                            for (Producto.Inventario inv : producto.getInventario()) {
                                if (cantidadRestante <= 0) break;
                                
                                int stockDisponible = inv.getStock();
                                if (stockDisponible > 0) {
                                    int cantidadAReducir = Math.min(stockDisponible, cantidadRestante);
                                    inv.setStock(stockDisponible - cantidadAReducir);
                                    cantidadRestante -= cantidadAReducir;
                                    System.out.println("Reducido stock en sucursal " + inv.getSucursalId() + 
                                                     ": " + stockDisponible + " -> " + inv.getStock());
                                }
                            }
                        }
                        
                        if (cantidadRestante > 0) {
                            System.out.println("Advertencia: No se pudo reducir toda la cantidad solicitada");
                        }
                    }
                    
                    productoRepository.save(producto);
                    System.out.println("Stock actualizado para " + producto.getNombre() + 
                                     ". Stock total: " + producto.getStockTotal());
                }
            }

            // Guardar orden
            System.out.println("Guardando orden...");
            Orden ordenGuardada = ordenRepository.save(nuevaOrden);
            System.out.println("Orden guardada con ID: " + ordenGuardada.getId());

            // Limpiar carrito después de crear la orden
            carritoRepository.deleteByVisitanteId(request.visitanteId());
            System.out.println("Carrito limpiado para visitante: " + request.visitanteId());

            // Crear respuesta simple con ID como string
            var response = java.util.Map.of(
                "id", ordenGuardada.getId().toString(),
                "total", ordenGuardada.getTotal(),
                "estado", ordenGuardada.getEstado(),
                "metodoPago", ordenGuardada.getMetodoPago(),
                "fechaPedido", ordenGuardada.getFechaPedido().toString()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("Error al crear orden: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear la orden: " + e.getMessage());
        }
    }

    // Obtener órdenes del usuario autenticado
    @GetMapping("/mis-ordenes")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> obtenerMisOrdenes(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            Optional<Usuario> usuarioOpt = userRepository.findByEmail(emailUsuario);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no encontrado");
            }

            List<Orden> ordenes = ordenRepository.findByUsuarioId(new ObjectId(usuarioOpt.get().getId()));
            return ResponseEntity.ok(ordenes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener órdenes: " + e.getMessage());
        }
    }

    // Endpoint temporal para visitantes sin autenticación - devuelve órdenes recientes
    @GetMapping("/visitante/ordenes")
    public ResponseEntity<?> obtenerOrdenesVisitante() {
        try {
            // Como no tenemos autenticación, devolvemos las órdenes más recientes
            // En un sistema real, esto debería asociarse al visitanteId de alguna manera
            List<Orden> ordenesRecientes = ordenRepository.findAll();
            
            // Limitamos a las últimas 10 órdenes para demo
            if (ordenesRecientes.size() > 10) {
                ordenesRecientes = ordenesRecientes.subList(0, 10);
            }
            
            return ResponseEntity.ok(ordenesRecientes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener órdenes: " + e.getMessage());
        }
    }

    // Endpoint para obtener órdenes de un visitante específico o usuario autenticado
    @GetMapping("/visitante/{visitanteId}")
    public ResponseEntity<?> obtenerOrdenesPorVisitante(@PathVariable String visitanteId,
                                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== OBTENER ÓRDENES POR VISITANTE ===");
            System.out.println("VisitanteId: " + visitanteId);
            System.out.println("Auth header recibido: " + authHeader);
            
            String userEmail = obtenerEmailDeToken(authHeader);
            System.out.println("Email extraído del token: " + userEmail);
            List<Orden> ordenes;
            
            if (userEmail != null) {
                // Usuario autenticado: buscar por email
                System.out.println("Buscando órdenes para usuario: " + userEmail);
                ordenes = ordenRepository.findByUsuarioEmail(userEmail);
            } else {
                // Visitante: buscar por visitanteId
                System.out.println("Buscando órdenes para visitante: " + visitanteId);
                ordenes = ordenRepository.findByVisitanteId(visitanteId);
            }
            
            System.out.println("Órdenes encontradas: " + ordenes.size());
            
            return ResponseEntity.ok(ordenes);

        } catch (Exception e) {
            System.err.println("Error al obtener órdenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener órdenes: " + e.getMessage());
        }
    }

    // Obtener orden por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> obtenerOrdenPorId(@PathVariable String id, 
                                              Authentication authentication) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Orden> ordenOpt = ordenRepository.findById(objectId);
            
            if (ordenOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Orden orden = ordenOpt.get();

            // Verificar que el usuario puede acceder a esta orden
            String emailUsuario = authentication.getName();
            Optional<Usuario> usuarioOpt = userRepository.findByEmail(emailUsuario);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // Solo permitir acceso si es el propietario de la orden o es admin
            if (!orden.getUsuarioId().toString().equals(usuario.getId()) && 
                !usuario.getRol().getNombre().equals("ROLE_ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(orden);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener orden: " + e.getMessage());
        }
    }

    // Actualizar estado de orden (solo admins)
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarEstadoOrden(@PathVariable String id, 
                                                  @RequestParam String nuevoEstado) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Orden> ordenOpt = ordenRepository.findById(objectId);
            
            if (ordenOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Orden orden = ordenOpt.get();
            orden.setEstado(nuevoEstado);
            orden.setFechaActualizacion(LocalDateTime.now());

            Orden ordenActualizada = ordenRepository.save(orden);
            return ResponseEntity.ok(ordenActualizada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar estado: " + e.getMessage());
        }
    }

    // Obtener todas las órdenes (solo admins)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Orden>> obtenerTodasLasOrdenes() {
        List<Orden> ordenes = ordenRepository.findAll();
        return ResponseEntity.ok(ordenes);
    }

    // Obtener órdenes por estado (solo admins)
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Orden>> obtenerOrdenesPorEstado(@PathVariable String estado) {
        List<Orden> ordenes = ordenRepository.findByEstado(estado);
        return ResponseEntity.ok(ordenes);
    }

    // Endpoint temporal para actualizar órdenes pendientes a confirmadas
    @PostMapping("/actualizar-pendientes")
    public ResponseEntity<?> actualizarOrdenesPendientes() {
        try {
            System.out.println("=== ACTUALIZANDO ÓRDENES PENDIENTES ===");
            List<Orden> ordenesPendientes = ordenRepository.findByEstado("pendiente");
            System.out.println("Órdenes pendientes encontradas: " + ordenesPendientes.size());
            
            for (Orden orden : ordenesPendientes) {
                orden.setEstado("confirmada");
                orden.setFechaActualizacion(LocalDateTime.now());
                ordenRepository.save(orden);
                System.out.println("Orden actualizada: " + orden.getId() + " -> confirmada");
            }
            
            return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Órdenes actualizadas exitosamente",
                "ordenesActualizadas", ordenesPendientes.size()
            ));
            
        } catch (Exception e) {
            System.err.println("Error al actualizar órdenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar órdenes: " + e.getMessage());
        }
    }

    // Endpoint para forzar actualización de TODAS las órdenes a confirmadas
    @PostMapping("/forzar-actualizar-todas")
    public ResponseEntity<?> forzarActualizarTodasLasOrdenes() {
        try {
            System.out.println("=== FORZANDO ACTUALIZACIÓN DE TODAS LAS ÓRDENES ===");
            List<Orden> todasLasOrdenes = ordenRepository.findAll();
            System.out.println("Total órdenes encontradas: " + todasLasOrdenes.size());
            
            int actualizadas = 0;
            for (Orden orden : todasLasOrdenes) {
                if (!"confirmada".equals(orden.getEstado())) {
                    orden.setEstado("confirmada");
                    orden.setFechaActualizacion(LocalDateTime.now());
                    ordenRepository.save(orden);
                    System.out.println("Orden actualizada: " + orden.getId() + " -> confirmada");
                    actualizadas++;
                }
            }
            
            return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Todas las órdenes actualizadas exitosamente",
                "ordenesActualizadas", actualizadas,
                "totalOrdenes", todasLasOrdenes.size()
            ));
            
        } catch (Exception e) {
            System.err.println("Error al actualizar órdenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar órdenes: " + e.getMessage());
        }
    }
}