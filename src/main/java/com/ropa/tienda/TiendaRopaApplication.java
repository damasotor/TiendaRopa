package com.ropa.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;


@SpringBootApplication
public class TiendaRopaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaRopaApplication.class, args);
    }

    // Muestra la base de datos conectada al iniciar (no imprime credenciales)
    @Bean
    public CommandLineRunner showConnectedDatabase(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                String dbName = mongoTemplate.getDb().getName();
                System.out.println(">>> Conectado a MongoDB - base de datos: " + dbName);
            } catch (Exception e) {
                System.err.println(">>> No fue posible obtener el nombre de la base de datos: " + e.getMessage());
            }
        };
    }

}
