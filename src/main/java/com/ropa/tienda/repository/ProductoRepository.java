package com.ropa.tienda.repository;

import com.ropa.tienda.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// La interfaz extiende MongoRepository, especificando la clase de modelo (Producto)
// y el tipo de su ID (String).
@Repository
public interface ProductoRepository extends MongoRepository<Producto, String> {

    // Opcional: Aquí podrías añadir métodos de consulta personalizados, por ejemplo:
    // List<Producto> findByMarca(String marca);
    // List<Producto> findByPrecioLessThan(double maxPrecio);
}