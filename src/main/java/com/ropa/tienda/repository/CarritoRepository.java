package com.ropa.tienda.repository;

import com.ropa.tienda.model.Carrito;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends MongoRepository<Carrito, ObjectId> {
    
    // Buscar carrito por ID de visitante (cookie de sesión o ID de usuario)
    Optional<Carrito> findByVisitanteId(String visitanteId);
    
    // Verificar si existe un carrito para el visitante
    Boolean existsByVisitanteId(String visitanteId);
    
    // Eliminar carrito por visitante ID
    void deleteByVisitanteId(String visitanteId);
    
    // Buscar carrito por email de usuario
    Optional<Carrito> findByUsuarioEmail(String usuarioEmail);
    
    // Verificar si existe un carrito para el usuario
    Boolean existsByUsuarioEmail(String usuarioEmail);
}