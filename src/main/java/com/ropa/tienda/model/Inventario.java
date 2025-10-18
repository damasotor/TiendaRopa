import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "articulos")
@CompoundIndexes({
        @CompoundIndex(name = "idx_inventario_sucursal_stock", def = "{'inventario.sucursal_id': 1, 'inventario.stock': 1}")
})
public class Articulo {

    @Id
    private ObjectId id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser no negativo")
    private Double precio;

    @NotBlank(message = "La categoría no puede estar vacía")
    private String categoria;

    private Map<String, Object> atributos; // Objeto flexible sin restricciones

    private List<@Pattern(regexp = "^https?://.*", message = "Debe ser una URL válida") String> imagenes = new ArrayList<>();

    private List<Inventario> inventario = new ArrayList<>();

    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDateTime creadoEn;

    @NotNull(message = "La fecha de actualización es obligatoria")
    private LocalDateTime actualizadoEn;

    // Clase interna para el inventario
    public static class Inventario {
        @NotNull(message = "El ID de la sucursal es obligatorio")
        private ObjectId sucursalId;

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock debe ser no negativo")
        private Integer stock;

        @NotNull(message = "La última actualización es obligatoria")
        private LocalDateTime ultimaActualizacion;

        // Constructor
        public Inventario() {}

        public Inventario(ObjectId sucursalId, Integer stock, LocalDateTime ultimaActualizacion) {
            this.sucursalId = sucursalId;
            this.stock = stock;
            this.ultimaActualizacion = ultimaActualizacion;
        }

        // Getters y Setters
        public ObjectId getSucursalId() {
            return sucursalId;
        }

        public void setSucursalId(ObjectId sucursalId) {
            this.sucursalId = sucursalId;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public LocalDateTime getUltimaActualizacion() {
            return ultimaActualizacion;
        }

        public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
            this.ultimaActualizacion = ultimaActualizacion;
        }
    }

    // Constructor vacío
    public Articulo() {}

    // Constructor con campos requeridos
    public Articulo(String nombre, Double precio, String categoria,
                    LocalDateTime creadoEn, LocalDateTime actualizadoEn) {
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Map<String, Object> getAtributos() {
        return atributos;
    }

    public void setAtributos(Map<String, Object> atributos) {
        this.atributos = atributos;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public List<Inventario> getInventario() {
        return inventario;
    }

    public void setInventario(List<Inventario> inventario) {
        this.inventario = inventario;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    // Método para agregar un registro de inventario
    public void agregarInventario(Inventario inventario) {
        this.inventario.add(inventario);
    }

    // Método para agregar una imagen
    public void agregarImagen(String imagenUrl) {
        if (imagenUrl != null && imagenUrl.matches("^https?://.*")) {
            this.imagenes.add(imagenUrl);
        } else {
            throw new IllegalArgumentException("La URL de la imagen no es válida");
        }
    }
}
