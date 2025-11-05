package com.ropa.tienda.controller;

import com.ropa.tienda.model.Usuario;
import com.ropa.tienda.payload.RegistroDto;
import com.ropa.tienda.model.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import com.ropa.tienda.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

record LoginRequest(String email, String password) {}
record AuthResponse(String token, String rol) {}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Mapa para asociar tokens con emails de usuarios
    public static final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = userRepository.findByEmail(loginRequest.email()).orElse(null);
            if (usuario != null && passwordEncoder.matches(loginRequest.password(), usuario.getPasswordHash())) {
                // Buscar si ya existe un token activo para este usuario
                String existingToken = null;
                for (Map.Entry<String, String> entry : activeTokens.entrySet()) {
                    if (entry.getValue().equals(usuario.getEmail())) {
                        existingToken = entry.getKey();
                        break;
                    }
                }
                
                // Si no existe un token activo, crear uno nuevo
                String token = existingToken != null ? existingToken : "token-" + usuario.getEmail().hashCode() + "-" + System.currentTimeMillis();
                if (existingToken == null) {
                    activeTokens.put(token, usuario.getEmail());
                }
                
                return ResponseEntity.ok(new AuthResponse(token, usuario.getRol().getNombre()));
            } else {
                return ResponseEntity.status(401).body("Credenciales Inválidas. Acceso Denegado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales Inválidas. Acceso Denegado.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistroDto registroDto) {
        if (userRepository.existsByEmail(registroDto.getEmail())) {
            return new ResponseEntity<>("Email ya registrado!", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(registroDto.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(registroDto.getPassword()));
        
        // Asegurar que siempre hay un nombre (requerido por el schema de MongoDB)
        if (registroDto.getNombre() != null && !registroDto.getNombre().trim().isEmpty()) {
            usuario.setNombre(registroDto.getNombre().trim());
        } else {
            // Si no se proporciona nombre, usar el email como nombre por defecto
            String emailNombre = registroDto.getEmail().split("@")[0];
            usuario.setNombre(emailNombre);
        }
        
        // Configurar rol (por defecto USER, pero permitir ADMIN desde el DTO)
        String rolNombre = "ROLE_USER";
        if (registroDto.getRol() != null && registroDto.getRol().startsWith("ROLE_")) {
            rolNombre = registroDto.getRol();
        }
        usuario.setRol(new Rol(rolNombre));
        
        // Configurar campos adicionales
        usuario.setFechaRegistro(java.time.LocalDateTime.now());
        usuario.setActivo(true);

        userRepository.save(usuario);
        return new ResponseEntity<>("Usuario registrado exitosamente!", HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Extraer el token del header Authorization
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // Buscar el email asociado con este token
                String userEmail = activeTokens.get(token);
                if (userEmail != null) {
                    // Buscar el usuario por email
                    Usuario usuario = userRepository.findByEmail(userEmail).orElse(null);
                    if (usuario != null) {
                        Map<String, String> response = new HashMap<>();
                        response.put("email", usuario.getEmail());
                        response.put("rol", usuario.getRol().getNombre());
                        response.put("status", "valid");
                        return ResponseEntity.ok(response);
                    }
                }
            }
            
            // Token inválido o no encontrado
            return ResponseEntity.status(401).body("Token inválido");
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error al verificar token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                activeTokens.remove(token);
                return ResponseEntity.ok("Logout exitoso");
            }
            return ResponseEntity.ok("No hay sesión activa");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cerrar sesión");
        }
    }
}