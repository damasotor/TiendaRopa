// Script para verificar la distribución de productos por sucursal
print("=== VERIFICANDO DISTRIBUCIÓN DE PRODUCTOS POR SUCURSAL ===");

print("\n📊 ANÁLISIS ACTUAL:");

// Obtener IDs de sucursales
const sucursales = db.sucursales.find({}, {_id: 1, nombre: 1}).toArray();
print("\n🏢 SUCURSALES DISPONIBLES:");
sucursales.forEach(function(sucursal) {
    print(`- ${sucursal.nombre}: ${sucursal._id}`);
});

// Verificar cuántos productos tienen inventario por sucursal
print("\n📦 PRODUCTOS CON INVENTARIO POR SUCURSAL:");
sucursales.forEach(function(sucursal) {
    const count = db.productos.countDocuments({
        "inventario.sucursal_id": sucursal._id.toString()
    });
    print(`- ${sucursal.nombre}: ${count} productos con inventario`);
});

// Mostrar algunos ejemplos de estructura de inventario actual
print("\n🔍 EJEMPLOS DE ESTRUCTURA ACTUAL:");
const ejemplos = db.productos.find({}, {nombre: 1, inventario: 1}).limit(3).toArray();
ejemplos.forEach(function(producto, index) {
    print(`\nProducto ${index + 1}: ${producto.nombre}`);
    print("Inventario actual:");
    if (producto.inventario && producto.inventario.length > 0) {
        producto.inventario.forEach(function(inv) {
            print(`  - Sucursal ID: ${inv.sucursal_id}, Stock: ${inv.stock}`);
        });
    } else {
        print("  - Sin inventario definido");
    }
});

// Verificar si hay productos sin inventario
const sinInventario = db.productos.countDocuments({
    $or: [
        { inventario: { $exists: false } },
        { inventario: { $size: 0 } },
        { inventario: null }
    ]
});
print(`\n⚠️  Productos sin inventario: ${sinInventario}`);

print("\n🎯 PRÓXIMO PASO: Redistribuir productos en todas las sucursales");