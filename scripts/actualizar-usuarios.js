// Script para actualizar usuarios con información completa
// Ejecutar con: mongosh tiendaropa actualizar-usuarios.js

print("=== ACTUALIZANDO USUARIOS ===");

// Actualizar usuario administrador
const adminUpdate = db.usuarios.updateOne(
    { email: "admin@tiendaropa.com" },
    {
        $set: {
            username: "admin",
            nombre: "Administrador",
            apellido: "Sistema",
            telefono: "099123456",
            fechaNacimiento: new Date("1980-01-15"),
            direccion: {
                calle: "18 de Julio",
                numero: "1234",
                ciudad: "Montevideo",
                departamento: "Montevideo",
                codigoPostal: "11100"
            },
            rol: "ROLE_ADMIN", // Cambiar a administrador
            fechaRegistro: new Date(),
            activo: true
        }
    }
);

// Actualizar usuario común
const userUpdate = db.usuarios.updateOne(
    { email: "juan.perez@email.com" },
    {
        $set: {
            username: "usuario1",
            nombre: "Juan",
            apellido: "Pérez",
            telefono: "099654321",
            fechaNacimiento: new Date("1990-05-20"),
            direccion: {
                calle: "Avenida Italia",
                numero: "5678",
                ciudad: "Montevideo",
                departamento: "Montevideo",
                codigoPostal: "11600"
            },
            rol: "ROLE_USER",
            fechaRegistro: new Date(),
            activo: true
        }
    }
);

print("✅ Usuario admin actualizado: " + adminUpdate.modifiedCount + " documento");
print("✅ Usuario común actualizado: " + userUpdate.modifiedCount + " documento");

// Verificar usuarios actualizados
print("\n📋 USUARIOS FINALES:");
db.usuarios.find({}, {
    username: 1, 
    email: 1, 
    nombre: 1, 
    apellido: 1, 
    rol: 1,
    telefono: 1
}).forEach(function(usuario) {
    print(`- ${usuario.nombre} ${usuario.apellido} (${usuario.username})`);
    print(`  Email: ${usuario.email}`);
    print(`  Rol: ${usuario.rol}`);
    print(`  Teléfono: ${usuario.telefono}`);
    print("");
});

print("🎉 USUARIOS ACTUALIZADOS CORRECTAMENTE");
print("\n👤 CREDENCIALES DE ACCESO:");
print("Admin: admin@tiendaropa.com / admin123");
print("Usuario: juan.perez@email.com / user123");