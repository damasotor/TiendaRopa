package com.ropa.tienda.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "sucursales")
public class Sucursal {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("id")
    private ObjectId id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "La dirección no puede estar vacía")
    private String direccion;

    @NotBlank(message = "La ciudad no puede estar vacía")
    private String ciudad;

    private String horarios;

    @NotNull(message = "La fecha de creación es obligatoria")
    @Field("creado_en")
    private LocalDateTime creadoEn;

    // Constructor vacío
    public Sucursal() {}

    // Constructor con campos requeridos
    public Sucursal(String nombre, String direccion, String ciudad, LocalDateTime creadoEn) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.creadoEn = creadoEn;
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }
    
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getHorarios() {
        return horarios;
    }

    public void setHorarios(String horarios) {
        this.horarios = horarios;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
