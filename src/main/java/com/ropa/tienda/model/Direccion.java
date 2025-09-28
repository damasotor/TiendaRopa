package com.ropa.tienda.model;

// No se usa @Document ni @Id
public class Direccion {

    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String pais;
    private String referencia; // Ejemplo: "Dejar en portería"

    // Campo para identificar la dirección principal de envío, si es necesario
    private boolean esPrincipal;

    // --- Constructor (Opcional, pero útil) ---
    public Direccion() {}

    // --- Getters y Setters ---

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public boolean isEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }
}