package com.ropa.tienda.controller;

import com.ropa.tienda.model.Carrito;
import com.ropa.tienda.model.Orden;
import com.ropa.tienda.model.Usuario;
import com.ropa.tienda.repository.CarritoRepository;
import com.ropa.tienda.repository.OrdenRepository;
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
    private UserRepository userRepository;

    // Crear orden desde carrito
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> crearOrden(@RequestBody CrearOrdenRequest request, 
                                       Authentication authentication) {
        try {
            // Obtener usuario autenticado
            String emailUsuario = authentication.getName();
            Optional<Usuario> usuarioOpt = userRepository.findByEmail(emailUsuario);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Obtener carrito del visitante
            Optional<Carrito> carritoOpt = carritoRepository.findByVisitanteId(request.visitanteId());
            
            if (carritoOpt.isEmpty() || carritoOpt.get().getItems().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Carrito vacío o no encontrado");
            }

            Carrito carrito = carritoOpt.get();

            // Crear orden a partir del carrito
            Orden nuevaOrden = new Orden();
            nuevaOrden.setUsuarioId(new ObjectId(usuario.getId()));
            nuevaOrden.setTotal(carrito.getTotal());
            nuevaOrden.setMetodoPago(request.metodoPago());
            nuevaOrden.setEstado("pendiente");
            nuevaOrden.setFechaPedido(LocalDateTime.now());
            nuevaOrden.setFechaActualizacion(LocalDateTime.now());

            // Configurar sucursal o dirección de envío
            if (request.sucursalId() != null) {
                nuevaOrden.setSucursalId(new ObjectId(request.sucursalId()));
            } else {
                nuevaOrden.setDireccionEnvio(request.direccionEnvio());
            }

            // Convertir items del carrito a items de orden
            List<Orden.ItemOrden> itemsOrden = carrito.getItems().stream()
                .map(itemCarrito -> new Orden.ItemOrden(
                    itemCarrito.getArticuloId(),
                    itemCarrito.getCantidad(),
                    itemCarrito.getPrecioUnitario(),
                    itemCarrito.getSubtotal()
                )).toList();

            nuevaOrden.setItems(itemsOrden);

            // Guardar orden
            Orden ordenGuardada = ordenRepository.save(nuevaOrden);

            // Limpiar carrito después de crear la orden
            carritoRepository.deleteByVisitanteId(request.visitanteId());

            return ResponseEntity.status(HttpStatus.CREATED).body(ordenGuardada);

        } catch (Exception e) {
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
}