package com.ropa.tienda.controller;

import com.ropa.tienda.model.Usuario;
import com.ropa.tienda.payload.RegistroDto;
import com.ropa.tienda.model.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.ropa.tienda.security.JwtTokenProvider;
import java.util.Map;
import java.util.HashMap;
import com.ropa.tienda.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

record LoginRequest(String email, String password) {}
record AuthResponse(String token, String rol) {}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Inyecta el AuthenticationManager que validará las credenciales
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // VARIABLES DE INSTANCIA
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // *** REEMPLAZO CLAVE: GENERAR EL TOKEN REAL ***
            String jwt = tokenProvider.generateToken(authentication);
            String rol = authentication.getAuthorities().iterator().next().getAuthority();

            // Devuelve el token JWT real
            return ResponseEntity.ok(new AuthResponse(jwt, rol));

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

        // 1. Hashear la contraseña y usar el setter correcto: setPasswordHash
        usuario.setPasswordHash(passwordEncoder.encode(registroDto.getPassword()));

        // 2. Asignar el Rol por defecto (asumiendo que tienes un constructor en Rol)
        // Usamos el setter de un solo rol: setRol
        usuario.setRol(new Rol("ROLE_USER"));


        // 3. Guardar en MongoDB
        userRepository.save(usuario);

        return new ResponseEntity<>("Usuario registrado exitosamente!", HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken() {
        // Si llegamos aquí, el JWT es válido (se verifica en el filtro)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            String rol = authentication.getAuthorities().iterator().next().getAuthority();
            
            Map<String, String> response = new HashMap<>();
            response.put("email", email);
            response.put("rol", rol);
            response.put("status", "valid");
            
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
    }
}