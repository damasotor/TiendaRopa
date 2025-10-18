package com.ropa.tienda.payload;

public class RegistroDto {
    private String email;
    private String password;

    // Constructor vacío
    public RegistroDto() {}

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}