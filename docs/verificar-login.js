// Script para verificar credenciales disponibles para login
print("=== CREDENCIALES DISPONIBLES PARA TESTING ===");

print("\n👥 USUARIOS EN LA BASE DE DATOS:");
db.usuarios.find({}, {
    email: 1, 
    nombre: 1, 
    rol: 1
}).forEach(function(usuario) {
    print(`📧 Email: ${usuario.email}`);
    print(`👤 Nombre: ${usuario.nombre || 'Sin nombre'}`);
    print(`🔐 Rol: ${usuario.rol}`);
    
    // Sugerir contraseña basada en el tipo de usuario
    if (usuario.rol === "ROLE_ADMIN") {
        print(`🔑 Contraseña sugerida: admin123`);
    } else {
        print(`🔑 Contraseña sugerida: user123`);
    }
    print("---");
});

print("\n🌐 URLS PARA TESTING:");
print("🔗 Login: http://localhost:8080/login.html");
print("🔗 Página Principal: http://localhost:8080");
print("🔗 Panel Admin: http://localhost:8080/admin.html");

print("\n✅ CAMBIOS APLICADOS EN EL LOGIN:");
print("- ❌ Icono removido del título");
print("- ❌ Botones Administrador/Usuario removidos");
print("- ✅ Solo campos Email y Contraseña");
print("- ✅ Campos vacíos por defecto");
print("- ✅ Placeholders descriptivos");

print("\n🎯 INSTRUCCIONES DE PRUEBA:");
print("1. Ve a http://localhost:8080/login.html");
print("2. Los campos deben aparecer vacíos");
print("3. Ingresa email y contraseña manualmente");
print("4. El sistema detectará automáticamente el rol");
print("5. Redirección automática según el rol del usuario");