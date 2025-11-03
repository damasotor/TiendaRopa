// Script de configuración para MongoDB Local
// Ejecutar en MongoDB Compass o MongoDB Shell

// 1. Usar la base de datos tienda_ropa
use tienda_ropa

// 2. Crear colecciones con validación de esquemas

// Colección de productos
db.createCollection("productos", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "precio", "categoria"],
      properties: {
        nombre: {
          bsonType: "string",
          description: "Nombre del producto"
        },
        descripcion: {
          bsonType: "string",
          description: "Descripción del producto"
        },
        precio: {
          bsonType: "double",
          minimum: 0,
          description: "Precio del producto"
        },
        categoria: {
          bsonType: "string",
          enum: ["camisas", "pantalones", "zapatos", "accesorios"],
          description: "Categoría del producto"
        },
        atributos: {
          bsonType: "object",
          description: "Atributos del producto (color, talla, etc.)"
        },
        imagenes: {
          bsonType: "array",
          items: {
            bsonType: "string"
          },
          description: "URLs de imágenes del producto"
        },
        inventario: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["sucursalId", "stock"],
            properties: {
              sucursalId: {
                bsonType: "string",
                description: "ID de la sucursal"
              },
              stock: {
                bsonType: "int",
                minimum: 0,
                description: "Stock disponible"
              },
              stockMinimo: {
                bsonType: "int",
                minimum: 0,
                description: "Stock mínimo antes de reabastecimiento"
              },
              stockMaximo: {
                bsonType: "int",
                minimum: 0,
                description: "Stock máximo permitido"
              }
            }
          },
          description: "Inventario por sucursal"
        },
        stock: {
          bsonType: "int",
          minimum: 0,
          description: "Stock total simplificado"
        },
        sucursal: {
          bsonType: "string",
          description: "Sucursal principal (compatibilidad)"
        },
        creadoEn: {
          bsonType: "date",
          description: "Fecha de creación"
        },
        actualizadoEn: {
          bsonType: "date",
          description: "Fecha de última actualización"
        }
      }
    }
  }
});

// Colección de carritos
db.createCollection("carritos", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["visitanteId", "registrado", "total", "ultimaActividad"],
      properties: {
        visitanteId: {
          bsonType: "string",
          description: "ID del visitante"
        },
        usuarioEmail: {
          bsonType: "string",
          description: "Email del usuario autenticado (opcional)"
        },
        registrado: {
          bsonType: "bool",
          description: "Si el usuario está registrado"
        },
        items: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["articuloId", "cantidad", "precioUnitario", "subtotal"],
            properties: {
              articuloId: {
                bsonType: "string",
                description: "ID del producto"
              },
              cantidad: {
                bsonType: "int",
                minimum: 1,
                description: "Cantidad del producto"
              },
              precioUnitario: {
                bsonType: "double",
                minimum: 0,
                description: "Precio unitario"
              },
              subtotal: {
                bsonType: "double",
                minimum: 0,
                description: "Subtotal del item"
              }
            }
          },
          description: "Items en el carrito"
        },
        total: {
          bsonType: "double",
          minimum: 0,
          description: "Total del carrito"
        },
        ultimaActividad: {
          bsonType: "date",
          description: "Última actividad en el carrito"
        },
        creadoEn: {
          bsonType: "date",
          description: "Fecha de creación"
        }
      }
    }
  }
});

// Colección de órdenes
db.createCollection("ordenes", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["total", "metodoPago", "estado", "fechaPedido"],
      properties: {
        visitanteId: {
          bsonType: "string",
          description: "ID del visitante"
        },
        usuarioEmail: {
          bsonType: "string",
          description: "Email del usuario autenticado (opcional)"
        },
        usuarioId: {
          bsonType: "objectId",
          description: "ID del usuario (opcional)"
        },
        items: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["articuloId", "cantidad", "precioUnitario", "subtotal"],
            properties: {
              articuloId: {
                bsonType: "string",
                description: "ID del producto"
              },
              cantidad: {
                bsonType: "int",
                minimum: 1,
                description: "Cantidad del producto"
              },
              precioUnitario: {
                bsonType: "double",
                minimum: 0,
                description: "Precio unitario"
              },
              subtotal: {
                bsonType: "double",
                minimum: 0,
                description: "Subtotal del item"
              }
            }
          },
          description: "Items de la orden"
        },
        total: {
          bsonType: "double",
          minimum: 0,
          description: "Total de la orden"
        },
        metodoPago: {
          bsonType: "string",
          enum: ["efectivo", "tarjeta", "transferencia"],
          description: "Método de pago"
        },
        estado: {
          bsonType: "string",
          enum: ["pendiente", "confirmada", "enviada", "entregada", "cancelada"],
          description: "Estado de la orden"
        },
        sucursalId: {
          bsonType: "objectId",
          description: "ID de la sucursal (si es retiro en tienda)"
        },
        direccionEnvio: {
          bsonType: "object",
          properties: {
            calle: { bsonType: "string" },
            ciudad: { bsonType: "string" },
            codigoPostal: { bsonType: "string" },
            pais: { bsonType: "string" }
          },
          description: "Dirección de envío (si es envío a domicilio)"
        },
        fechaPedido: {
          bsonType: "date",
          description: "Fecha del pedido"
        },
        fechaActualizacion: {
          bsonType: "date",
          description: "Fecha de última actualización"
        }
      }
    }
  }
});

// Colección de usuarios
db.createCollection("usuarios", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["email", "password", "nombre"],
      properties: {
        email: {
          bsonType: "string",
          pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
          description: "Email del usuario"
        },
        password: {
          bsonType: "string",
          description: "Contraseña hasheada"
        },
        nombre: {
          bsonType: "string",
          description: "Nombre del usuario"
        },
        rol: {
          bsonType: "string",
          enum: ["USER", "ADMIN"],
          description: "Rol del usuario"
        },
        creadoEn: {
          bsonType: "date",
          description: "Fecha de creación"
        }
      }
    }
  }
});

// Colección de sucursales
db.createCollection("sucursales", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "direccion"],
      properties: {
        nombre: {
          bsonType: "string",
          description: "Nombre de la sucursal"
        },
        direccion: {
          bsonType: "string",
          description: "Dirección de la sucursal"
        },
        telefono: {
          bsonType: "string",
          description: "Teléfono de la sucursal"
        },
        horario: {
          bsonType: "string",
          description: "Horario de atención"
        },
        activa: {
          bsonType: "bool",
          description: "Si la sucursal está activa"
        }
      }
    }
  }
});

// 3. Crear índices para mejorar el rendimiento

// Índices para productos
db.productos.createIndex({ "categoria": 1 });
db.productos.createIndex({ "precio": 1 });
db.productos.createIndex({ "stock": 1 });
db.productos.createIndex({ "inventario.sucursalId": 1, "inventario.stock": 1 });
db.productos.createIndex({ "atributos.color": 1 });
db.productos.createIndex({ "atributos.talla": 1 });

// Índices para carritos
db.carritos.createIndex({ "visitanteId": 1 }, { unique: true });
db.carritos.createIndex({ "usuarioEmail": 1 });
db.carritos.createIndex({ "ultimaActividad": 1 }, { expireAfterSeconds: 2592000 }); // TTL 30 días

// Índices para órdenes
db.ordenes.createIndex({ "visitanteId": 1 });
db.ordenes.createIndex({ "usuarioEmail": 1 });
db.ordenes.createIndex({ "usuarioId": 1 });
db.ordenes.createIndex({ "estado": 1 });
db.ordenes.createIndex({ "fechaPedido": -1 });

// Índices para usuarios
db.usuarios.createIndex({ "email": 1 }, { unique: true });

// 4. Insertar datos de sucursales básicas
db.sucursales.insertMany([
  {
    _id: ObjectId("507f1f77bcf86cd799439011"),
    nombre: "Centro",
    direccion: "18 de Julio 1234, Montevideo",
    telefono: "+598 2901 1234",
    horario: "Lunes a Sábado 9:00-20:00",
    activa: true
  },
  {
    _id: ObjectId("507f1f77bcf86cd799439012"),
    nombre: "Punta Carretas",
    direccion: "Ellauri 350, Montevideo",
    telefono: "+598 2711 5678",
    horario: "Lunes a Sábado 10:00-22:00",
    activa: true
  },
  {
    _id: ObjectId("507f1f77bcf86cd799439013"),
    nombre: "Maldonado",
    direccion: "Sarandí 123, Maldonado",
    telefono: "+598 4222 9876",
    horario: "Lunes a Sábado 9:00-19:00",
    activa: true
  }
]);

// 5. Crear usuario administrador por defecto
db.usuarios.insertOne({
  email: "admin@tiendaropa.com",
  password: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqWF.hUMYIU8H8J4.Y8K/QS", // password: admin123
  nombre: "Administrador",
  rol: "ADMIN",
  creadoEn: new Date()
});

print("¡Configuración de MongoDB completada!");
print("Base de datos: tienda_ropa");
print("Colecciones creadas: productos, carritos, ordenes, usuarios, sucursales");
print("Índices creados para optimización");
print("Datos iniciales insertados");
print("");
print("Credenciales de administrador:");
print("Email: admin@tiendaropa.com");
print("Password: admin123");