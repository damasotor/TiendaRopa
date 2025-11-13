package com.ropa.tienda.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.dao.DataAccessException;
import com.mongodb.MongoCommandException;
import org.springframework.stereotype.Component;
import org.bson.Document;

@Component
public class MongoIndexConfig implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        crearIndicesProductos();
        crearIndicesCarritos();
        crearIndicesOrdenes();
        System.out.println(">>> Índices MongoDB creados correctamente");
    }

    private void crearIndicesProductos() {
        // Índice compuesto para inventario (sucursal_id + stock) - CU-001
        IndexDefinition inventarioIndex = new CompoundIndexDefinition(
            new Document("inventario.sucursal_id", 1)
                .append("inventario.stock", 1)
        ).named("idx_inventario_sucursal_stock");
        
        ensureIndexSafe("productos", inventarioIndex);

        // Índices para filtros comunes
        ensureIndexSafe("productos", new Index().on("categoria", Sort.Direction.ASC).named("idx_categoria"));
        
        ensureIndexSafe("productos", new Index().on("precio", Sort.Direction.ASC).named("idx_precio"));

        // Índices para atributos dinámicos más comunes
        ensureIndexSafe("productos", new Index().on("atributos.color", Sort.Direction.ASC).named("idx_atributos_color"));
    }

    private void crearIndicesCarritos() {
        // Índice TTL para auto-limpieza de carritos - CU-002
        ensureIndexSafe("carritos", new Index().on("ultimaActividad", Sort.Direction.ASC)
            .expire(java.time.Duration.ofDays(30))
            .named("idx_carrito_ttl")
        );

        // Índice por visitante ID
        ensureIndexSafe("carritos", new Index().on("visitanteId", Sort.Direction.ASC).named("idx_visitante_id"));
    }

    private void crearIndicesOrdenes() {
        // Índice por usuario ID
        ensureIndexSafe("ordenes", new Index().on("usuarioId", Sort.Direction.ASC).named("idx_orden_usuario_id"));
    }

    private void ensureIndexSafe(String collection, IndexDefinition indexDefinition) {

        /* que la aplicación no falle al arrancar si otro servicio o una versión anterior 
        ya creó un índice con el mismo nombre pero con propiedades ligeramente diferentes */

        try {
            mongoTemplate.indexOps(collection).ensureIndex(indexDefinition);
        } catch (DataAccessException dae) {
            Throwable cause = dae.getCause();
            if (cause instanceof MongoCommandException) {
                MongoCommandException mce = (MongoCommandException) cause;
                
                if ("IndexKeySpecsConflict".equals(mce.getErrorCodeName()) || 
                    "IndexOptionsConflict".equals(mce.getErrorCodeName()) ||
                    mce.getCode() == 86 || mce.getCode() == 85) {
                    System.err.println("[WARN] Conflicto de indices ignorado para la coleccion '" + collection + "': " + mce.getErrorMessage());
                    return;
                }
            }
            
            throw dae;
        } catch (Exception e) {
            // Sigue siendo una excepción desconocida, revisamos el mensaje
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("IndexKeySpecsConflict") || 
                msg.contains("IndexOptionsConflict") ||
                msg.contains("code: 86") || 
                msg.contains("code: 85") ||
                msg.contains("Index already exists")) {
                System.err.println("[WARN] Conflicto de indices ignorado para la coleccion '" + collection + "': " + msg);
                return;
            }
            throw e;
        }
    }
}