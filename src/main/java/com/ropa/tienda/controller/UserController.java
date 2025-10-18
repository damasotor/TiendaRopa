package com.ropa.tienda.controller;

import com.ropa.tienda.model.Usuario;
import com.ropa.tienda.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Solo accesible por un usuario autenticado con el rol ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsers() {
        // Devuelve la lista de usuarios directamente desde el repositorio
        List<Usuario> usuarios = userRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}") // e.g., DELETE /api/users/65f9f8f...
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Usuario con ID " + id + " eliminado exitosamente.");
    }
}