package com.ropa.tienda.config;

import com.ropa.tienda.model.Rol;
import com.ropa.tienda.model.Usuario;
import com.ropa.tienda.repository.UserRepository; // ¡Usamos UserRepository!
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository; // Inyección corregida
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos datos si la colección de usuarios está vacía
        if (userRepository.count() == 0) {

            // --- Creación de Usuario Administrador ---
            Usuario admin = new Usuario();
            admin.setEmail("admin@tienda.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRol(new Rol("ROLE_ADMIN"));

            userRepository.save(admin);

            System.out.println(">>> Usuario Administrador insertado: admin@tienda.com / admin123 (ROL: ADMIN)");
            // --- Creación de un Usuario Normal ---
            Usuario user = new Usuario();
            user.setEmail("user@tienda.com");
            user.setPasswordHash(passwordEncoder.encode("user123")); // Revisa el setter aquí también
            user.setRol(new Rol("ROLE_USER"));

            userRepository.save(user);

            System.out.println(">>> Usuario Normal insertado: user@tienda.com / user123 (ROL: USER)");
        }
    }
}
