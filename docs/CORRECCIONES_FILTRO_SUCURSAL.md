## CORRECCIONES APLICADAS PARA FILTRO POR SUCURSAL

### PROBLEMA IDENTIFICADO:
El filtro por sucursal no mostraba productos debido a problemas de serialización/deserialización de ObjectIds entre el backend Spring Boot y el frontend JavaScript.

### CORRECCIONES IMPLEMENTADAS:

#### 1. **Backend - Modelo Sucursal** (Sucursal.java):
```java
// Agregadas anotaciones Jackson para serialización correcta
@JsonSerialize(using = ToStringSerializer.class)
@JsonProperty("id")
private ObjectId id;

// Agregado método helper
public String getIdAsString() {
    return id != null ? id.toString() : null;
}
```

#### 2. **Frontend - Carga de Sucursales** (index.html):
```javascript
// Múltiples formas de obtener el ID
const sucursalId = sucursal.id || sucursal._id || sucursal.idAsString;

// Logging mejorado
console.log(`Sucursal cargada: ${sucursal.nombre} -> ID: ${sucursalId}`);
```

#### 3. **Frontend - Aplicación de Filtros** (index.html):
```javascript
// Debug mejorado para filtros por sucursal
if (sucursal && sucursal !== '') {
    filtros.sucursales = [sucursal];
    console.log('🔍 FILTRO POR SUCURSAL APLICADO:', sucursal);
}

// Logging de resultados específico para sucursales
if (filtros.sucursales.length > 0) {
    console.log('🏢 Filtro por sucursal activo, productos encontrados:', productosFiltrados.length);
    if (productosFiltrados.length === 0) {
        console.warn('⚠️ NO SE ENCONTRARON PRODUCTOS para sucursal:', filtros.sucursales[0]);
    }
}
```

#### 4. **Fallback Mejorado**:
```javascript
// IDs hardcoded como strings que coinciden exactamente con la BD
const sucursalesDisponibles = [
    {id: '6908e8a0b482ce1ec23cdb83', nombre: 'Centro'},
    {id: '6908e8a0b482ce1ec23cdb84', nombre: 'Punta Carretas'},
    {id: '6908e8a0b482ce1ec23cdb85', nombre: 'Maldonado'}
];
```

### DATOS VERIFICADOS EN MONGODB:
```
ID: ObjectId('6908e8a0b482ce1ec23cdb83') Nombre: Centro
ID: ObjectId('6908e8a0b482ce1ec23cdb84') Nombre: Punta Carretas  
ID: ObjectId('6908e8a0b482ce1ec23cdb85') Nombre: Maldonado

Inventario estructura:
{
  inventario: [
    { sucursal_id: '6908e8a0b482ce1ec23cdb83', stock: 45 },
    { sucursal_id: '6908e8a0b482ce1ec23cdb84', stock: 51 },
    { sucursal_id: '6908e8a0b482ce1ec23cdb85', stock: 28 }
  ]
}
```

### ARCHIVOS MODIFICADOS:
1. `/src/main/java/com/ropa/tienda/model/Sucursal.java`
2. `/src/main/resources/static/index.html`

### PARA TESTING:
1. Reiniciar servidor: `mvn spring-boot:run`
2. Abrir navegador: http://localhost:8080
3. Abrir Developer Tools (F12) para ver los logs
4. Seleccionar cualquier sucursal
5. Hacer clic en "Aplicar Filtros"
6. Verificar logs en consola para debugging

### RESULTADO ESPERADO:
- ✅ Los filtros por sucursal deberían mostrar 100 productos para cualquier sucursal
- ✅ Los logs deberían mostrar el ID de la sucursal seleccionada
- ✅ No debería aparecer el warning de "NO SE ENCONTRARON PRODUCTOS"
- ✅ El contador debería mostrar "100 productos encontrados"
