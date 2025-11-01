package com.ropa.tienda.config;

import com.ropa.tienda.model.*;
import com.ropa.tienda.repository.*;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder,
                     SucursalRepository sucursalRepository, ProductoRepository productoRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sucursalRepository = sucursalRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos datos si las colecciones están vacías
        if (userRepository.count() == 0) {
            crearUsuarios();
        }

        if (sucursalRepository.count() == 0) {
            crearSucursales();
        }

        if (productoRepository.count() == 0) {
            crearProductos();
        }
    }

    private void crearUsuarios() {
        // --- Creación de Usuario Administrador ---
        Usuario admin = new Usuario();
        admin.setEmail("admin@tienda.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRol(new Rol("ROLE_ADMIN"));
        admin.setNombre("Administrador Principal");

        userRepository.save(admin);
        System.out.println(">>> Usuario Administrador insertado: admin@tienda.com / admin123 (ROL: ADMIN)");

        // --- Creación de un Usuario Normal ---
        Usuario user = new Usuario();
        user.setEmail("user@tienda.com");
        user.setPasswordHash(passwordEncoder.encode("user123"));
        user.setRol(new Rol("ROLE_USER"));
        user.setNombre("Usuario de Prueba");

        userRepository.save(user);
        System.out.println(">>> Usuario Normal insertado: user@tienda.com / user123 (ROL: USER)");
    }

    private void crearSucursales() {
        // Crear sucursales según el diseño
        Sucursal sucursal1 = new Sucursal(
            "Sucursal Centro",
            "Av. 18 de Julio 1234",
            "Montevideo",
            LocalDateTime.now()
        );
        sucursal1.setHorarios("Lunes a Viernes: 9:00-18:00, Sábados: 9:00-15:00");

        Sucursal sucursal2 = new Sucursal(
            "Sucursal Punta Carretas",
            "Ellauri 350",
            "Montevideo",
            LocalDateTime.now()
        );
        sucursal2.setHorarios("Lunes a Domingo: 10:00-22:00");

        Sucursal sucursal3 = new Sucursal(
            "Sucursal Maldonado",
            "Av. Roosevelt 1500",
            "Maldonado",
            LocalDateTime.now()
        );
        sucursal3.setHorarios("Lunes a Sábado: 9:00-19:00");

        sucursalRepository.saveAll(List.of(sucursal1, sucursal2, sucursal3));
        System.out.println(">>> Sucursales creadas: Centro, Punta Carretas, Maldonado");
    }

    private void crearProductos() {
        // Obtener sucursales para el inventario
        List<Sucursal> sucursales = sucursalRepository.findAll();
        ObjectId sucursal1Id = sucursales.get(0).getId();
        ObjectId sucursal2Id = sucursales.get(1).getId();
        ObjectId sucursal3Id = sucursales.get(2).getId();

        // --- Producto 1: Camisa ---
        Producto camisa = new Producto();
        camisa.setNombre("Camisa Formal Azul");
        camisa.setDescripcion("Camisa formal de algodón 100%, perfecta para oficina");
        camisa.setPrecio(1500.0);
        camisa.setCategoria("camisas");
        camisa.setCreadoEn(LocalDateTime.now());
        camisa.setActualizadoEn(LocalDateTime.now());

        // Atributos dinámicos
        Map<String, Object> atributosCamisa = new HashMap<>();
        atributosCamisa.put("color", "azul");
        atributosCamisa.put("talla", "M");
        atributosCamisa.put("marca", "TiendaRopa");
        atributosCamisa.put("material", "algodón");
        camisa.setAtributos(atributosCamisa);

        // Inventario por sucursal
        camisa.setInventario(List.of(
            new Producto.Inventario(sucursal1Id, 15, LocalDateTime.now()),
            new Producto.Inventario(sucursal2Id, 8, LocalDateTime.now()),
            new Producto.Inventario(sucursal3Id, 3, LocalDateTime.now())
        ));

        // --- Producto 2: Pantalón ---
        Producto pantalon = new Producto();
        pantalon.setNombre("Pantalón Jeans Clásico");
        pantalon.setDescripcion("Pantalón jeans de corte clásico, cómodo y duradero");
        pantalon.setPrecio(2200.0);
        pantalon.setCategoria("pantalones");
        pantalon.setCreadoEn(LocalDateTime.now());
        pantalon.setActualizadoEn(LocalDateTime.now());

        Map<String, Object> atributosPantalon = new HashMap<>();
        atributosPantalon.put("color", "negro");
        atributosPantalon.put("talla", "32");
        atributosPantalon.put("marca", "TiendaRopa");
        atributosPantalon.put("tipo", "jeans");
        pantalon.setAtributos(atributosPantalon);

        pantalon.setInventario(List.of(
            new Producto.Inventario(sucursal1Id, 12, LocalDateTime.now()),
            new Producto.Inventario(sucursal2Id, 20, LocalDateTime.now()),
            new Producto.Inventario(sucursal3Id, 5, LocalDateTime.now())
        ));

        // --- Producto 3: Zapatos ---
        Producto zapatos = new Producto();
        zapatos.setNombre("Zapatos de Cuero Marrón");
        zapatos.setDescripcion("Zapatos formales de cuero genuino, ideales para eventos");
        zapatos.setPrecio(3500.0);
        zapatos.setCategoria("zapatos");
        zapatos.setCreadoEn(LocalDateTime.now());
        zapatos.setActualizadoEn(LocalDateTime.now());

        Map<String, Object> atributosZapatos = new HashMap<>();
        atributosZapatos.put("color", "marrón");
        atributosZapatos.put("talla", "42");
        atributosZapatos.put("marca", "TiendaRopa");
        atributosZapatos.put("material", "cuero");
        atributosZapatos.put("tipo", "formal");
        zapatos.setAtributos(atributosZapatos);

        zapatos.setInventario(List.of(
            new Producto.Inventario(sucursal1Id, 6, LocalDateTime.now()),
            new Producto.Inventario(sucursal2Id, 10, LocalDateTime.now()),
            new Producto.Inventario(sucursal3Id, 2, LocalDateTime.now())
        ));

        // --- Producto 4: Remera ---
        Producto remera = new Producto();
        remera.setNombre("Remera Casual Blanca");
        remera.setDescripcion("Remera de algodón para uso diario, cómoda y fresca");
        remera.setPrecio(800.0);
        remera.setCategoria("camisas");
        remera.setCreadoEn(LocalDateTime.now());
        remera.setActualizadoEn(LocalDateTime.now());

        Map<String, Object> atributosRemera = new HashMap<>();
        atributosRemera.put("color", "blanco");
        atributosRemera.put("talla", "L");
        atributosRemera.put("marca", "TiendaRopa");
        atributosRemera.put("material", "algodón");
        atributosRemera.put("tipo", "casual");
        remera.setAtributos(atributosRemera);

        remera.setInventario(List.of(
            new Producto.Inventario(sucursal1Id, 25, LocalDateTime.now()),
            new Producto.Inventario(sucursal2Id, 18, LocalDateTime.now()),
            new Producto.Inventario(sucursal3Id, 12, LocalDateTime.now())
        ));

        // --- Producto 5: Accesorio ---
        Producto cinturon = new Producto();
        cinturon.setNombre("Cinturón de Cuero Negro");
        cinturon.setDescripcion("Cinturón elegante de cuero genuino con hebilla metálica");
        cinturon.setPrecio(1200.0);
        cinturon.setCategoria("accesorios");
        cinturon.setCreadoEn(LocalDateTime.now());
        cinturon.setActualizadoEn(LocalDateTime.now());

        Map<String, Object> atributosCinturon = new HashMap<>();
        atributosCinturon.put("color", "negro");
        atributosCinturon.put("talla", "M");
        atributosCinturon.put("marca", "TiendaRopa");
        atributosCinturon.put("material", "cuero");
        cinturon.setAtributos(atributosCinturon);

        cinturon.setInventario(List.of(
            new Producto.Inventario(sucursal1Id, 8, LocalDateTime.now()),
            new Producto.Inventario(sucursal2Id, 12, LocalDateTime.now()),
            new Producto.Inventario(sucursal3Id, 4, LocalDateTime.now())
        ));

        // Guardar todos los productos
        productoRepository.saveAll(List.of(camisa, pantalon, zapatos, remera, cinturon));
        System.out.println(">>> Productos creados: Camisa, Pantalón, Zapatos, Remera, Cinturón");
        System.out.println(">>> Datos de prueba cargados correctamente");
    }
}
