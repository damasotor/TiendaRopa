package com.ropa.tienda.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Producto {

    @Id
    private String id; // ID único generado por MongoDB

    private String nombre;
    private float precio;
    private String categoria;
    private String marca;
    private String descripcion;
    //private Object atributos;
    private String[] imagenes;
    private String[] inventario;
    private int stock;

    // Stock general (puede ser la suma de las variantes)
    private int stockTotal;

    // Aquí puedes añadir Variantes (tallas, colores) si decides modelarlas.
    // private List<Variante> variantes;

    // --- Constructor (Opcional, pero útil) ---
    public Producto() {}

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
    }
}