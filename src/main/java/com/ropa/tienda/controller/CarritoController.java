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

    // CU-002: Obtener carrito por visitante
    @GetMapping("/{visitanteId}")
    public ResponseEntity<Carrito> obtenerCarrito(@PathVariable String visitanteId) {
        Optional<Carrito> carrito = carritoRepository.findByVisitanteId(visitanteId);
        
        if (carrito.isPresent()) {
            return ResponseEntity.ok(carrito.get());
        } else {
            // Crear carrito vacío si no existe
            Carrito nuevoCarrito = new Carrito(
                visitanteId, 
                false, // asumimos visitante anónimo por defecto
                0.0, 
                LocalDateTime.now(), 
                LocalDateTime.now()
            );
            Carrito carritoGuardado = carritoRepository.save(nuevoCarrito);
            return ResponseEntity.ok(carritoGuardado);
        }
    }

    // CU-002: Agregar item al carrito (operación atómica)
    @PostMapping("/{visitanteId}/items")
    public ResponseEntity<?> agregarItem(@PathVariable String visitanteId, 
                                        @RequestBody AgregarItemRequest request) {
        try {
            // Verificar que el producto existe
            Optional<Producto> producto = productoRepository.findById(request.articuloId());
            if (producto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado");
            }

            // Obtener o crear carrito
            Carrito carrito = carritoRepository.findByVisitanteId(visitanteId)
                .orElse(new Carrito(visitanteId, false, 0.0, LocalDateTime.now(), LocalDateTime.now()));

            // Verificar si el item ya existe en el carrito
            Optional<Carrito.ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getArticuloId().equals(request.articuloId()))
                .findFirst();

            if (itemExistente.isPresent()) {
                // Incrementar cantidad del item existente
                Carrito.ItemCarrito item = itemExistente.get();
                item.setCantidad(item.getCantidad() + request.cantidad());
                item.setSubtotal(item.getCantidad() * item.getPrecioUnitario());
            } else {
                // Agregar nuevo item
                Carrito.ItemCarrito nuevoItem = new Carrito.ItemCarrito(
                    request.articuloId(), // Usar el ID como string directamente
                    request.cantidad(),
                    producto.get().getPrecio(),
                    request.cantidad() * producto.get().getPrecio()
                );
                carrito.getItems().add(nuevoItem);
            }

            // Actualizar total del carrito
            double nuevoTotal = carrito.getItems().stream()
                .mapToDouble(Carrito.ItemCarrito::getSubtotal)
                .sum();
            carrito.setTotal(nuevoTotal);
            carrito.setUltimaActividad(LocalDateTime.now());

            // Guardar carrito actualizado
            Carrito carritoActualizado = carritoRepository.save(carrito);
            
            return ResponseEntity.ok(carritoActualizado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al agregar item al carrito: " + e.getMessage());
        }
    }

    // CU-002: Eliminar item del carrito
    @DeleteMapping("/{visitanteId}/items")
    public ResponseEntity<?> eliminarItem(@PathVariable String visitanteId,
                                         @RequestBody EliminarItemRequest request) {
        try {
            Optional<Carrito> carritoOpt = carritoRepository.findByVisitanteId(visitanteId);
            
            if (carritoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Carrito no encontrado");
            }

            Carrito carrito = carritoOpt.get();
            
            // Remover item del carrito
            carrito.getItems().removeIf(item -> 
                item.getArticuloId().equals(request.articuloId()));

            // Recalcular total
            double nuevoTotal = carrito.getItems().stream()
                .mapToDouble(Carrito.ItemCarrito::getSubtotal)
                .sum();
            carrito.setTotal(nuevoTotal);
            carrito.setUltimaActividad(LocalDateTime.now());

            // Guardar carrito actualizado
            Carrito carritoActualizado = carritoRepository.save(carrito);
            
            return ResponseEntity.ok(carritoActualizado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar item del carrito: " + e.getMessage());
        }
    }

    // Limpiar carrito
    @DeleteMapping("/{visitanteId}")
    public ResponseEntity<?> limpiarCarrito(@PathVariable String visitanteId) {
        try {
            carritoRepository.deleteByVisitanteId(visitanteId);
            return ResponseEntity.ok("Carrito limpiado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al limpiar carrito: " + e.getMessage());
        }
    }
}