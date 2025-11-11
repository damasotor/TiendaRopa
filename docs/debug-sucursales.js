// Script para depurar el problema de mapeo de sucursales
// Ejecutar con: mongosh tienda_ropa docs/debug-sucursales.js

print("=== DEBUG: MAPEO DE SUCURSALES ===");

// 1. Ver sucursales en la BD
print("\n1. Sucursales en la base de datos:");
const sucursales = db.sucursales.find({}, { _id: 1, nombre: 1 }).toArray();
sucursales.forEach(s => {
    print(`  ID: ${s._id}, Nombre: ${s.nombre}`);
});

// 2. Ver qué IDs usan los productos en su inventario
print("\n2. IDs de sucursales usados en inventario de productos:");
const inventarioIds = db.productos.aggregate([
    { $unwind: "$inventario" },
    { $group: { _id: "$inventario.sucursal_id", count: { $sum: 1 } } },
    { $sort: { _id: 1 } }
]).toArray();

inventarioIds.forEach(item => {
    print(`  Sucursal ID en inventario: "${item._id}" (usado en ${item.count} items)`);
});

// 3. Verificar coincidencias
print("\n3. Verificando coincidencias:");
const sucursalIdsFromDB = sucursales.map(s => s._id.toString());
const sucursalIdsFromInventario = inventarioIds.map(item => item._id);

print("IDs de sucursales de la BD:", sucursalIdsFromDB);
print("IDs de sucursales en inventario:", sucursalIdsFromInventario);

let hayCoincidencias = false;
sucursalIdsFromInventario.forEach(invId => {
    if (sucursalIdsFromDB.includes(invId)) {
        print(`✅ COINCIDENCIA: ${invId}`);
        hayCoincidencias = true;
    } else {
        print(`❌ NO COINCIDE: inventario usa "${invId}" pero no existe sucursal con ese ID`);
    }
});

if (!hayCoincidencias) {
    print("\n❌ PROBLEMA: NO HAY COINCIDENCIAS entre IDs de sucursales y inventario");
    print("Esto explica por qué aparece 'undefined' en el frontend");
}

// 4. Mostrar estructura completa de algunas sucursales
print("\n4. Estructura completa de sucursales:");
db.sucursales.find().limit(3).forEach(s => {
    print(`Sucursal: ${JSON.stringify(s, null, 2)}`);
});

// 5. Mostrar inventario de algunos productos
print("\n5. Ejemplo de inventario en productos:");
db.productos.find({}, { nombre: 1, inventario: 1 }).limit(3).forEach(p => {
    print(`Producto: ${p.nombre}`);
    if (p.inventario) {
        p.inventario.forEach(inv => {
            print(`  - sucursal_id: "${inv.sucursal_id}", stock: ${inv.stock}`);
        });
    }
});