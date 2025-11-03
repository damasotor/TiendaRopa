// Script para probar el filtro por sucursal directamente
print("=== PROBANDO FILTRO POR SUCURSAL ===");

// Obtener una sucursal específica para probar
const sucursal = db.sucursales.findOne({nombre: "Centro"});
print(`\n🎯 Probando filtro para sucursal: ${sucursal.nombre} (${sucursal._id})`);

// Hacer la consulta que debería hacer el backend
const query = {
    "inventario.sucursal_id": sucursal._id.toString()
};

print("\n🔍 Query MongoDB equivalente:");
print(JSON.stringify(query, null, 2));

const productosEnSucursal = db.productos.find(query).limit(5);
print(`\n📦 Productos encontrados en ${sucursal.nombre}:`);

let count = 0;
productosEnSucursal.forEach(function(producto) {
    count++;
    print(`${count}. ${producto.nombre} - Categoría: ${producto.categoria}`);
    
    // Mostrar inventario específico de esta sucursal
    const inventarioSucursal = producto.inventario.find(function(inv) {
        return inv.sucursal_id === sucursal._id.toString();
    });
    
    if (inventarioSucursal) {
        print(`   Stock en ${sucursal.nombre}: ${inventarioSucursal.stock} unidades`);
    }
});

print(`\n📊 Total de productos en ${sucursal.nombre}: ${db.productos.countDocuments(query)}`);

// Probar con todas las sucursales
print("\n🏢 PRUEBA CON TODAS LAS SUCURSALES:");
db.sucursales.find().forEach(function(suc) {
    const countSuc = db.productos.countDocuments({
        "inventario.sucursal_id": suc._id.toString()
    });
    print(`- ${suc.nombre}: ${countSuc} productos`);
});