// Script de configuración para MongoDB Local
// Ejecutar en MongoDB Compass o MongoDB Shell

// 1. Usar la base de datos tienda_ropa
// Detectar el entorno: si existe la función use() (mongosh/mongo), usarla;
// si no, intentar seleccionar la BD con db.getSiblingDB (cuando db está definido);
// en otro caso lanzar un error indicando ejecutar el script en un shell de MongoDB.
// Script de configuración para MongoDB Local
// Ejecutar en MongoDB Compass o MongoDB Shell

// 1. Usar la base de datos tienda_ropa
// Detectar el entorno: si existe la función use() (mongosh/mongo), usarla;
// si no, intentar seleccionar la BD con db.getSiblingDB (cuando db está definido);
// en otro caso lanzar un error indicando ejecutar el script en un shell de MongoDB.
if (typeof use === 'function') {
  use('tiendaropa');
} else if (typeof db !== 'undefined' && typeof db.getSiblingDB === 'function') {
  db = db.getSiblingDB('tiendaropa');
} else {
  throw new Error("Este script debe ejecutarse en mongosh/mongo; si lo ejecutas desde Node.js usa el driver oficial de MongoDB y adapta el script.");
}

// Función para manejar la creación segura de colecciones
function crearColeccionSegura(nombreColeccion, opciones) {
  try {
    // Verificar si la colección ya existe usando getCollectionNames()
    const colecciones = db.getCollectionNames();
    if (colecciones.indexOf(nombreColeccion) !== -1) {
      print(`La colección '${nombreColeccion}' ya existe, saltando creación`);
      return false;
    } else {
      db.createCollection(nombreColeccion, opciones);
      print(`Colección '${nombreColeccion}' creada exitosamente`);
      return true;
    }
  } catch (error) {
    // Si hay error, intentar crear la colección directamente
    try {
      db.createCollection(nombreColeccion, opciones);
      print(`Colección '${nombreColeccion}' creada exitosamente`);
      return true;
    } catch (createError) {
      print(`Error al crear la colección '${nombreColeccion}': ${createError.message}`);
      return false;
    }
  }
}

// Función para limpiar índices conflictivos
function limpiarIndicesConflictivos() {
  print("Verificando y limpiando índices conflictivos...");
  
  const colecciones = ["productos", "carritos", "ordenes", "usuarios", "sucursales"];
  
  colecciones.forEach(coleccion => {
    try {
      // Verificar si la colección existe antes de intentar obtener índices
      const coleccionesExistentes = db.getCollectionNames();
      if (coleccionesExistentes.indexOf(coleccion) === -1) {
        // La colección no existe, no hay nada que limpiar
        return;
      }
      
      // Obtener índices existentes
      const indices = db[coleccion].getIndexes();
      let indicesEliminados = 0;
      
      // Eliminar índices automáticos que pueden causar conflictos
      indices.forEach(index => {
        if (index.name !== "_id_" && 
            (index.name.endsWith("_1") || 
             index.name.endsWith("_-1") || 
             index.name.includes("_1_"))) {
          try {
            db[coleccion].dropIndex(index.name);
            indicesEliminados++;
          } catch (dropError) {
            // Ignorar errores de índices que no existen
          }
        }
      });
      
      if (indicesEliminados > 0) {
        print(`  - Eliminados ${indicesEliminados} índices conflictivos de '${coleccion}'`);
      }
    } catch (error) {
      // Solo mostrar errores que no sean "namespace does not exist"
      if (!error.message.includes("ns does not exist")) {
        print(`  - Error procesando ${coleccion}: ${error.message}`);
      }
    }
  });
  
  print("Limpieza de índices completada");
}

// 2. Limpiar índices conflictivos antes de crear nuevos
limpiarIndicesConflictivos();

// 3. Crear colecciones con validación de esquemas

// Colección de productos
crearColeccionSegura("productos", {
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
          bsonType: ["double", "int"],
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
            properties: {
              sucursal_id: {
                bsonType: "string",
                description: "ID de la sucursal"
              },
              stock: {
                bsonType: ["int", "double"],
                minimum: 0,
                description: "Stock disponible"
              },
              stockMinimo: {
                bsonType: ["int", "double"],
                minimum: 0,
                description: "Stock mínimo antes de reabastecimiento"
              },
              stockMaximo: {
                bsonType: ["int", "double"],
                minimum: 0,
                description: "Stock máximo permitido"
              }
            }
          },
          description: "Inventario por sucursal"
        },
        stock: {
          bsonType: ["int", "double"],
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
crearColeccionSegura("carritos", {
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
crearColeccionSegura("ordenes", {
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
// Definir JSON Schema alineado al modelo Java: passwordHash (no "password") y rol como objeto con "nombre"
const schemaUsuarios = {
  $jsonSchema: {
    bsonType: "object",
    required: ["email", "passwordHash", "nombre"],
    properties: {
      email: {
        bsonType: "string",
        pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        description: "Email del usuario"
      },
      passwordHash: {
        bsonType: "string",
        description: "Contraseña hasheada (BCrypt)"
      },
      nombre: {
        bsonType: "string",
        description: "Nombre del usuario"
      },
      rol: {
        bsonType: "object",
        required: ["nombre"],
        properties: {
          nombre: {
            bsonType: "string",
            enum: ["ROLE_USER", "ROLE_ADMIN"],
            description: "Nombre del rol con prefijo ROLE_"
          }
        },
        description: "Rol del usuario (objeto)"
      },
      creadoEn: {
        bsonType: "date",
        description: "Fecha de creación"
      }
    }
  }
};

// Crear colección si no existe con el validador correcto
crearColeccionSegura("usuarios", { validator: schemaUsuarios });

// Asegurar (o actualizar) el validador de la colección usuarios vía collMod para entornos ya creados
try {
  db.runCommand({
    collMod: "usuarios",
    validator: schemaUsuarios,
    validationLevel: "moderate"
  });
  print("Validador de 'usuarios' actualizado (collMod)");
} catch (e) {
  // Silenciar errores si la operación no es soportada en la versión o ya está aplicado
  print("Aviso: No se pudo actualizar el validador de 'usuarios' via collMod: " + e.message);
}

// Colección de sucursales
crearColeccionSegura("sucursales", {
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

// 4. Crear índices para mejorar el rendimiento (con nombres específicos para evitar conflictos)

print("Creando índices...");

try {
  // Índices para productos - usando los mismos nombres que Spring Boot
  db.productos.createIndex({ "categoria": 1 }, { name: "idx_categoria" });
  db.productos.createIndex({ "precio": 1 }, { name: "idx_precio" });
  db.productos.createIndex({ "inventario.sucursal_id": 1, "inventario.stock": 1 }, { name: "idx_inventario_sucursal_stock" });
  db.productos.createIndex({ "atributos.color": 1 }, { name: "idx_atributos_color" });
  print("Índices de productos creados");
} catch (error) {
  print("Error al crear índices de productos: " + error.message);
}

try {
  // Índices para carritos - usando los mismos nombres que Spring Boot
  db.carritos.createIndex({ "visitanteId": 1 }, { name: "idx_visitante_id" });
  db.carritos.createIndex({ "ultimaActividad": 1 }, { expireAfterSeconds: 2592000, name: "idx_carrito_ttl" }); // TTL 30 días
  print("Índices de carritos creados");
} catch (error) {
  print("Error al crear índices de carritos: " + error.message);
}

try {
  // Índices para órdenes - usando los mismos nombres que Spring Boot
  db.ordenes.createIndex({ "usuarioId": 1 }, { name: "idx_orden_usuario_id" });
  db.ordenes.createIndex({ "estado": 1 }, { name: "idx_estado" });
  db.ordenes.createIndex({ "sucursalId": 1 }, { name: "idx_orden_sucursal_id" });
  print("Índices de órdenes creados");
} catch (error) {
  print("Error al crear índices de órdenes: " + error.message);
}

try {
  // Índices para usuarios - usando los mismos nombres que Spring Boot
  db.usuarios.createIndex({ "email": 1 }, { unique: true, name: "email_1" }); // Mantener nombre estándar para unique
  print("Índices de usuarios creados");
} catch (error) {
  print("Error al crear índices de usuarios: " + error.message);
}

try {
  // Índices para sucursales - usando los mismos nombres que Spring Boot
  db.sucursales.createIndex({ "nombre": 1 }, { name: "idx_sucursal_nombre" });
  print("Índices de sucursales creados");
} catch (error) {
  print("Error al crear índices de sucursales: " + error.message);
}

// 5. Insertar datos de sucursales básicas (solo si no existen)
try {
  // Verificar si ya existen sucursales
  if (db.sucursales.countDocuments() === 0) {
    db.sucursales.insertMany([
      {
        _id: ObjectId("507f1f77bcf86cd799439011"),
        nombre: "Centro",
        direccion: "18 de Julio 1234, Montevideo",
        telefono: "2908-1234",
        horario: "Lunes a Sábado 9:00-20:00",
        activa: true
      },
      {
        _id: ObjectId("507f1f77bcf86cd799439012"),
        nombre: "Punta Carretas",
        direccion: "Ellauri 350, Montevideo",
        telefono: "2908-5678",
        horario: "Lunes a Sábado 10:00-22:00",
        activa: true
      },
      {
        _id: ObjectId("507f1f77bcf86cd799439013"),
        nombre: "Maldonado",
        direccion: "Sarandí 123, Maldonado",
        telefono: "4222-9999",
        horario: "Lunes a Sábado 9:00-19:00",
        activa: true
      }
    ]);
    print("Sucursales insertadas correctamente");
  } else {
    print("Las sucursales ya existen, saltando inserción");
  }
} catch (error) {
  print("Error al insertar sucursales: " + error.message);
}

// Migraciones de compatibilidad: renombrar campo password -> passwordHash si existe
try {
  const resRename = db.usuarios.updateMany(
    { password: { $exists: true }, passwordHash: { $exists: false } },
    { $rename: { "password": "passwordHash" } }
  );
  if (resRename && resRename.modifiedCount) {
    print(`Usuarios migrados (password -> passwordHash): ${resRename.modifiedCount}`);
  }
} catch (e) {
  print("Aviso: No se pudo realizar migración de password->passwordHash: " + e.message);
}

// Migrar rol string -> objeto { nombre: "ROLE_*" }
try {
  // Caso común sin usar pipeline: valores USER/ADMIN
  const resAdmin = db.usuarios.updateMany(
    { rol: "ADMIN" },
    { $set: { rol: { nombre: "ROLE_ADMIN" } } }
  );
  const resUser = db.usuarios.updateMany(
    { rol: "USER" },
    { $set: { rol: { nombre: "ROLE_USER" } } }
  );
  // Intento genérico con pipeline update (MongoDB 4.2+)
  try {
    const resGeneric = db.usuarios.updateMany(
      { rol: { $type: "string" } },
      [
        {
          $set: {
            rol: {
              nombre: {
                $cond: [
                  { $regexMatch: { input: "$rol", regex: /^ROLE_/ } },
                  "$rol",
                  { $concat: ["ROLE_", "$rol"] }
                ]
              }
            }
          }
        }
      ]
    );
    if (resGeneric && resGeneric.modifiedCount) {
      print(`Usuarios migrados (rol string -> objeto): ${resGeneric.modifiedCount}`);
    } else if ((resAdmin && resAdmin.modifiedCount) || (resUser && resUser.modifiedCount)) {
      print(`Usuarios migrados (rol string -> objeto): ${(resAdmin.modifiedCount||0)+(resUser.modifiedCount||0)}`);
    }
  } catch (e2) {
    // Si el pipeline no es soportado, ya manejamos casos comunes arriba
  }
} catch (e) {
  print("Aviso: No se pudo realizar migración de rol string->objeto: " + e.message);
}

// 6. Crear usuario administrador por defecto (solo si no existe)
try {
  if (db.usuarios.countDocuments({ email: "admin@gmail.com" }) === 0) {
    db.usuarios.insertOne({
      email: "admin@gmail.com",
      passwordHash: "$2a$12$qYMdLfg7pfTDbTmXIQyppOayM.97GSSdV5.0HI8YpMrLwFwsCICs.", // password: admin123
      nombre: "Administrador",
      rol: { nombre: "ROLE_ADMIN" },
      creadoEn: new Date()
    });
    print("Usuario administrador creado");
  } else {
    // Si el usuario ya existe, actualizar la contraseña para asegurar que sea correcta
    db.usuarios.updateOne(
      { email: "admin@gmail.com" },
      { 
        $set: { 
          passwordHash: "$2a$12$qYMdLfg7pfTDbTmXIQyppOayM.97GSSdV5.0HI8YpMrLwFwsCICs.",
          rol: { nombre: "ROLE_ADMIN" }
        }
      }
    );
    print("El usuario administrador ya existe - contraseña actualizada");
  }
} catch (error) {
  print("Error al crear usuario administrador: " + error.message);
}

// 7. Crear usuario de prueba (solo si no existe)
try {
  if (db.usuarios.countDocuments({ email: "usuario@gmail.com" }) === 0) {
    db.usuarios.insertOne({
      email: "usuario@gmail.com",
      passwordHash: "$2a$12$Fnot8cK3Mr9cTQgMZNWCdu13LGD4y8lCFuMOgAa1uJ4XXiQmck0oW", // password: password
      nombre: "Usuario de Prueba",
      rol: { nombre: "ROLE_USER" },
      creadoEn: new Date()
    });
    print("Usuario de prueba creado");
  } else {
    // Si el usuario ya existe, actualizar la contraseña para asegurar que sea correcta
    db.usuarios.updateOne(
      { email: "usuario@gmail.com" },
      { 
        $set: { 
          passwordHash: "$2a$12$Fnot8cK3Mr9cTQgMZNWCdu13LGD4y8lCFuMOgAa1uJ4XXiQmck0oW",
          rol: { nombre: "ROLE_USER" }
        }
      }
    );
    print("El usuario de prueba ya existe - contraseña actualizada");
  }
} catch (error) {
  print("Error al crear usuario de prueba: " + error.message);
}

print("¡Configuración de MongoDB completada!");
print("Base de datos: tiendaropa");
print("Colecciones creadas: productos, carritos, ordenes, usuarios, sucursales");
print("Índices creados para optimización");
print("Datos iniciales insertados");

// Verificar que las contraseñas estén correctamente configuradas
print("\n=== VERIFICACIÓN DE USUARIOS ===");
const usuarios = db.usuarios.find({}, { email: 1, passwordHash: 1, rol: 1 }).toArray();
usuarios.forEach(usuario => {
  const passwordStatus = usuario.passwordHash && usuario.passwordHash.length > 0 ? "✓ OK" : "✗ VACÍA";
  const rolNombre = usuario.rol && usuario.rol.nombre ? usuario.rol.nombre : JSON.stringify(usuario.rol);
  print(`${usuario.email} (${rolNombre}): ${passwordStatus}`);
});

print("");
print("Credenciales de administrador:");
print("Email: admin@gmail.com");
print("Password: admin123");
print("Credenciales de usuario de prueba:");
print("Email: usuario@gmail.com");
print("Password: password");
