package com.ropa.tienda.config;

import com.ropa.tienda.model.Producto;
import com.ropa.tienda.model.Sucursal;
import com.ropa.tienda.repository.ProductoRepository;
import com.ropa.tienda.repository.SucursalRepository;
//import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(2) // Se ejecuta después del DataLoader original
public class InventarioUpdater implements CommandLineRunner {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private SucursalRepository sucursalRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== ACTUALIZADOR DE INVENTARIO ===");
        
        // Crear sucursales si no existen
        crearSucursalesInicial();
        
        // Actualizar productos con inventario por sucursal
        actualizarInventarioProductos();
    }

    private void crearSucursalesInicial() {
        long countExistentes = sucursalRepository.count();
        
        if (countExistentes == 0) {
            System.out.println("No hay sucursales existentes, creando desde InventarioUpdater...");
            
            List<Sucursal> sucursales = new ArrayList<>();
            
            Sucursal centro = new Sucursal("Centro", "18 de Julio 1234", "Montevideo", LocalDateTime.now());
            centro.setHorarios("Lunes a Sábado 9:00-20:00");
            
            Sucursal puntaCarretas = new Sucursal("Punta Carretas", "Ellauri 350", "Montevideo", LocalDateTime.now());
            puntaCarretas.setHorarios("Lunes a Sábado 10:00-22:00");
            
            Sucursal maldonado = new Sucursal("Maldonado", "Sarandí 123", "Maldonado", LocalDateTime.now());
            maldonado.setHorarios("Lunes a Sábado 9:00-19:00");
            
            sucursales.add(centro);
            sucursales.add(puntaCarretas);
            sucursales.add(maldonado);
            
            sucursalRepository.saveAll(sucursales);
            System.out.println(">>> " + sucursales.size() + " sucursales creadas por InventarioUpdater!");
        } else {
            System.out.println("Las sucursales ya existen (" + countExistentes + " encontradas) - usando existentes.");
        }
    }

    private void actualizarInventarioProductos() {
        List<Producto> productos = productoRepository.findAll();
        List<Sucursal> sucursales = sucursalRepository.findAll();
        
        if (sucursales.size() < 3) {
            System.out.println("No hay suficientes sucursales para distribuir inventario.");
            return;
        }
        
        System.out.println("Verificando inventario de " + productos.size() + " productos...");
        
        int productosActualizados = 0;
        Random random = new Random();
        
        for (Producto producto : productos) {
            // Solo actualizar si no tiene inventario por sucursal
            if (producto.getInventario() == null || producto.getInventario().isEmpty()) {
                
                // Crear inventario por sucursal basado en el stock actual
                List<Producto.Inventario> inventarioPorSucursal = new ArrayList<>();
                int stockTotal = producto.getStock() != null ? producto.getStock() : 0;
                
                if (stockTotal > 0) {
                    // Distribuir el stock entre las 3 sucursales de manera inteligente
                    int stockCentro = Math.max(1, stockTotal * (30 + random.nextInt(20)) / 100); // 30-50%
                    int stockPuntaCarretas = Math.max(1, stockTotal * (25 + random.nextInt(15)) / 100); // 25-40%
                    int stockMaldonado = Math.max(0, stockTotal - stockCentro - stockPuntaCarretas); // El resto
                    
                    // Asegurar que no excedamos el stock total
                    if (stockCentro + stockPuntaCarretas + stockMaldonado > stockTotal) {
                        stockMaldonado = stockTotal - stockCentro - stockPuntaCarretas;
                    }
                    
                    // Crear inventario para Centro (principal)
                    Producto.Inventario invCentro = new Producto.Inventario(
                        sucursales.get(0).getId().toString(), 
                        stockCentro, 
                        LocalDateTime.now()
                    );
                    
                    // Crear inventario para Punta Carretas
                    Producto.Inventario invPuntaCarretas = new Producto.Inventario(
                        sucursales.get(1).getId().toString(), 
                        stockPuntaCarretas, 
                        LocalDateTime.now()
                    );
                    
                    // Crear inventario para Maldonado
                    Producto.Inventario invMaldonado = new Producto.Inventario(
                        sucursales.get(2).getId().toString(), 
                        stockMaldonado, 
                        LocalDateTime.now()
                    );
                    
                    inventarioPorSucursal.add(invCentro);
                    inventarioPorSucursal.add(invPuntaCarretas);
                    inventarioPorSucursal.add(invMaldonado);
                } else {
                    // Si no hay stock, crear inventario vacío
                    for (Sucursal sucursal : sucursales) {
                        Producto.Inventario invVacio = new Producto.Inventario(
                            sucursal.getId().toString(), 
                            0, 
                            LocalDateTime.now()
                        );
                        inventarioPorSucursal.add(invVacio);
                    }
                }
                
                // Actualizar el producto
                producto.setInventario(inventarioPorSucursal);
                producto.setActualizadoEn(LocalDateTime.now());
                
                productoRepository.save(producto);
                productosActualizados++;
            }
        }
        
        if (productosActualizados > 0) {
            System.out.println(">>> " + productosActualizados + " productos actualizados con inventario por sucursal!");
            
            // Mostrar estadísticas
            mostrarEstadisticasInventario();
        } else {
            System.out.println("Todos los productos ya tienen inventario configurado.");
        }
    }
    
    private void mostrarEstadisticasInventario() {
        List<Producto> productos = productoRepository.findAll();
        List<Sucursal> sucursales = sucursalRepository.findAll();
        
        System.out.println(">>> ESTADÍSTICAS DE INVENTARIO:");
        
            for (Sucursal sucursal : sucursales) {
                int stockTotal = 0;
                for (Producto producto : productos) {
                    if (producto.getInventario() != null) {
                        for (Producto.Inventario inv : producto.getInventario()) {
                            if (inv.getSucursalId().equals(sucursal.getId().toString())) {
                                stockTotal += inv.getStock();
                            }
                        }
                    }
                }
                System.out.println(">>> - " + sucursal.getNombre() + ": " + stockTotal + " unidades");
            }        // Stock total
        int stockGlobal = productos.stream().mapToInt(p -> p.getStock() != null ? p.getStock() : 0).sum();
        System.out.println(">>> - TOTAL GLOBAL: " + stockGlobal + " unidades");
    }
}