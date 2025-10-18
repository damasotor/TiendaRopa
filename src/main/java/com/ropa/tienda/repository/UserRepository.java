package com.ropa.tienda.repository;

import com.ropa.tienda.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<Usuario, String> {

    // Necesario para el login
    Optional<Usuario> findByEmail(String email);

    // NECESARIO PARA EL REGISTRO (Error: No candidates found for method call userRepository.existsByEmail)
    Boolean existsByEmail(String email);
}