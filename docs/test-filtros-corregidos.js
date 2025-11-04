// Script para probar los filtros corregidos
print("=== PROBANDO FILTROS CORREGIDOS ===");

print("\n📊 DATOS DISPONIBLES PARA FILTRAR:");

print("\n🏢 SUCURSALES:");
db.sucursales.find({}, {_id: 1, nombre: 1}).forEach(function(sucursal) {
    print(`- ID: ${sucursal._id} | Nombre: ${sucursal.nombre}`);
});

print("\n📁 CATEGORÍAS:");
const categorias = db.productos.distinct('categoria');
categorias.forEach(function(categoria) {
    const count = db.productos.countDocuments({categoria: categoria});
    print(`- ${categoria}: ${count} productos`);
});

print("\n🎨 COLORES:");
const colores = db.productos.distinct('atributos.color');
colores.forEach(function(color) {
    const count = db.productos.countDocuments({"atributos.color": color});
    print(`- ${color}: ${count} productos`);
});

print("\n💰 RANGO DE PRECIOS:");
const precios = db.productos.aggregate([
    {
        $group: {
            _id: null,
            minPrecio: { $min: "$precio" },
            maxPrecio: { $max: "$precio" },
            promedio: { $avg: "$precio" }
        }
    }
]).toArray();

if (precios.length > 0) {
    const stats = precios[0];
    print(`- Precio mínimo: $${stats.minPrecio}`);
    print(`- Precio máximo: $${stats.maxPrecio}`);
    print(`- Precio promedio: $${stats.promedio.toFixed(2)}`);
}

print("\n📦 STOCK TOTAL POR SUCURSAL:");
db.sucursales.find().forEach(function(sucursal) {
    const stockTotal = db.productos.aggregate([
        { $unwind: "$inventario" },
        { $match: { "inventario.sucursal_id": sucursal._id.toString() } },
        { $group: { _id: null, total: { $sum: "$inventario.stock" } } }
    ]).toArray();
    
    const total = stockTotal.length > 0 ? stockTotal[0].total : 0;
    print(`- ${sucursal.nombre} (${sucursal._id}): ${total} unidades`);
});

print("\n🧪 PRUEBAS RECOMENDADAS EN EL FRONTEND:");
print("1. Filtrar por categoría 'Pantalones' (debería mostrar ~19 productos)");
print("2. Filtrar por color 'Azul' (debería mostrar varios productos)");
print("3. Filtrar por precio máximo $1000 (debería limitar resultados)");
print("4. Filtrar por sucursal específica usando ID");
print("5. Combinar múltiples filtros para pruebas avanzadas");

print("\n🌐 URLs PARA TESTING:");
print("Frontend: http://localhost:8080");
print("API Productos: http://localhost:8080/api/productos");
print("API Sucursales: http://localhost:8080/api/sucursales");
print("API Filtros: http://localhost:8080/api/productos/filtrar");

print("\n✅ CORRECCIONES APLICADAS:");
print("- ✅ Filtros de sucursal usan IDs reales en lugar de nombres");
print("- ✅ Categorías actualizadas con valores reales de la BD");
print("- ✅ Colores actualizados con valores reales de la BD");
print("- ✅ Backend corregido para usar estructura de inventario correcta");
print("- ✅ Frontend carga sucursales dinámicamente desde API");