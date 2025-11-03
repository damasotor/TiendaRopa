package com.ropa.tienda.config;

import com.ropa.tienda.model.Producto;
import com.ropa.tienda.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// @Component // Desactivado para usar DataLoaderMejorado
public class DataLoader implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public DataLoader(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Siempre recrear productos para testing
        System.out.println("=== DATALOADER: Limpiando productos existentes ===");
        productoRepository.deleteAll();
        System.out.println("=== DATALOADER: Creando nuevos productos ===");
        crearProductos();
    }

    private void crearProductos() {
        List<Producto> productos = new ArrayList<>();

        // ==================== SUCURSAL CENTRO ====================
        // Camisas
        productos.add(crearProductoSimple("Camisa Formal Azul", "Camisa formal de algodón", 1500.0, "camisas",
            Map.of("color", "azul", "talla", "M"), "Centro", 15));
        productos.add(crearProductoSimple("Camisa Casual Blanca", "Camisa de manga larga", 1200.0, "camisas",
            Map.of("color", "blanco", "talla", "L"), "Centro", 12));
            
        // Pantalones
        productos.add(crearProductoSimple("Pantalon Jeans Negro", "Pantalon jeans clásico", 2200.0, "pantalones",
            Map.of("color", "negro", "talla", "32"), "Centro", 12));
        productos.add(crearProductoSimple("Pantalon Formal Gris", "Pantalon de vestir", 2800.0, "pantalones",
            Map.of("color", "gris", "talla", "34"), "Centro", 8));
            
        // Zapatos
        productos.add(crearProductoSimple("Zapatillas Deportivas", "Zapatillas cómodas para deporte", 2800.0, "zapatos",
            Map.of("color", "blanco", "talla", "42"), "Centro", 8));
        productos.add(crearProductoSimple("Zapatos Oxford Negro", "Zapatos elegantes de cuero", 4500.0, "zapatos",
            Map.of("color", "negro", "talla", "41"), "Centro", 6));
            
        // Accesorios Centro
        productos.add(crearProductoSimple("Cinturón de Cuero Negro", "Cinturón clásico de cuero genuino", 800.0, "accesorios",
            Map.of("color", "negro", "material", "cuero"), "Centro", 20));
        productos.add(crearProductoSimple("Reloj Deportivo", "Reloj digital resistente al agua", 1500.0, "accesorios",
            Map.of("color", "negro", "tipo", "deportivo"), "Centro", 10));

        // ==================== SUCURSAL PUNTA CARRETAS ====================
        // Camisas
        productos.add(crearProductoSimple("Camisa Casual Roja", "Camisa polo de algodón", 1200.0, "camisas",
            Map.of("color", "rojo", "talla", "L"), "Punta Carretas", 12));
        productos.add(crearProductoSimple("Camisa Estampada", "Camisa con diseño tropical", 1400.0, "camisas",
            Map.of("color", "multicolor", "talla", "M"), "Punta Carretas", 10));
            
        // Pantalones
        productos.add(crearProductoSimple("Pantalon Chino Beige", "Pantalon casual cómodo", 1800.0, "pantalones",
            Map.of("color", "beige", "talla", "34"), "Punta Carretas", 10));
        productos.add(crearProductoSimple("Short de Verano Azul", "Short de algodón ligero", 900.0, "pantalones",
            Map.of("color", "azul", "talla", "M"), "Punta Carretas", 18));
            
        // Zapatos
        productos.add(crearProductoSimple("Mocasines Marrones", "Zapatos casuales de cuero", 3200.0, "zapatos",
            Map.of("color", "marron", "talla", "41"), "Punta Carretas", 6));
        productos.add(crearProductoSimple("Sandalias de Verano", "Sandalias cómodas para playa", 1200.0, "zapatos",
            Map.of("color", "beige", "talla", "39"), "Punta Carretas", 15));
            
        // Accesorios Punta Carretas
        productos.add(crearProductoSimple("Gafas de Sol Aviador", "Gafas estilo aviador con UV", 2200.0, "accesorios",
            Map.of("color", "dorado", "tipo", "aviador"), "Punta Carretas", 12));
        productos.add(crearProductoSimple("Cartera de Cuero", "Billetera con múltiples compartimentos", 1400.0, "accesorios",
            Map.of("color", "marron", "material", "cuero"), "Punta Carretas", 15));
        productos.add(crearProductoSimple("Sombrero Panamá", "Sombrero clásico de fibra natural", 1800.0, "accesorios",
            Map.of("color", "beige", "tipo", "panama"), "Punta Carretas", 8));

        // ==================== SUCURSAL MALDONADO ====================
        // Camisas
        productos.add(crearProductoSimple("Camisa Verde Militar", "Camisa estilo militar resistente", 1350.0, "camisas",
            Map.of("color", "verde", "talla", "M"), "Maldonado", 10));
        productos.add(crearProductoSimple("Camisa de Lino Blanca", "Camisa fresca de lino para verano", 1600.0, "camisas",
            Map.of("color", "blanco", "talla", "L"), "Maldonado", 8));
            
        // Pantalones
        productos.add(crearProductoSimple("Pantalon Cargo Verde", "Pantalon con múltiples bolsillos", 2000.0, "pantalones",
            Map.of("color", "verde", "talla", "34"), "Maldonado", 7));
        productos.add(crearProductoSimple("Bermudas de Playa", "Bermudas de secado rápido", 1100.0, "pantalones",
            Map.of("color", "azul", "talla", "L"), "Maldonado", 12));
            
        // Zapatos
        productos.add(crearProductoSimple("Zapatos Negros Ejecutivos", "Zapatos elegantes de oficina", 4200.0, "zapatos",
            Map.of("color", "negro", "talla", "43"), "Maldonado", 5));
        productos.add(crearProductoSimple("Botas de Montaña", "Botas resistentes para trekking", 5500.0, "zapatos",
            Map.of("color", "marron", "talla", "42"), "Maldonado", 4));
            
        // Accesorios Maldonado
        productos.add(crearProductoSimple("Mochila de Cuero", "Mochila vintage de cuero genuino", 3500.0, "accesorios",
            Map.of("color", "marron", "material", "cuero"), "Maldonado", 6));
        productos.add(crearProductoSimple("Gorra de Béisbol", "Gorra ajustable con logo bordado", 650.0, "accesorios",
            Map.of("color", "azul", "tipo", "deportiva"), "Maldonado", 25));
        productos.add(crearProductoSimple("Bufanda de Lana", "Bufanda tejida para clima frío", 800.0, "accesorios",
            Map.of("color", "gris", "material", "lana"), "Maldonado", 12));
        productos.add(crearProductoSimple("Lentes de Lectura", "Lentes con armazón clásico", 1500.0, "accesorios",
            Map.of("color", "negro", "tipo", "lectura"), "Maldonado", 8));

        productoRepository.saveAll(productos);
        System.out.println(">>> " + productos.size() + " productos creados exitosamente!");
        System.out.println(">>> Distribución por sucursal:");
        System.out.println(">>> - Centro: 8 productos (camisas, pantalones, zapatos, accesorios)");
        System.out.println(">>> - Punta Carretas: 9 productos (camisas, pantalones, zapatos, accesorios)");
        System.out.println(">>> - Maldonado: 10 productos (camisas, pantalones, zapatos, accesorios)");
        System.out.println(">>> Categorías disponibles: camisas, pantalones, zapatos, accesorios");
    }

    private Producto crearProductoSimple(String nombre, String descripcion, double precio, String categoria,
                                       Map<String, Object> atributos, String sucursal, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setAtributos(atributos);
        producto.setSucursal(sucursal);
        producto.setStock(stock);
        producto.setCreadoEn(LocalDateTime.now());
        producto.setActualizadoEn(LocalDateTime.now());
        return producto;
    }
}
