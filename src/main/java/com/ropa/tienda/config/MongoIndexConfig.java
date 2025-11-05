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
        crearIndicesSucursales();
        System.out.println(">>> Índices MongoDB creados correctamente");
    }

    private void crearIndicesProductos() {
        // Índice compuesto para inventario (sucursal_id + stock) - CU-001
        // Use the same field name as in MongoDB script (inventario.sucursal_id)
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
        
        ensureIndexSafe("productos", new Index().on("atributos.talla", Sort.Direction.ASC).named("idx_atributos_talla"));
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

        // Nota: la entidad `Orden` ya declara @Indexed(name = "idx_fecha_pedido") sobre
        // el campo `fechaPedido`. Para evitar conflictos con índices preexistentes en Atlas
        // (mismo nombre pero distinta especificación), no creamos aquí el índice programáticamente.
        // Si necesitas un índice con diferente orden/dirección, crea uno con un nombre distinto.

        // Índice por estado
        ensureIndexSafe("ordenes", new Index().on("estado", Sort.Direction.ASC).named("idx_estado"));

        // Índice por sucursal
        ensureIndexSafe("ordenes", new Index().on("sucursalId", Sort.Direction.ASC).named("idx_orden_sucursal_id"));

        // Índice compuesto para consultas complejas (usuario + estado)
        IndexDefinition usuarioEstadoIndex = new CompoundIndexDefinition(
            new Document("usuarioId", 1).append("estado", 1)
        ).named("idx_usuario_estado");
        
        ensureIndexSafe("ordenes", usuarioEstadoIndex);
    }

    private void crearIndicesSucursales() {
        // Índice por nombre
        ensureIndexSafe("sucursales", new Index().on("nombre", Sort.Direction.ASC).named("idx_sucursal_nombre"));
    }

    /**
     * Try to create an index but do not fail startup if there is an existing index
     * with the same name but different key specification or options.
     */
    private void ensureIndexSafe(String collection, IndexDefinition indexDefinition) {
        try {
            mongoTemplate.indexOps(collection).ensureIndex(indexDefinition);
        } catch (DataAccessException dae) {
            Throwable cause = dae.getCause();
            if (cause instanceof MongoCommandException) {
                MongoCommandException mce = (MongoCommandException) cause;
                // Handle both IndexKeySpecsConflict (86) and IndexOptionsConflict (85)
                if ("IndexKeySpecsConflict".equals(mce.getErrorCodeName()) || 
                    "IndexOptionsConflict".equals(mce.getErrorCodeName()) ||
                    mce.getCode() == 86 || mce.getCode() == 85) {
                    System.err.println("[WARN] Index conflict ignored for collection '" + collection + "': " + mce.getErrorMessage());
                    return;
                }
            }
            // If we didn't handle it, rethrow to keep the original behavior
            throw dae;
        } catch (Exception e) {
            // Last resort: if the message contains known index conflict errors, don't fail startup
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("IndexKeySpecsConflict") || 
                msg.contains("IndexOptionsConflict") ||
                msg.contains("code: 86") || 
                msg.contains("code: 85") ||
                msg.contains("Index already exists")) {
                System.err.println("[WARN] Index conflict ignored for collection '" + collection + "': " + msg);
                return;
            }
            throw e;
        }
    }
}