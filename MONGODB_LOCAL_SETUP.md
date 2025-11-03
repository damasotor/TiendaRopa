# CONFIGURACIÓN DE MONGODB LOCAL - INSTRUCCIONES PASO A PASO

## Prerequisitos
1. **Instalar MongoDB Community Server**
   - Descargar desde: https://www.mongodb.com/try/download/community
   - Instalar con opciones por defecto
   - Asegurarse de que MongoDB Compass se instale también

2. **Verificar instalación**
   - Abrir Command Prompt como administrador
   - Ejecutar: `mongod --version`
   - Debería mostrar la versión instalada

## Paso 1: Iniciar MongoDB
1. Abrir Command Prompt como administrador
2. Ejecutar: `net start MongoDB`
   - Si no funciona, ejecutar: `mongod --dbpath C:\data\db`

## Paso 2: Configurar la Base de Datos
1. **Abrir MongoDB Compass**
   - Conectar a: `mongodb://localhost:27017`

2. **Crear la base de datos manualmente**
   - Click en "Create Database"
   - Database Name: `tienda_ropa`
   - Collection Name: `productos`

3. **Crear las demás colecciones**
   - En la base de datos `tienda_ropa`, crear:
     - `carritos`
     - `ordenes`
     - `usuarios`
     - `sucursales`

## Paso 3: Configurar Aplicación
La aplicación ya está configurada para usar MongoDB local. El archivo `application.properties` está listo.

## Paso 4: Ejecutar la Aplicación
1. **Detener cualquier instancia anterior**
   ```powershell
   taskkill /f /im java.exe
   ```

2. **Navegar al directorio del proyecto**
   ```powershell
   cd "C:\Users\Usuario\Documents\NoSQL\TiendaRopa"
   ```

3. **Iniciar la aplicación**
   ```powershell
   mvn spring-boot:run
   ```

4. **Verificar que se carguen los datos**
   - La consola debería mostrar: "X productos creados exitosamente con inventario completo!"
   - Si ya existen datos, mostrará: "Ya existen productos en la base de datos"

## Paso 5: Verificar en MongoDB Compass
1. **Refrescar la vista en Compass**
2. **Verificar las colecciones**
   - `productos`: Debería tener 12 productos con inventario detallado
   - `sucursales`: Debería tener 3 sucursales
   - `usuarios`: Debería tener 1 usuario administrador

3. **Examinar un producto**
   - Abrir la colección `productos`
   - Ver la estructura del inventario por sucursal
   - Notar los campos de stock por sucursal

## Paso 6: Probar la Funcionalidad de Stock
1. **Abrir la aplicación**: http://localhost:8080
2. **Agregar productos al carrito**
3. **Realizar una compra**
4. **Verificar en Compass que el stock se redujo**

## Características del Sistema de Stock

### Stock por Sucursal
Cada producto tiene un array `inventario` con:
- `sucursalId`: ID de la sucursal
- `stock`: Cantidad disponible
- `stockMinimo`: Nivel mínimo antes de reabastecimiento
- `stockMaximo`: Nivel máximo permitido

### Reducción Automática de Stock
- Al realizar una compra, el stock se reduce automáticamente
- Se verifica que hay stock suficiente antes de procesar
- Si no hay stock, la compra se rechaza

### Monitoreo en Tiempo Real
- Los cambios se reflejan inmediatamente en la base de datos
- Puedes ver las actualizaciones en MongoDB Compass
- Los logs muestran cada actualización de stock

## Estructura de Datos Creada

### Productos (12 items)
- **Camisas**: Camisa Formal Azul, Camisa Casual Blanca, Camisa Polo Roja
- **Pantalones**: Jeans Clásico Negro, Pantalón Formal Gris, Short Deportivo  
- **Zapatos**: Zapatillas Deportivas, Zapatos Oxford Negro, Botas de Trabajo
- **Accesorios**: Cinturón de Cuero Negro, Reloj Deportivo Digital, Gafas de Sol Aviador

### Sucursales (3 ubicaciones)
- **Centro**: 18 de Julio 1234, Montevideo
- **Punta Carretas**: Ellauri 350, Montevideo  
- **Maldonado**: Sarandí 123, Maldonado

### Stock Inicial
Cada producto tiene stock distribuido entre las 3 sucursales con diferentes niveles según la demanda esperada.

## Comandos Útiles

### Reiniciar aplicación
```powershell
taskkill /f /im java.exe
cd "C:\Users\Usuario\Documents\NoSQL\TiendaRopa"
mvn spring-boot:run
```

### Limpiar y recrear datos (si es necesario)
```javascript
// En MongoDB Compass, ejecutar en la consola:
use tienda_ropa
db.productos.deleteMany({})
db.carritos.deleteMany({})
db.ordenes.deleteMany({})
// Luego reiniciar la aplicación
```

### Ver stock actual
```javascript
// En MongoDB Compass:
db.productos.find({}, {nombre: 1, stock: 1, inventario: 1})
```

## Solución de Problemas

1. **MongoDB no inicia**
   - Verificar que el servicio esté ejecutándose: `net start MongoDB`
   - Crear directorio de datos: `mkdir C:\data\db`

2. **Aplicación no conecta**
   - Verificar que MongoDB esté en puerto 27017
   - Revisar logs de la aplicación

3. **No se cargan datos**
   - Verificar conexión en los logs
   - Reiniciar la aplicación

4. **Stock no se actualiza**
   - Verificar logs del servidor
   - Refrescar vista en MongoDB Compass

## Credenciales por Defecto
- **Admin**: admin@tiendaropa.com / admin123