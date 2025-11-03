package com.ropa.tienda.payload;

public class RegistroDto {
    private String email;
    private String password;
    private String nombre;
    private String rol;

    // Constructor vacío
    public RegistroDto() {}

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}