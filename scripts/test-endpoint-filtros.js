// Script para probar el endpoint de filtro por sucursal
print("=== PRUEBA DEL ENDPOINT DE FILTROS ===");

// Simular llamada al endpoint POST /api/productos/filtrar
// con filtro por sucursal

const sucursalId = "6908e8a0b482ce1ec23cdb83"; // Centro
print(`\n🎯 Probando filtro para sucursal: ${sucursalId}`);

// Consulta que debería hacer el backend
const query = {
    "inventario.sucursal_id": sucursalId
};

print("\n🔍 Query MongoDB que debería ejecutar el backend:");
print(JSON.stringify(query, null, 2));

// Verificar resultados esperados
const countExpected = db.productos.countDocuments(query);
print(`\n📊 Productos esperados: ${countExpected}`);

// Mostrar algunos ejemplos
print("\n📦 Primeros 3 productos que deberían aparecer:");
db.productos.find(query, {nombre: 1, categoria: 1, precio: 1}).limit(3).forEach(function(producto) {
    print(`- ${producto.nombre} (${producto.categoria}) - $${producto.precio}`);
});

print("\n🧪 PAYLOAD PARA TESTING:");
const payload = {
    "sucursales": [sucursalId],
    "stockMinimo": 1
};
print("POST /api/productos/filtrar");
print("Content-Type: application/json");
print(JSON.stringify(payload, null, 2));

print("\n✅ VERIFICACIONES:");
print("1. ¿El servidor está ejecutándose en puerto 8080? ✅");
print("2. ¿La consulta MongoDB funciona directamente? ✅");
print("3. ¿Los IDs de sucursal son correctos? ✅");
print("4. Ahora verificar si el endpoint del backend funciona...");