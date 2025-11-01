package com.ropa.tienda.controller;

import com.ropa.tienda.model.Producto;
import com.ropa.tienda.repository.ProductoRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// DTOs para filtros
record FiltroProductosRequest(
    Map<String, Object> atributos,
    List<String> sucursales,
    Integer stockMinimo,
    String categoria,
    Double precioMin,
    Double precioMax
) {}

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // RUTA PÚBLICA (Todos pueden ver los productos)
    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // CU-001: Filtro avanzado de productos usando Aggregation Pipeline
    @PostMapping("/filtrar")
    public ResponseEntity<List<Producto>> filtrarProductos(@RequestBody FiltroProductosRequest filtros) {
        try {
            // Crear criterios de filtrado dinámicos
            Criteria criteria = new Criteria();

            // Filtrar por categoría si se especifica
            if (filtros.categoria() != null && !filtros.categoria().isEmpty()) {
                criteria.and("categoria").is(filtros.categoria());
            }

            // Filtrar por rango de precios
            if (filtros.precioMin() != null) {
                criteria.and("precio").gte(filtros.precioMin());
            }
            if (filtros.precioMax() != null) {
                criteria.and("precio").lte(filtros.precioMax());
            }

            // Filtrar por atributos dinámicos (color, talla, marca, etc.)
            if (filtros.atributos() != null && !filtros.atributos().isEmpty()) {
                for (Map.Entry<String, Object> atributo : filtros.atributos().entrySet()) {
                    criteria.and("atributos." + atributo.getKey()).is(atributo.getValue());
                }
            }

            // Filtrar por stock en sucursales específicas
            if (filtros.sucursales() != null && !filtros.sucursales().isEmpty()) {
                Integer stockMin = filtros.stockMinimo() != null ? filtros.stockMinimo() : 1;
                
                // Convertir string IDs a ObjectIds
                List<ObjectId> sucursalIds = filtros.sucursales().stream()
                    .map(ObjectId::new)
                    .toList();

                criteria.and("inventario").elemMatch(
                    Criteria.where("sucursalId").in(sucursalIds)
                           .and("stock").gte(stockMin)
                );
            }

            // Crear operaciones de aggregation
            MatchOperation matchOperation = Aggregation.match(criteria);
            ProjectionOperation projectionOperation = Aggregation.project(
                "nombre", "precio", "categoria", "atributos", "imagenes", "inventario"
            );

            // Ejecutar aggregation pipeline
            Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectionOperation
            );

            AggregationResults<Producto> results = mongoTemplate.aggregate(
                aggregation, "articulos", Producto.class);

            List<Producto> productos = results.getMappedResults();

            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener productos por categoría (método simple)
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> obtenerPorCategoria(@PathVariable String categoria) {
        try {
            Criteria criteria = Criteria.where("categoria").is(categoria);
            MatchOperation matchOperation = Aggregation.match(criteria);
            
            Aggregation aggregation = Aggregation.newAggregation(matchOperation);
            AggregationResults<Producto> results = mongoTemplate.aggregate(
                aggregation, "articulos", Producto.class);

            return ResponseEntity.ok(results.getMappedResults());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // RUTA PROTEGIDA (Solo Administradores)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto nuevoProducto) {
        try {
            nuevoProducto.setCreadoEn(LocalDateTime.now());
            nuevoProducto.setActualizadoEn(LocalDateTime.now());
            Producto productoGuardado = productoRepository.save(nuevoProducto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // RUTA PROTEGIDA (Solo Administradores)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Actualizar producto (Solo Administradores)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable String id, 
                                                      @RequestBody Producto productoActualizado) {
        try {
            if (productoRepository.existsById(id)) {
                productoActualizado.setId(id);
                productoActualizado.setActualizadoEn(LocalDateTime.now());
                Producto productoGuardado = productoRepository.save(productoActualizado);
                return ResponseEntity.ok(productoGuardado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}