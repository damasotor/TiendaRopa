package com.ropa.tienda.repository;

import com.ropa.tienda.model.Orden;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdenRepository extends MongoRepository<Orden, ObjectId> {
    
    // Buscar órdenes por usuario
    List<Orden> findByUsuarioId(ObjectId usuarioId);
    
    // Buscar órdenes por estado
    List<Orden> findByEstado(String estado);
    
    // Buscar órdenes por sucursal
    List<Orden> findBySucursalId(ObjectId sucursalId);
    
    // Buscar órdenes en un rango de fechas
    List<Orden> findByFechaPedidoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Buscar órdenes por usuario y estado
    List<Orden> findByUsuarioIdAndEstado(ObjectId usuarioId, String estado);
}