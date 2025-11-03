// Script para limpiar campos innecesarios de usuarios
print("=== LIMPIANDO CAMPOS INNECESARIOS DE USUARIOS ===");

// Remover campos apellido y telefono de todos los usuarios
const result = db.usuarios.updateMany(
    {},
    {
        $unset: {
            apellido: "",
            telefono: ""
        }
    }
);

print("✅ Campos apellido y telefono removidos de " + result.modifiedCount + " usuarios");

// Verificar usuarios actualizados
print("\n📋 USUARIOS DESPUÉS DE LA LIMPIEZA:");
db.usuarios.find({}, {
    email: 1, 
    nombre: 1, 
    rol: 1
}).forEach(function(usuario) {
    print(`- ${usuario.nombre || 'N/A'} (${usuario.email}) - ${usuario.rol}`);
});

print("\n🎯 RESULTADO: Solo campos nombre, email y rol mantenidos");