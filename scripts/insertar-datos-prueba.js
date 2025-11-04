// Script para insertar datos de prueba en MongoDB
// Ejecutar con: mongosh tiendaropa insertar-datos-prueba.js

print("=== INSERTANDO DATOS DE PRUEBA ===");

// 1. CREAR USUARIOS
print("\n1. Credenciales de usuarios de prueba:");
print("Admin: admin@gmail.com / admin123");
print("Usuario: usuario@gmail.com / user123");
print("\nNota: Si necesitas crear estos usuarios desde este script, añade lógica para comprobar existencia e insertar en db.usuarios evitando duplicados.");

// 2. OBTENER IDs DE SUCURSALES
print("\n2. Obteniendo sucursales...");
const sucursales = db.sucursales.find().toArray();
print("✅ Encontradas " + sucursales.length + " sucursales");

// 3. CREAR PRODUCTOS
print("\n3. Creando 100 productos...");

const categorias = ["Camisas", "Pantalones", "Vestidos", "Zapatos", "Accesorios", "Chaquetas", "Faldas", "Shorts"];
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
        sucursal_id: sucursal._id.toString(),
        stock: Math.floor(Math.random() * 50) + 5, // Entre 5 y 54 unidades
        stockMinimo: 5,
        ultimaActualizacion: new Date()
    }));
    
    const producto = {
        nombre: `${categoria} ${marca} ${color}`,
        descripcion: `${categoria} de marca ${marca} en color ${color}, talla ${talla}. Producto de alta calidad.`,
        precio: Math.floor(Math.random() * 2000) + 500, // Entre $500 y $2500
        categoria: categoria,
        marca: marca,
        sku: `SKU${String(i).padStart(3, '0')}-${marca.substring(0,3).toUpperCase()}-${color.substring(0,3).toUpperCase()}`,
        atributos: {
            color: color,
            talla: talla,
            material: categoria === "Zapatos" ? "Cuero" : "Algodón",
            genero: Math.random() > 0.5 ? "Unisex" : (Math.random() > 0.5 ? "Hombre" : "Mujer")
        },
        inventario: inventario,
        fechaCreacion: new Date(),
        activo: true,
        descuento: Math.random() > 0.8 ? Math.floor(Math.random() * 30) + 5 : 0 // 20% chance de descuento
    };
    
    productos.push(producto);
}

// Insertar productos
try {
    db.productos.insertMany(productos);
    print("✅ 100 productos creados exitosamente");
} catch (error) {
    print("❌ Error creando productos: " + error);
}

// 4. VERIFICAR DATOS CREADOS
print("\n4. Verificando datos creados...");

const totalUsuarios = db.usuarios.countDocuments();
const totalProductos = db.productos.countDocuments();
const totalSucursales = db.sucursales.countDocuments();

print("📊 RESUMEN:");
print("- Usuarios: " + totalUsuarios);
print("- Productos: " + totalProductos);
print("- Sucursales: " + totalSucursales);

// Mostrar stock total por sucursal
print("\n📦 STOCK POR SUCURSAL:");
sucursales.forEach(sucursal => {
    const stockTotal = db.productos.aggregate([
        { $unwind: "$inventario" },
        { $match: { "inventario.sucursal_id": sucursal._id.toString() } },
        { $group: { _id: null, total: { $sum: "$inventario.stock" } } }
    ]).toArray();
    
    const total = stockTotal.length > 0 ? stockTotal[0].total : 0;
    print(`- ${sucursal.nombre}: ${total} unidades`);
});

print("\n🎉 DATOS DE PRUEBA INSERTADOS EXITOSAMENTE");
print("\n👤 CREDENCIALES DE ACCESO:");
print("Admin: admin / admin123");
print("Usuario: usuario1 / user123");