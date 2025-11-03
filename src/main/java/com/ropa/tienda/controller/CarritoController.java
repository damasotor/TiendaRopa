package com.ropa.tienda.controller;

import com.ropa.tienda.model.Carrito;
import com.ropa.tienda.model.Producto;
import com.ropa.tienda.repository.CarritoRepository;
import com.ropa.tienda.repository.ProductoRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// DTOs para las peticiones
record AgregarItemRequest(String articuloId, Integer cantidad) {}
record EliminarItemRequest(String articuloId) {}

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Método helper para obtener email del usuario desde el token
    private String obtenerEmailDeToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Acceder al mapa de tokens del AuthController
            return AuthController.activeTokens.get(token);
        }
        return null;
    }

    // CU-002: Obtener carrito por visitante o usuario autenticado
    @GetMapping("/{visitanteId}")
    public ResponseEntity<Carrito> obtenerCarrito(@PathVariable String visitanteId,
                                                 @RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("=== OBTENER CARRITO ===");
        System.out.println("VisitanteId: " + visitanteId);
        
        String userEmail = obtenerEmailDeToken(authHeader);
        Carrito carrito = null;
        
        if (userEmail != null) {
            // Usuario autenticado: buscar carrito por email
            System.out.println("Usuario autenticado: " + userEmail);
            Optional<Carrito> carritoUsuario = carritoRepository.findByUsuarioEmail(userEmail);
            if (carritoUsuario.isPresent()) {
                carrito = carritoUsuario.get();
                System.out.println("Carrito de usuario encontrado con " + carrito.getItems().size() + " items");
            }
        } else {
            // Visitante: buscar carrito por visitanteId
            Optional<Carrito> carritoVisitante = carritoRepository.findByVisitanteId(visitanteId);
            if (carritoVisitante.isPresent()) {
                carrito = carritoVisitante.get();
                System.out.println("Carrito de visitante encontrado con " + carrito.getItems().size() + " items");
            }
        }
        
        if (carrito != null) {
            for (Carrito.ItemCarrito item : carrito.getItems()) {
                System.out.println("Item en carrito - ArticuloId: " + item.getArticuloId() + ", Cantidad: " + item.getCantidad());
            }
            return ResponseEntity.ok(carrito);
        } else {
            System.out.println("Carrito no encontrado, creando uno nuevo");
            // Crear carrito vacío
            Carrito nuevoCarrito = new Carrito(
                visitanteId, 
                userEmail != null, // registrado si hay usuario autenticado
                0.0, 
                LocalDateTime.now(), 
                LocalDateTime.now()
            );
            
            if (userEmail != null) {
                nuevoCarrito.setUsuarioEmail(userEmail);
            }
            
            Carrito carritoGuardado = carritoRepository.save(nuevoCarrito);
            return ResponseEntity.ok(carritoGuardado);
        }
    }

    // CU-002: Agregar item al carrito (operación atómica)
    @PostMapping("/{visitanteId}/items")
    public ResponseEntity<?> agregarItem(@PathVariable String visitanteId, 
                                        @RequestBody AgregarItemRequest request,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== AGREGAR ITEM AL CARRITO ===");
            System.out.println("VisitanteId: " + visitanteId);
            System.out.println("ArticuloId recibido: " + request.articuloId());
            System.out.println("Cantidad: " + request.cantidad());
            
            // Verificar que el producto existe
            Optional<Producto> producto = productoRepository.findById(request.articuloId());
            System.out.println("Producto encontrado: " + producto.isPresent());
            
            if (producto.isEmpty()) {
                System.out.println("ERROR: Producto no encontrado con ID: " + request.articuloId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado");
            }

            System.out.println("Producto encontrado: " + producto.get().getNombre());

            // Obtener email del usuario si está autenticado
            String userEmail = obtenerEmailDeToken(authHeader);
            Carrito carrito = null;
            
            if (userEmail != null) {
                // Usuario autenticado: buscar carrito por email
                carrito = carritoRepository.findByUsuarioEmail(userEmail)
                    .orElse(new Carrito(visitanteId, true, 0.0, LocalDateTime.now(), LocalDateTime.now()));
                carrito.setUsuarioEmail(userEmail);
                carrito.setRegistrado(true);
            } else {
                // Visitante: buscar carrito por visitanteId
                carrito = carritoRepository.findByVisitanteId(visitanteId)
                    .orElse(new Carrito(visitanteId, false, 0.0, LocalDateTime.now(), LocalDateTime.now()));
            }

            // Verificar si el item ya existe en el carrito
            Optional<Carrito.ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getArticuloId().equals(request.articuloId()))
                .findFirst();

            if (itemExistente.isPresent()) {
                // Incrementar cantidad del item existente
                Carrito.ItemCarrito item = itemExistente.get();
                item.setCantidad(item.getCantidad() + request.cantidad());
                item.setSubtotal(item.getCantidad() * item.getPrecioUnitario());
                System.out.println("Item existente actualizado");
            } else {
                // Agregar nuevo item
                Carrito.ItemCarrito nuevoItem = new Carrito.ItemCarrito(
                    request.articuloId(), // Usar el ID como string directamente
                    request.cantidad(),
                    producto.get().getPrecio(),
                    request.cantidad() * producto.get().getPrecio()
                );
                carrito.getItems().add(nuevoItem);
                System.out.println("Nuevo item agregado con ID: " + request.articuloId());
            }

            // Actualizar total del carrito
            double nuevoTotal = carrito.getItems().stream()
                .mapToDouble(Carrito.ItemCarrito::getSubtotal)
                .sum();
            carrito.setTotal(nuevoTotal);
            carrito.setUltimaActividad(LocalDateTime.now());

            // Guardar carrito actualizado
            Carrito carritoActualizado = carritoRepository.save(carrito);
            System.out.println("Carrito guardado con " + carritoActualizado.getItems().size() + " items");
            
            return ResponseEntity.ok(carritoActualizado);

        } catch (Exception e) {
            System.err.println("Error agregando item al carrito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    // CU-002: Eliminar item del carrito
    @DeleteMapping("/{visitanteId}/items")
    public ResponseEntity<?> eliminarItem(@PathVariable String visitanteId,
                                         @RequestBody EliminarItemRequest request,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== ELIMINAR ITEM DEL CARRITO ===");
            System.out.println("VisitanteId: " + visitanteId);
            System.out.println("ArticuloId a eliminar: " + request.articuloId());
            
            // Obtener email del usuario si está autenticado
            String userEmail = obtenerEmailDeToken(authHeader);
            Carrito carrito = null;
            
            if (userEmail != null) {
                // Usuario autenticado: buscar carrito por email
                Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioEmail(userEmail);
                if (carritoOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Carrito no encontrado para usuario autenticado");
                }
                carrito = carritoOpt.get();
                System.out.println("Carrito de usuario autenticado encontrado");
            } else {
                // Visitante: buscar carrito por visitanteId
                Optional<Carrito> carritoOpt = carritoRepository.findByVisitanteId(visitanteId);
                if (carritoOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Carrito no encontrado para visitante");
                }
                carrito = carritoOpt.get();
                System.out.println("Carrito de visitante encontrado");
            }
            
            // Verificar que el item existe antes de eliminarlo
            boolean itemExistia = carrito.getItems().stream()
                .anyMatch(item -> item.getArticuloId().equals(request.articuloId()));
            
            if (!itemExistia) {
                System.out.println("Item no encontrado en el carrito");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item no encontrado en el carrito");
            }
            
            // Remover item del carrito
            boolean removido = carrito.getItems().removeIf(item -> 
                item.getArticuloId().equals(request.articuloId()));
            
            System.out.println("Item eliminado: " + removido);
            System.out.println("Items restantes en carrito: " + carrito.getItems().size());

            // Recalcular total
            double nuevoTotal = carrito.getItems().stream()
                .mapToDouble(Carrito.ItemCarrito::getSubtotal)
                .sum();
            carrito.setTotal(nuevoTotal);
            carrito.setUltimaActividad(LocalDateTime.now());

            // Guardar carrito actualizado
            Carrito carritoActualizado = carritoRepository.save(carrito);
            System.out.println("Carrito actualizado, nuevo total: " + carritoActualizado.getTotal());
            
            return ResponseEntity.ok(carritoActualizado);

        } catch (Exception e) {
            System.out.println("Error eliminando item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar item del carrito: " + e.getMessage());
        }
    }

    // Limpiar carrito
    @DeleteMapping("/{visitanteId}")
    public ResponseEntity<?> limpiarCarrito(@PathVariable String visitanteId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== LIMPIAR CARRITO ===");
            System.out.println("VisitanteId: " + visitanteId);
            
            // Obtener email del usuario si está autenticado
            String userEmail = obtenerEmailDeToken(authHeader);
            
            if (userEmail != null) {
                // Usuario autenticado: limpiar carrito por email
                Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioEmail(userEmail);
                if (carritoOpt.isPresent()) {
                    Carrito carrito = carritoOpt.get();
                    carrito.getItems().clear();
                    carrito.setTotal(0.0);
                    carrito.setUltimaActividad(LocalDateTime.now());
                    carritoRepository.save(carrito);
                    System.out.println("Carrito de usuario autenticado limpiado");
                } else {
                    System.out.println("Carrito de usuario no encontrado");
                }
            } else {
                // Visitante: limpiar carrito por visitanteId
                Optional<Carrito> carritoOpt = carritoRepository.findByVisitanteId(visitanteId);
                if (carritoOpt.isPresent()) {
                    Carrito carrito = carritoOpt.get();
                    carrito.getItems().clear();
                    carrito.setTotal(0.0);
                    carrito.setUltimaActividad(LocalDateTime.now());
                    carritoRepository.save(carrito);
                    System.out.println("Carrito de visitante limpiado");
                } else {
                    System.out.println("Carrito de visitante no encontrado");
                }
            }
            
            return ResponseEntity.ok("Carrito limpiado exitosamente");
        } catch (Exception e) {
            System.out.println("Error limpiando carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al limpiar carrito: " + e.getMessage());
        }
    }
}