## 1. Resumen ejecutivo

TiendaRopa es una aplicación de ejemplo que implementa una tienda online simplificada. Consta de tres capas:

- Frontend: páginas estáticas (HTML + JavaScript) que consumen la API.
- Backend: una API REST escrita en Java con Spring Boot que implementa la lógica de negocio, autenticación y persistencia.
- Base de datos: MongoDB, que guarda productos, usuarios, carritos, órdenes y sucursales.

Objetivos del proyecto:
- Practicar modelado documental en MongoDB.
- Implementar filtros complejos y agregaciones usando Aggregation Framework.
- Proteger endpoints con autenticación y autorización (JWT + Spring Security).
- Documentar y justificar las decisiones de diseño.

## 2. Conceptos básicos (explicados para un junior)

2.1 ¿Qué es una API REST?
- API (Application Programming Interface) permite que distintas piezas de software se comuniquen.
- REST (Representational State Transfer) es un estilo arquitectónico para APIs basado en recursos (p. ej. /api/productos, /api/auth). Usa HTTP (GET, POST, PUT, DELETE) y suele intercambiar datos en formato JSON.

En este proyecto la API REST expone endpoints como `/api/auth/register` (registrar usuario), `/api/productos` (listar productos), `/api/ordenes` (crear órdenes), etc.

2.2 ¿Qué es Spring Boot?
- Spring Boot es un framework para Java que facilita crear aplicaciones web y microservicios. Viene con configuración por defecto, servidor embebido (Tomcat), integración con bases de datos y herramientas para crear endpoints REST de forma rápida.

En el proyecto usamos Spring Boot porque permite:
- Crear controladores REST con anotaciones (`@RestController`, `@GetMapping`, etc.).
- Integrarse con Spring Data para persistencia en MongoDB.
- Integrar seguridad con Spring Security (filtros, configuración de rutas protegidas).

2.3 ¿Qué es MongoDB?
- MongoDB es una base de datos NoSQL orientada a documentos. En lugar de tablas y filas, guarda documentos JSON (en realidad BSON) dentro de colecciones.
- Ventajas: esquema flexible, ideal para datos con atributos variables (p. ej. productos con distintos atributos), y buen soporte para operaciones agregadas.

Conceptos clave:
- Documento: equivalente a una fila en RDBMS, por ejemplo un producto con campos `nombre`, `precio`, `inventario`.
- Colección: equivalente a una tabla (p. ej. `productos`).
- BSON: formato binario similar a JSON que MongoDB usa internamente.

2.4 ¿Qué es JSON Schema en MongoDB?
- JSON Schema permite definir reglas de validación para documentos en una colección (p. ej., campo `email` debe ser string y coincidir con un patrón). Si un documento no cumple el schema el servidor puede rechazar la inserción o actualización.

En este proyecto usamos JSON Schema para asegurar que `usuarios` tiene `email`, `passwordHash` y `nombre`, y que `rol` sea un objeto con `nombre`.

2.5 ¿Qué es el Aggregation Framework?
- Es el motor de MongoDB para procesar y transformar conjuntos de documentos. Funciona como una pipeline (tubería) donde cada etapa ($match, $project, $group, $sort, $unwind, $lookup, etc.) transforma la salida para la siguiente.
- Se usa para obtener resúmenes, estadísticas, hacer joins mediante `$lookup`, descomponer arrays con `$unwind`, agrupar con `$group` y mucho más.

Ejemplo sencillo: filtrar productos por categoría y obtener el precio mínimo por producto.

2.6 ¿Qué es JWT (JSON Web Token)?
- JWT es un formato para tokens de autenticación. Contiene tres partes: header, payload y firma. Se usa para que el servidor genere un token al loguearte y luego el cliente lo envía en cada request (Authorization: Bearer <token>) para demostrar identidad.

Ventajas: stateless (no necesitas guardar sesiones en el servidor), portable y ampliamente soportado.

2.7 ¿Qué es BCrypt?
- BCrypt es un algoritmo de hashing para contraseñas. Es lento deliberadamente para dificultar ataques de fuerza bruta. En lugar de guardar la contraseña en texto plano, guardamos su hash con BCrypt.

2.8 ¿Qué es Maven?
- Maven es la herramienta de construcción y gestión de dependencias para proyectos Java. Define dependencias y plugins en `pom.xml`.

## 3. Estructura del proyecto (qué hace cada carpeta/archivo)

- `src/main/java/com/ropa/tienda/controller/` — Controladores que exponen los endpoints REST. Ej.: `AuthController`, `ProductoController`, `OrdenController`.
- `src/main/java/com/ropa/tienda/service/` — (si aplica) lógica de negocio separada de los controladores.
- `src/main/java/com/ropa/tienda/repository/` — Interfaces que extienden `MongoRepository` para CRUD básico.
- `src/main/java/com/ropa/tienda/model/` — Clases que representan documentos en MongoDB (`Producto`, `Usuario`, `Orden`, `Rol`).
- `src/main/java/com/ropa/tienda/config/` — Configuraciones: índices (`MongoIndexConfig`), seguridad (`SecurityConfig`), conversores (`MongoConvertersConfig`).
- `src/main/resources/static/` — Páginas estáticas (frontend).
- `scripts/` — Scripts para preparar MongoDB localmente (`configurar-mongodb-local.js`, migraciones).

## 4. Diseño de datos (modelado)

4.1 Producto
- Campos principales: `nombre`, `descripcion`, `precio`, `categoria`, `atributos` (mapa flexible), `imagenes` (array), `inventario` (lista de subdocumentos por sucursal).
- Ventaja: al embedir `inventario` dentro del producto evitamos joins al mostrar producto y su stock por sucursal.

4.2 Usuario
- Campos: `email`, `passwordHash`, `nombre`, `rol` (objeto con `nombre`), `direcciones`.
- Importante: `rol` se persiste como objeto `{ nombre: 'ROLE_USER' }` para alinearse con el validador JSON Schema y las expectativas de Spring Security.

4.3 Orden
- Contiene `items`, `total`, `metodoPago`, `estado`, `fechaPedido`, y opcionalmente `direccionEnvio` o `sucursalId` si se retira en tienda.
- Atención: `direccionEnvio` contiene datos sensibles y en el código se marcó como `@JsonProperty(access = WRITE_ONLY)` para que no se exponga en las respuestas.

## 5. Índices — qué son y por qué los usamos

¿Qué es un índice?
- Un índice es una estructura auxiliar que permite encontrar documentos más rápido, similar al índice de un libro.
- Mejora lecturas (consultas) pero penaliza escrituras (inserciones/actualizaciones) y ocupa espacio en disco.

Índices definidos y su propósito (explicado sencillo):
- `idx_categoria` ({ categoria: 1 }): acelera `find({ categoria: 'camisas' })`.
- `idx_precio` ({ precio: 1 }): ayuda ordenamientos y consultas por rango de precio.
- `idx_atributos_color` y `idx_atributos_talla`: para facetas de color/talla en la UI.
- `idx_inventario_sucursal_stock` ({ 'inventario.sucursal_id': 1, 'inventario.stock': 1 }): índice multikey (porque `inventario` es un array); acelera búsquedas de productos con stock>0 en una sucursal concreta.
- TTL (`idx_carrito_ttl`): índice que borra documentos automáticamente después de n segundos (útil para carritos inactivos).

Cómo comprobar si un índice se usa:
- Ejecutar `db.<coleccion>.aggregate([{ $indexStats: {} }])` y revisar `accesses.ops` para cada índice.

## 6. Seguridad — pasos y por qué

6.1 Almacenamiento de contraseñas
- Nunca guardar contraseñas en texto plano.
- Se usa BCrypt: al registrar, aplicamos `passwordEncoder.encode(password)` y guardamos ese hash.

6.2 Autenticación con JWT
- En el login validamos credenciales y devolvemos un JWT firmado. El frontend guarda el token y lo envía en `Authorization: Bearer <token>`.
- En cada request protegido, el `JwtAuthenticationFilter` extrae el token, lo valida y establece el contexto de seguridad.

6.3 Autorización
- Usamos anotaciones `@PreAuthorize("hasRole('ADMIN')")` para endpoints que solo admins deben ejecutar.

## 7. Ejemplos de endpoints y cómo usarlos (ejemplos prácticos)

1) Registro de usuario (POST /api/auth/register)
- Payload de ejemplo:

```json
{
  "email": "usuario@example.com",
  "password": "Secreto123!",
  "nombre": "Usuario",
  "rol": "ROLE_USER"
}
```

- Qué hace el backend: valida que no exista el email, hace `passwordEncoder.encode()`, rellena `nombre` si falta usando el email, y guarda `Usuario` con `rol` embebido.

2) Login (POST /api/auth/login)
- Payload: `{ "email": "usuario@example.com", "password": "Secreto123!" }`
- Respuesta: `{ "token": "eyJ...", "rol": "ROLE_USER" }`.

3) Listar productos con filtro (GET /api/productos?categoria=camisas)
- El controller construye los criterios y usa una pipeline de aggregación para combinar `$match`, `$unwind` y agrupaciones necesarias.

4) Crear orden (POST /api/ordenes)
- El controller crea la orden a partir del carrito, verifica stock y reduce inventario por sucursal.

## 8. Aggregation Framework — explicación paso a paso

La pipeline se compone de etapas. Cada etapa toma los documentos de entrada, los transforma, y pasa la salida a la siguiente etapa. Las etapas más comunes:

- `$match`: filtra documentos (equivalente a WHERE).
- `$project`: incluye/excluye o transforma campos (equivalente a SELECT con transformación).
- `$unwind`: descompone un array en múltiples documentos (uno por cada elemento del array).
- `$group`: agrupa y permite agregaciones (sum, avg, min, max, push, addToSet).
- `$sort`, `$limit`, `$skip`: ordenamiento y paginación.
- `$lookup`: hace joins con otra colección.

Ejemplo explicado (pipeline para productos con stock>0 y precio mínimo):

```javascript
db.productos.aggregate([
  { $match: { categoria: 'camisas' } },               // 1. filtramos por categoría
  { $unwind: '$inventario' },                         // 2. convertimos cada inventario en un documento separado
  { $match: { 'inventario.stock': { $gt: 0 } } },     // 3. filtramos solo inventario con stock > 0
  { $group: {                                        // 4. agrupamos por producto
      _id: '$_id',
      nombre: { $first: '$nombre' },
      precioMin: { $min: '$precio' },
      inventario: { $push: '$inventario' }
  }},
  { $sort: { precioMin: 1 } },                       // 5. ordenamos por precio mínimo asc
  { $limit: 50 }                                     // 6. limitamos el resultado
])
```

Buenas prácticas:
- aplicar `$match` lo antes posible para reducir volumen de datos.
- usar `$project` para limitar campos si no necesitas todo el documento.
- utilizar `allowDiskUse: true` para pipelines que consumen mucha memoria.

## 9. Migraciones y consistencia

Problema real resuelto en el proyecto:
- Teníamos conversores que persistían `rol` como string ("ROLE_ADMIN") y el validador JSON Schema exigía un objeto `{ nombre: "ROLE_ADMIN" }`. Esto provocó errores de validación al escribir.

Solución:
- Eliminamos esos conversores o los reescribimos para serializar `Rol` como documento.
- Escribimos un script de migración (`scripts/configurar-mongodb-local.js` incluye migraciones) que convierte documentos existentes con `rol` string a `{ nombre: <string> }`.

Ejemplo de migración (pipeline update):

```javascript
db.usuarios.updateMany(
  { rol: { $type: 'string' } },
  [ { $set: { rol: { nombre: { $cond: [ { $regexMatch: { input: '$rol', regex: /^ROLE_/ } }, '$rol', { $concat: ['ROLE_', '$rol'] } ] } } } ]
)
```

## 10. Pruebas y verificación (cómo comprobar que todo funciona)

Pasos manuales:
- Iniciar MongoDB.
- Ejecutar `scripts/configurar-mongodb-local.js` para crear datos de ejemplo.
- Ejecutar `mvn -DskipTests compile` y `mvn spring-boot:run`.
- Usar Postman o fetch desde las páginas estáticas para probar registro/login/filtrado/checkout.

Recomendación para tests automáticos:
- Añadir pruebas de integración con Spring Boot Test y Flapdoodle (Mongo embebido) para:
  - registrar y autenticar usuarios,
  - insertar productos y asegurar que las aggregations devuelven resultados esperados,
  - probar endpoints protegidos con JWT.

## 11. Troubleshooting (problemas comunes y cómo resolverlos)

- Error 121 (WriteError Document failed validation): revisar el validador de la colección (`db.getCollectionInfos({name:'usuarios'})[0].options.validator`) y comparar con la estructura que la aplicación está enviando.
- Token JWT inválido: verificar la clave secreta/configuración de `JwtTokenProvider` y la hora del sistema.
- Problemas de índice: usar `db.collection.aggregate([{ $indexStats: {} }])` y `explain('executionStats')` para identificar índices no usados o queries lentas.

## 12. Limitaciones y mejoras futuras (más detalle)

- Tests y CI: agregar pipeline CI para ejecutar pruebas y validar migraciones antes de merge.
- DTOs y mappers: separar las entidades de persistencia de los objetos de respuesta (DTO) para mayor control y seguridad.
- Migraciones robustas: usar Mongock o similar para versionar y aplicar migraciones en entornos controlados.
- Escalado: analizar sharding si la colección `productos` crece mucho o si la carga de escritura es alta.

## 13. Glosario rápido

- API: Interfaz de programación.
- REST: Estilo de arquitectura para APIs HTTP.
- JWT: JSON Web Token.
- BSON: Binary JSON, formato interno de Mongo.
- TTL: Time To Live, índice que borra documentos automáticamente.

## 14. Checklist de entrega (archivos a incluir)

- `README.md` con instrucciones de arranque (recomendado).  
- `docs/DOCUMENTACION.md` (este archivo, ampliado).  
- `scripts/configurar-mongodb-local.js` y scripts de migración.  
- Código fuente en `src/main/java/...` y frontend en `src/main/resources/static`.  
- Evidencias: capturas o logs, branch/tag entregable (`dev1`).

---

Si querés, genero ahora el `README.md` en la raíz con pasos resumidos y algunos comandos listos para que los pegues durante la defensa. ¿Lo hago? 
