package com.ropa.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class MongoConvertersConfig {

    // Conversores comentados para permitir que Rol se serialice como objeto
    // en lugar de string, cumpliendo con el schema de validación de MongoDB
    
    /*
    static class RolToStringConverter implements Converter<Rol, String> {
        @Override
        public String convert(Rol source) {
            return source == null ? null : source.getNombre();
        }
    }

    static class StringToRolConverter implements Converter<String, Rol> {
        @Override
        public Rol convert(String source) {
            return source == null ? null : new Rol(source);
        }
    }
    */

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(
                // Conversores removidos temporalmente
        ));
    }
}
