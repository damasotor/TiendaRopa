package com.ropa.tienda.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
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
        IndexDefinition inventarioIndex = new CompoundIndexDefinition(
            new Document("inventario.sucursalId", 1)
                .append("inventario.stock", 1)
        ).named("idx_inventario_sucursal_stock");
        
        mongoTemplate.indexOps("articulos").ensureIndex(inventarioIndex);

        // Índices para filtros comunes
        mongoTemplate.indexOps("articulos").ensureIndex(
            new Index().on("categoria", Sort.Direction.ASC).named("idx_categoria")
        );
        
        mongoTemplate.indexOps("articulos").ensureIndex(
            new Index().on("precio", Sort.Direction.ASC).named("idx_precio")
        );

        // Índices para atributos dinámicos más comunes
        mongoTemplate.indexOps("articulos").ensureIndex(
            new Index().on("atributos.color", Sort.Direction.ASC).named("idx_atributos_color")
        );
        
        mongoTemplate.indexOps("articulos").ensureIndex(
            new Index().on("atributos.talla", Sort.Direction.ASC).named("idx_atributos_talla")
        );
    }

    private void crearIndicesCarritos() {
        // Índice TTL para auto-limpieza de carritos - CU-002
        mongoTemplate.indexOps("carritos").ensureIndex(
            new Index().on("ultimaActividad", Sort.Direction.ASC)
                .expire(java.time.Duration.ofDays(30))
                .named("idx_carrito_ttl")
        );

        // Índice por visitante ID
        mongoTemplate.indexOps("carritos").ensureIndex(
            new Index().on("visitanteId", Sort.Direction.ASC).named("idx_visitante_id")
        );
    }

    private void crearIndicesOrdenes() {
        // Índice por usuario ID
        mongoTemplate.indexOps("ordenes").ensureIndex(
            new Index().on("usuarioId", Sort.Direction.ASC).named("idx_orden_usuario_id")
        );

        // Índice por fecha de pedido (descendente para consultas recientes primero)
        mongoTemplate.indexOps("ordenes").ensureIndex(
            new Index().on("fechaPedido", Sort.Direction.DESC).named("idx_fecha_pedido")
        );

        // Índice por estado
        mongoTemplate.indexOps("ordenes").ensureIndex(
            new Index().on("estado", Sort.Direction.ASC).named("idx_estado")
        );

        // Índice por sucursal
        mongoTemplate.indexOps("ordenes").ensureIndex(
            new Index().on("sucursalId", Sort.Direction.ASC).named("idx_orden_sucursal_id")
        );

        // Índice compuesto para consultas complejas (usuario + estado)
        IndexDefinition usuarioEstadoIndex = new CompoundIndexDefinition(
            new Document("usuarioId", 1).append("estado", 1)
        ).named("idx_usuario_estado");
        
        mongoTemplate.indexOps("ordenes").ensureIndex(usuarioEstadoIndex);
    }

    private void crearIndicesSucursales() {
        // Índice por ciudad
        mongoTemplate.indexOps("sucursales").ensureIndex(
            new Index().on("ciudad", Sort.Direction.ASC).named("idx_sucursal_ciudad")
        );

        // Índice por nombre
        mongoTemplate.indexOps("sucursales").ensureIndex(
            new Index().on("nombre", Sort.Direction.ASC).named("idx_sucursal_nombre")
        );
    }
}