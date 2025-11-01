package com.ropa.tienda.repository;

import com.ropa.tienda.model.Sucursal;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalRepository extends MongoRepository<Sucursal, ObjectId> {
    
    // Buscar sucursales por ciudad
    List<Sucursal> findByCiudad(String ciudad);
    
    // Buscar sucursal por nombre
    List<Sucursal> findByNombre(String nombre);
}