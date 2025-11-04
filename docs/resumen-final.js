// Script para mostrar resumen final de la base de datos
print("=== RESUMEN FINAL DE LA BASE DE DATOS ===");

print("\n📁 COLECCIONES:");
db.runCommand("listCollections").cursor.firstBatch.forEach(
    function(collection) {
        print("- " + collection.name);
    }
);

print("\n📊 CONTEO DE DOCUMENTOS:");
print("- Usuarios: " + db.usuarios.countDocuments());
print("- Productos: " + db.productos.countDocuments());
print("- Sucursales: " + db.sucursales.countDocuments());
print("- Carritos: " + db.carritos.countDocuments());
print("- Órdenes: " + db.ordenes.countDocuments());

print("\n👥 USUARIOS DISPONIBLES:");
db.usuarios.find({}, {email: 1, rol: 1, nombre: 1, apellido: 1}).forEach(
    function(usuario) {
        print("- " + usuario.nombre + " " + usuario.apellido + " (" + usuario.email + ") - " + usuario.rol);
    }
);

print("\n📦 STOCK TOTAL POR SUCURSAL:");
db.sucursales.find().forEach(function(sucursal) {
    const stockTotal = db.productos.aggregate([
        { $unwind: "$inventario" },
        { $match: { "inventario.sucursal_id": sucursal._id.toString() } },
        { $group: { _id: null, total: { $sum: "$inventario.stock" } } }
    ]).toArray();
    
    const total = stockTotal.length > 0 ? stockTotal[0].total : 0;
    print("- " + sucursal.nombre + ": " + total + " unidades");
});

print("\n🛒 CATEGORÍAS DE PRODUCTOS:");
db.productos.aggregate([
    { $group: { _id: "$categoria", count: { $sum: 1 } } },
    { $sort: { count: -1 } }
]).forEach(function(categoria) {
    print("- " + categoria._id + ": " + categoria.count + " productos");
});

print("\n🎉 BASE DE DATOS LISTA PARA TESTING");
print("\n🔐 CREDENCIALES:");
print("Admin: admin@tiendaropa.com / admin123");
print("Usuario: juan.perez@email.com / user123");
print("\n🌐 ACCESO:");
print("Aplicación: http://localhost:8080");
print("Login: http://localhost:8080/login.html");
print("Admin Panel: http://localhost:8080/admin.html");