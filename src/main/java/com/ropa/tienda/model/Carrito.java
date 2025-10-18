import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carritos")
public class Carrito {

    @Id
    private ObjectId id;

    @NotBlank(message = "El ID del visitante no puede estar vacío")
    private String visitanteId;

    @NotNull(message = "El campo registrado es obligatorio")
    private Boolean registrado;

    private List<ItemCarrito> items = new ArrayList<>();

    @NotNull(message = "El total es obligatorio")
    @Min(value = 0, message = "El total debe ser no negativo")
    private Double total;

    @NotNull(message = "La última actividad es obligatoria")
    @Indexed(expireAfterSeconds = 2592000) // TTL de 30 días
    private LocalDateTime ultimaActividad;

    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDateTime creadoEn;

    // Clase interna para los items del carrito
    public static class ItemCarrito {
        @NotNull(message = "El ID del artículo es obligatorio")
        private ObjectId articuloId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        @Min(value = 0, message = "El precio unitario debe ser no negativo")
        private Double precioUnitario;

        @NotNull(message = "El subtotal es obligatorio")
        @Min(value = 0, message = "El subtotal debe ser no negativo")
        private Double subtotal;

        // Constructor
        public ItemCarrito() {}

        public ItemCarrito(ObjectId articuloId, Integer cantidad, Double precioUnitario, Double subtotal) {
            this.articuloId = articuloId;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }

        // Getters y Setters
        public ObjectId getArticuloId() {
            return articuloId;
        }

        public void setArticuloId(ObjectId articuloId) {
            this.articuloId = articuloId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public Double getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(Double precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public Double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(Double subtotal) {
            this.subtotal = subtotal;
        }
    }

    // Constructor vacío
    public Carrito() {}

    // Constructor con campos requeridos
    public Carrito(String visitanteId, Boolean registrado, Double total,
                   LocalDateTime ultimaActividad, LocalDateTime creadoEn) {
        this.visitanteId = visitanteId;
        this.registrado = registrado;
        this.total = total;
        this.ultimaActividad = ultimaActividad;
        this.creadoEn = creadoEn;
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getVisitanteId() {
        return visitanteId;
    }

    public void setVisitanteId(String visitanteId) {
        this.visitanteId = visitanteId;
    }

    public Boolean getRegistrado() {
        return registrado;
    }

    public void setRegistrado(Boolean registrado) {
        this.registrado = registrado;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public LocalDateTime getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(LocalDateTime ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    // Método para agregar un item al carrito
    public void agregarItem(ItemCarrito item) {
        this.items.add(item);
        actualizarTotal();
    }

    // Método para actualizar el total del carrito
    private void actualizarTotal() {
        this.total = items.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
    }
}
