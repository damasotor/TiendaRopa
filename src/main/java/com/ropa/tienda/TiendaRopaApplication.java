package com.ropa.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication es la anotación clave que inicia el autoconfigurador de Spring Boot
@SpringBootApplication
public class TiendaRopaApplication{

    public static void main(String[] args) {
        // Este método inicia el servidor web y toda la magia de Spring/MongoDB
        SpringApplication.run(TiendaRopaApplication.class, args);
    }
}