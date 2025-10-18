package com.ropa.tienda.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica solo a tus rutas de API
                .allowedOrigins("*") // Permite cualquier origen (para desarrollo)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite los métodos que usas
                .allowedHeaders("*"); // Permite cualquier encabezado
    }
}