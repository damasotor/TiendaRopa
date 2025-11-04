// Script para probar el registro de usuarios con campos completos
print("=== PROBANDO REGISTRO COMPLETO DE USUARIOS ===");

// 1. Probar registro via API con campos completos
print("\n🧪 TEST: Registrando usuario con campos completos vía API");

// Simular lo que hace el panel de admin
const testUser = {
    nombre: "María",
    apellido: "González",
    email: "maria.gonzalez@test.com",
    telefono: "099888777",
    password: "test123",
    rol: "ROLE_USER"
};

print("Datos de prueba:");
print("- Nombre: " + testUser.nombre);
print("- Apellido: " + testUser.apellido);
print("- Email: " + testUser.email);
print("- Teléfono: " + testUser.telefono);
print("- Rol: " + testUser.rol);

print("\n💡 NOTA: Para probar completamente:");
print("1. Ve a: http://localhost:8080/admin.html");
print("2. Inicia sesión con: admin@tiendaropa.com / admin123");
print("3. Usa el formulario mejorado con los nuevos campos");
print("4. Verifica que aparezcan nombre y apellido en lugar de 'N/A'");

print("\n📋 Verificando usuarios actuales:");
db.usuarios.find({}, {
    email: 1, 
    nombre: 1, 
    apellido: 1, 
    telefono: 1, 
    rol: 1
}).forEach(function(usuario) {
    print(`- ${usuario.nombre || 'N/A'} ${usuario.apellido || 'N/A'}`);
    print(`  Email: ${usuario.email}`);
    print(`  Teléfono: ${usuario.telefono || 'N/A'}`);
    print(`  Rol: ${usuario.rol}`);
    print("");
});

print("🎯 OBJETIVO: Ahora el panel de admin debe permitir registrar usuarios completos");