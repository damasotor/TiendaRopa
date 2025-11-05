// Script para insertar datos de prueba en MongoDB
// Ejecutar con: mongosh tiendaropa insertar-datos-prueba.js

// Asegurar que estamos en la base de datos correcta
if (typeof use === 'function') {
  use('tiendaropa');
} else if (typeof db !== 'undefined' && typeof db.getSiblingDB === 'function') {
  db = db.getSiblingDB('tiendaropa');
} else {
  throw new Error("Este script debe ejecutarse en mongosh/mongo");
}

print("=== INSERTANDO DATOS DE PRUEBA ===");

// 1. CREAR USUARIOS
print("\n1. Credenciales de usuarios de prueba:");
print("Admin: admin@gmail.com / admin123");
print("Usuario: usuario@gmail.com / password");
print("\nNota: Los usuarios ya están creados por el script configurar-mongodb-local.js");

// 2. OBTENER IDs DE SUCURSALES
print("\n2. Obteniendo sucursales...");
const sucursales = db.sucursales.find().toArray();
print("✅ Encontradas " + sucursales.length + " sucursales");

// 3. CREAR PRODUCTOS
print("\n3. Creando 100 productos...");

const categorias = ["camisas", "pantalones", "zapatos", "accesorios"]; // Debe coincidir con el esquema
const colores = ["Rojo", "Azul", "Verde", "Negro", "Blanco", "Gris", "Rosa", "Amarillo", "Marrón", "Violeta"];
const tallas = ["XS", "S", "M", "L", "XL", "XXL"];
const marcas = ["Nike", "Adidas", "Zara", "H&M", "Puma", "Levis", "Calvin Klein", "Tommy Hilfiger"];

const productos = [];

for (let i = 1; i <= 100; i++) {
    const categoria = categorias[Math.floor(Math.random() * categorias.length)];
    const color = colores[Math.floor(Math.random() * colores.length)];
    const talla = tallas[Math.floor(Math.random() * tallas.length)];
    const marca = marcas[Math.floor(Math.random() * marcas.length)];
    
    // Generar inventario para cada sucursal
    const inventario = sucursales.map(sucursal => ({
        sucursal_id: sucursal._id.toString(), // Usar sucursal_id para coincidir con el índice
        stock: Math.floor(Math.random() * 50) + 5, // Entre 5 y 54 unidades
        stockMinimo: 5
    }));
    
    // Generar precio siempre como double (con decimales)
    let precioBase = Math.random() * 2000 + 500;
    let precio = Math.round(precioBase * 100) / 100;
    // Si el precio resulta ser un entero, agregarle decimales mínimos
    if (precio === Math.floor(precio)) {
        precio = precio + 0.01;
    }
    
    const producto = {
        nombre: `${categoria} ${marca} ${color}`,
        descripcion: `${categoria} de marca ${marca} en color ${color}, talla ${talla}. Producto de alta calidad.`,
        precio: precio, // Ahora siempre será double
        categoria: categoria,
        atributos: {
            color: color,
            talla: talla,
            material: categoria === "zapatos" ? "Cuero" : "Algodón",
            genero: Math.random() > 0.5 ? "Unisex" : (Math.random() > 0.5 ? "Hombre" : "Mujer"),
            marca: marca // Mover marca a atributos ya que no está permitida como campo raíz
        },
        inventario: inventario,
        creadoEn: new Date() // Cambiar de fechaCreacion a creadoEn según el esquema
    };
    
    productos.push(producto);
}

// Insertar productos de uno en uno para mejor manejo de errores
let insertados = 0;
let errores = 0;

for (let i = 0; i < productos.length; i++) {
    try {
        db.productos.insertOne(productos[i]);
        insertados++;
    } catch (error) {
        errores++;
        print(`❌ Error insertando producto ${i + 1}: ${error.message}`);
        
        // Mostrar detalles del producto que falló para debugging
        print(`   Producto que falló: ${JSON.stringify(productos[i], null, 2)}`);
    }
}

print(`✅ Productos insertados exitosamente: ${insertados}`);
if (errores > 0) {
    print(`❌ Productos que fallaron: ${errores}`);
}

// 4. VERIFICAR DATOS CREADOS
print("\n4. Verificando datos creados...");

const totalUsuarios = db.usuarios.countDocuments({});
const totalProductos = db.productos.countDocuments({});
const totalSucursales = db.sucursales.countDocuments({});

print("📊 RESUMEN:");
print("- Usuarios: " + totalUsuarios);
print("- Productos: " + totalProductos);
print("- Sucursales: " + totalSucursales);

// Mostrar stock total por sucursal
print("\n📦 STOCK POR SUCURSAL:");
sucursales.forEach(sucursal => {
    const stockTotal = db.productos.aggregate([
        { $unwind: "$inventario" },
        { $match: { "inventario.sucursal_id": sucursal._id.toString() } }, // Usar sucursal_id
        { $group: { _id: null, total: { $sum: "$inventario.stock" } } }
    ]).toArray();
    
    const total = stockTotal.length > 0 ? stockTotal[0].total : 0;
    print(`- ${sucursal.nombre}: ${total} unidades`);
});

print("\n🎉 DATOS DE PRUEBA INSERTADOS EXITOSAMENTE");
print("\n👤 CREDENCIALES DE ACCESO:");
print("Admin: admin@gmail.com / admin123");
print("Usuario: usuario@gmail.com / password");