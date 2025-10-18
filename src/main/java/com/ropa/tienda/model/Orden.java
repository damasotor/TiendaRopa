package com.ropa.tienda.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ordenes")
public class Orden {

    @Id
    private ObjectId id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Indexed(name = "idx_usuario_id")
    private ObjectId usuarioId;

    @NotNull(message = "Los ítems son obligatorios")
    @jakarta.validation.constraints.Size(min = 1, message = "Debe haber al menos un ítem")
    private List<ItemOrden> items = new ArrayList<>();

    @NotNull(message = "El total es obligatorio")
    @Min(value = 0, message = "El total debe ser no negativo")
    private Double total;

    @Indexed(name = "idx_sucursal_id")
    private ObjectId sucursalId;

    private DireccionEnvio direccionEnvio;

    @NotNull(message = "El método de pago es obligatorio")
    @Pattern(regexp = "^(tarjeta|efectivo|transferencia)$", message = "Método de pago no válido")
    private String metodoPago;

    @NotNull(message = "El estado es obligatorio")
    @Pattern(regexp = "^(pendiente|procesando|enviado|entregado|cancelado)$", message = "Estado no válido")
    @Indexed(name = "idx_estado")
    private String estado;

    @NotNull(message = "La fecha de pedido es obligatoria")
    @Indexed(name = "idx_fecha_pedido", unique = false)
    private LocalDateTime fechaPedido;

    @NotNull(message = "La fecha de actualización es obligatoria")
    private LocalDateTime fechaActualizacion;

    // Clase interna para los ítems de la orden
    public static class ItemOrden {
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
        public ItemOrden() {}

        public ItemOrden(ObjectId articuloId, Integer cantidad, Double precioUnitario, Double subtotal) {
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

    // Clase interna para la dirección de envío
    public static class DireccionEnvio {
        @NotBlank(message = "La calle no puede estar vacía")
        private String calle;

        @NotBlank(message = "La ciudad no puede estar vacía")
        private String ciudad;

        @NotBlank(message = "El código postal no puede estar vacío")
        private String codigoPostal;

        // Constructor
        public DireccionEnvio() {}

        public DireccionEnvio(String calle, String ciudad, String codigoPostal) {
            this.calle = calle;
            this.ciudad = ciudad;
            this.codigoPostal = codigoPostal;
        }

        // Getters y Setters
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
    }

    // Constructor vacío
    public Orden() {}

    // Constructor con campos requeridos
    public Orden(ObjectId usuarioId, List<ItemOrden> items, Double total, String metodoPago,
                 String estado, LocalDateTime fechaPedido, LocalDateTime fechaActualizacion) {
        this.usuarioId = usuarioId;
        this.items = items;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemOrden> getItems() {
        return items;
    }

    public void setItems(List<ItemOrden> items) {
        this.items = items;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public ObjectId getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(ObjectId sucursalId) {
        this.sucursalId = sucursalId;
    }

    public DireccionEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(DireccionEnvio direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    // Método para agregar un ítem a la orden
    public void agregarItem(ItemOrden item) {
        this.items.add(item);
        actualizarTotal();
    }

    // Método para actualizar el total de la orden
    private void actualizarTotal() {
        this.total = items.stream()
                .mapToDouble(ItemOrden::getSubtotal)
                .sum();
    }
}