package com.ropa.tienda.controller;

import com.ropa.tienda.model.Sucursal;
import com.ropa.tienda.repository.SucursalRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    @Autowired
    private SucursalRepository sucursalRepository;

    // Obtener todas las sucursales (público para filtros)
    @GetMapping
    public ResponseEntity<List<Sucursal>> obtenerTodasLasSucursales() {
        List<Sucursal> sucursales = sucursalRepository.findAll();
        return ResponseEntity.ok(sucursales);
    }

    // Obtener sucursal por ID
    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtenerSucursalPorId(@PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Sucursal> sucursal = sucursalRepository.findById(objectId);
            
            if (sucursal.isPresent()) {
                return ResponseEntity.ok(sucursal.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener sucursales por ciudad
    @GetMapping("/ciudad/{ciudad}")
    public ResponseEntity<List<Sucursal>> obtenerSucursalesPorCiudad(@PathVariable String ciudad) {
        List<Sucursal> sucursales = sucursalRepository.findByCiudad(ciudad);
        return ResponseEntity.ok(sucursales);
    }

    // Crear nueva sucursal (solo admins)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sucursal> crearSucursal(@RequestBody Sucursal nuevaSucursal) {
        try {
            nuevaSucursal.setCreadoEn(LocalDateTime.now());
            Sucursal sucursalGuardada = sucursalRepository.save(nuevaSucursal);
            return ResponseEntity.status(HttpStatus.CREATED).body(sucursalGuardada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Actualizar sucursal (solo admins)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sucursal> actualizarSucursal(@PathVariable String id, 
                                                      @RequestBody Sucursal sucursalActualizada) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Sucursal> sucursalExistente = sucursalRepository.findById(objectId);
            
            if (sucursalExistente.isPresent()) {
                Sucursal sucursal = sucursalExistente.get();
                sucursal.setNombre(sucursalActualizada.getNombre());
                sucursal.setDireccion(sucursalActualizada.getDireccion());
                sucursal.setCiudad(sucursalActualizada.getCiudad());
                sucursal.setHorarios(sucursalActualizada.getHorarios());
                
                Sucursal sucursalGuardada = sucursalRepository.save(sucursal);
                return ResponseEntity.ok(sucursalGuardada);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Eliminar sucursal (solo admins)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarSucursal(@PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            if (sucursalRepository.existsById(objectId)) {
                sucursalRepository.deleteById(objectId);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}