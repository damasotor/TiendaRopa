package com.ropa.tienda.controller;

import com.ropa.tienda.model.Producto;
import com.ropa.tienda.repository.ProductoRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        List<Producto> productos = productoRepository.findAll();
        System.out.println("=== OBTENER TODOS LOS PRODUCTOS ===");
        System.out.println("Total productos encontrados: " + productos.size());
        if (!productos.isEmpty()) {
            System.out.println("Ejemplo de ID del primer producto: " + productos.get(0).getId());
            System.out.println("Nombre del primer producto: " + productos.get(0).getNombre());
        }
        return productos;
    }

    // CU-001: Filtro avanzado de productos usando Aggregation Pipeline
    @PostMapping("/filtrar")
    public ResponseEntity<List<Producto>> filtrarProductos(@RequestBody FiltroProductosRequest filtros) {
        System.out.println("=== FILTRAR PRODUCTOS ===");
        System.out.println("Filtros recibidos: " + filtros);
        
        try {
            // Crear criterios de filtrado dinámicos
            Criteria criteria = new Criteria();

            // Filtrar por categoría si se especifica
            if (filtros.categoria() != null && !filtros.categoria().isEmpty()) {
                System.out.println("Aplicando filtro por categoría: " + filtros.categoria());
                criteria.and("categoria").is(filtros.categoria());
            }

            // Filtrar por rango de precios
            if (filtros.precioMin() != null) {
                System.out.println("Aplicando filtro por precio mínimo: " + filtros.precioMin());
                criteria.and("precio").gte(filtros.precioMin());
            }
            if (filtros.precioMax() != null) {
                System.out.println("Aplicando filtro por precio máximo: " + filtros.precioMax());
                criteria.and("precio").lte(filtros.precioMax());
            }

            // Filtrar por atributos dinámicos (color, talla, marca, etc.)
            if (filtros.atributos() != null && !filtros.atributos().isEmpty()) {
                System.out.println("Aplicando filtros por atributos: " + filtros.atributos());
                for (Map.Entry<String, Object> atributo : filtros.atributos().entrySet()) {
                    String key = "atributos." + atributo.getKey();
                    Object value = atributo.getValue();
                    System.out.println("Filtro atributo: " + key + " = " + value);
                    criteria.and(key).is(value);
                }
            }

            // Filtrar por sucursal usando la estructura de inventario
            if (filtros.sucursales() != null && !filtros.sucursales().isEmpty()) {
                System.out.println("Aplicando filtro por sucursales: " + filtros.sucursales());
                System.out.println("Tipo de sucursales: " + filtros.sucursales().getClass());
                
                // Crear lista que incluya tanto String IDs como ObjectIds para compatibilidad
                List<Object> sucursalIds = new ArrayList<>();
                for (String sucursal : filtros.sucursales()) {
                    try {
                        // Agregar como String (para compatibilidad con inventario que tiene string IDs)
                        sucursalIds.add(sucursal);
                        
                        // También agregar como ObjectId (para compatibilidad con estructura nueva)
                        ObjectId objectId = new ObjectId(sucursal);
                        sucursalIds.add(objectId);
                        
                        System.out.println("  - Sucursal ID agregado como String: '" + sucursal + "'");
                        System.out.println("  - Sucursal ID agregado como ObjectId: " + objectId);
                    } catch (IllegalArgumentException e) {
                        System.err.println("ID de sucursal inválido: " + sucursal);
                        // Solo agregar como string si no es un ObjectId válido
                        sucursalIds.add(sucursal);
                    }
                }
                
                if (!sucursalIds.isEmpty()) {
                    // Filtrar productos que tengan inventario en alguna de las sucursales especificadas
                    criteria.and("inventario.sucursal_id").in(sucursalIds);
                    System.out.println("Criterio de sucursal aplicado: inventario.sucursal_id in " + sucursalIds);
                }
            }

            // Filtrar por stock mínimo usando la estructura de inventario
            if (filtros.stockMinimo() != null) {
                System.out.println("Aplicando filtro por stock mínimo: " + filtros.stockMinimo());
                // Filtrar productos que tengan al menos el stock mínimo en alguna sucursal
                criteria.and("inventario.stock").gte(filtros.stockMinimo());
            }

            // Crear operaciones de aggregation
            MatchOperation matchOperation = Aggregation.match(criteria);

            // Ejecutar aggregation pipeline
            Aggregation aggregation = Aggregation.newAggregation(matchOperation);

            System.out.println("Ejecutando aggregation en colección 'productos'");
            System.out.println("Criterios finales: " + criteria.getCriteriaObject());
            AggregationResults<Producto> results = mongoTemplate.aggregate(
                aggregation, "productos", Producto.class);

            List<Producto> productos = results.getMappedResults();
            System.out.println("Productos encontrados: " + productos.size());
            if (!productos.isEmpty()) {
                System.out.println("Primer producto encontrado: " + productos.get(0).getNombre());
            }

            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            System.err.println("Error en filtrarProductos: " + e.getMessage());
            e.printStackTrace();
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
                aggregation, "productos", Producto.class);

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