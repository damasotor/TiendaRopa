// Script para verificar que el selector de sucursales funciona correctamente
// Ejecutar con: mongosh tienda_ropa docs/verify-sucursal-mapping.js

print("=== VERIFICACIÓN DE MAPEO SUCURSALES-INVENTARIO ===");

// 1. Mostrar sucursales disponibles
print("\n1. Sucursales en la base de datos:");
const sucursales = db.sucursales.find({}, { _id: 1, nombre: 1, direccion: 1 }).toArray();
sucursales.forEach(s => {
    print(`  ID: "${s._id}" -> Nombre: "${s.nombre}" (${s.direccion})`);
});

// 2. Tomar una muestra de productos con inventario
print("\n2. Muestra de inventario en productos:");
const productosConInventario = db.productos.find(
    { "inventario": { $exists: true, $ne: [] } }, 
    { nombre: 1, inventario: 1 }
).limit(3);

productosConInventario.forEach(p => {
    print(`\nProducto: "${p.nombre}"`);
    if (p.inventario) {
        p.inventario.forEach(inv => {
            const sucursalEncontrada = sucursales.find(s => s._id.toString() === inv.sucursal_id);
            if (sucursalEncontrada) {
                print(`  ✅ sucursal_id: "${inv.sucursal_id}" -> ${sucursalEncontrada.nombre} (stock: ${inv.stock})`);
            } else {
                print(`  ❌ sucursal_id: "${inv.sucursal_id}" -> NO ENCONTRADA (stock: ${inv.stock})`);
            }
        });
    }
});

// 3. Verificar que el endpoint REST funcionará
print("\n3. Estructura que debe devolver /api/sucursales:");
sucursales.forEach(s => {
    const jsonEstructura = {
        id: s._id.toString(), // Debido a @JsonSerialize(using = ToStringSerializer.class)
        nombre: s.nombre,
        direccion: s.direccion,
        ciudad: s.ciudad || "No especificada"
    };
    print(`  ${JSON.stringify(jsonEstructura)}`);
});

// 4. Simular lo que haría el frontend
print("\n4. Simulación del mapeo frontend:");
const ejemploInventario = [
    { sucursal_id: sucursales[0]._id.toString(), stock: 10 },
    { sucursal_id: sucursales[1]._id.toString(), stock: 5 },
    { sucursal_id: sucursales[2]._id.toString(), stock: 0 }
];

print("Inventario ejemplo:");
ejemploInventario.forEach(inv => {
    const sucursal = sucursales.find(s => s._id.toString() === inv.sucursal_id);
    if (sucursal && inv.stock > 0) {
        print(`  ✅ Disponible: ${sucursal.nombre} (${inv.stock} unidades)`);
    } else if (sucursal && inv.stock === 0) {
        print(`  ⚪ Sin stock: ${sucursal.nombre}`);
    } else {
        print(`  ❌ ERROR: ID ${inv.sucursal_id} no encontrado`);
    }
});

print("\n=== FIN VERIFICACIÓN ===");
print("Si todo está OK, deberías ver ✅ en todos los puntos importantes.");