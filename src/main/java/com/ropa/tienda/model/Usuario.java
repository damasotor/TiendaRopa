package com.ropa.tienda.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

// Anotaciones de validación (javax.validation)
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "usuarios")
public class Usuario {

    // Manteniendo 'String id' si así lo requiere la capa de persistencia original
    @Id
    private String id;

    // --- Atributos de Seguridad y Requeridos ---

    @NotBlank(message = "El email no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Debe ser un email válido (ej., usuario@dominio.com)")
    private String email;

    // Se mantiene el hash de la contraseña
    @NotBlank(message = "El hash de la contraseña es obligatorio para la seguridad")
    private String passwordHash;

    // Se mantiene el objeto Rol original de la primera clase
    private Rol rol;

    // --- Atributos Extendidos y de Validación ---

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    // @Valid aplica la validación al contenido de la lista (clase Direccion)
    @Valid
    private List<Direccion> direcciones = new ArrayList<>();

    @NotNull(message = "La fecha de creación es obligatoria")
    @Field("creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now(); // Inicializar para simplificar

    // --- Clase Interna para Direccion ---

    // NOTA: Si usas Spring Data MongoDB, esta clase DEBE ser estática
    public static class Direccion {

        @NotBlank(message = "La calle no puede estar vacía")
        private String calle;

        @NotBlank(message = "La ciudad no puede estar vacía")
        private String ciudad;

        public Direccion() {}

        public Direccion(String calle, String ciudad) {
            this.calle = calle;
            this.ciudad = ciudad;
        }

        // Getters y Setters de Direccion
        public String getCalle() { return calle; }
        public void setCalle(String calle) { this.calle = calle; }
        public String getCiudad() { return ciudad; }
        public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    }

    // --- Constructores ---

    public Usuario() {}

    // Constructor para la creación inicial (con campos requeridos y seguridad)
    public Usuario(String email, String passwordHash, Rol rol, String nombre) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombre = nombre;
        this.creadoEn = LocalDateTime.now();
    }

    // --- Getters y Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Direccion> getDirecciones() { return direcciones; }
    public void setDirecciones(List<Direccion> direcciones) { this.direcciones = direcciones; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    // --- Metodo Auxiliar ---

    public void agregarDireccion(Direccion direccion) {
        if (this.direcciones == null) {
            this.direcciones = new ArrayList<>();
        }
        this.direcciones.add(direccion);
    }
}