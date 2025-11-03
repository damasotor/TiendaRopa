package com.ropa.tienda.config;

import com.ropa.tienda.model.Rol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class MongoConvertersConfig {

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

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(
                new RolToStringConverter(),
                new StringToRolConverter()
        ));
    }
}
