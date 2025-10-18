package com.ropa.tienda.controller;

import com.ropa.tienda.model.Producto;
import com.ropa.tienda.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Importante
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // RUTA PÚBLICA (Todos pueden ver los productos: ADMIN o USER)
    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // RUTA PROTEGIDA (Solo Administradores)
    @PostMapping
    // Requiere el rol 'ADMIN' para ejecutar. Spring le añade el prefijo 'ROLE_' por defecto.
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto nuevoProducto) {
        Producto productoGuardado = productoRepository.save(nuevoProducto);
        return ResponseEntity.ok(productoGuardado);
    }

    // RUTA PROTEGIDA (Solo Administradores)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}