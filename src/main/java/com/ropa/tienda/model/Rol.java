package com.ropa.tienda.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles")
public class Rol {

    private String nombre;

    public Rol() {}

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
