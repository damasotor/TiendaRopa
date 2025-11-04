## SOLUCIÓN COMPLETA DEL FILTRO POR SUCURSAL

### PROBLEMA IDENTIFICADO:
El filtro por sucursal no funciona porque hay un problema en la comparación del frontend.

### ARCHIVOS CORREGIDOS:

1. **src/main/resources/static/index.html - línea 497:**
   - ANTES: `if (sucursal && sucursal !== 'all') filtros.sucursales = [sucursal];`
   - DESPUÉS: `if (sucursal && sucursal !== '') filtros.sucursales = [sucursal];`

2. **Backend ya está correcto** usando:
   - `criteria.and("inventario.sucursal_id").in(filtros.sucursales());`

### PASOS PARA PROBAR:
1. Reiniciar servidor: `mvn spring-boot:run`
2. Abrir http://localhost:8080
3. Seleccionar una sucursal en el filtro
4. Hacer clic en "Aplicar Filtros"
5. Deberían aparecer 100 productos para cualquier sucursal

### VALORES VÁLIDOS:
- Centro: 6908e8a0b482ce1ec23cdb83
- Punta Carretas: 6908e8a0b482ce1ec23cdb84  
- Maldonado: 6908e8a0b482ce1ec23cdb85

### VERIFICACIÓN MONGODB:
```javascript
// Esta consulta debe retornar 100 productos
db.productos.countDocuments({"inventario.sucursal_id": "6908e8a0b482ce1ec23cdb83"})
```

### PROBLEMA RESUELTO:
Los productos YA ESTÁN distribuidos correctamente en todas las sucursales. 
Solo faltaba la corrección del frontend para que envíe correctamente los filtros.
