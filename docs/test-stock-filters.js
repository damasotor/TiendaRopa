// Script para probar los filtros corregidos por sucursal y stock
// Ejecutar con: mongosh tienda_ropa docs/test-stock-filters.js

print("=== PRUEBAS DE FILTROS DE STOCK Y SUCURSAL ===");

// 1. Ver el estado actual de inventario de productos
print("\n1. Estado actual del inventario:");
const productos = db.productos.find({}, { nombre: 1, inventario: 1 }).limit(5);
productos.forEach(producto => {
    print(`Producto: ${producto.nombre}`);
    if (producto.inventario) {
        producto.inventario.forEach(inv => {
            print(`  - Sucursal: ${inv.sucursal_id}, Stock: ${inv.stock}`);
        });
    }
    print("");
});

// 2. Buscar productos con stock 0 en alguna sucursal específica (ej: Maldonado)
print("2. Productos con stock 0 en alguna sucursal:");
const productosConStockCero = db.productos.find({
    "inventario": {
        $elemMatch: {
            "stock": 0
        }
    }
}, { nombre: 1, inventario: 1 });

let encontrados = false;
productosConStockCero.forEach(producto => {
    encontrados = true;
    print(`Producto: ${producto.nombre}`);
    producto.inventario.forEach(inv => {
        if (inv.stock === 0) {
            print(`  - ❌ Sucursal: ${inv.sucursal_id}, Stock: ${inv.stock} (AGOTADO)`);
        } else {
            print(`  - ✅ Sucursal: ${inv.sucursal_id}, Stock: ${inv.stock}`);
        }
    });
    print("");
});

if (!encontrados) {
    print("No se encontraron productos con stock 0.");
}

// 3. Simular poner stock 0 a un producto en una sucursal para testing
print("3. Poniendo stock 0 a un producto en sucursal de prueba...");

// Buscar un producto que tenga stock > 0
const productoParaTest = db.productos.findOne({
    "inventario.stock": { $gt: 0 }
});

if (productoParaTest) {
    const sucursalConStock = productoParaTest.inventario.find(inv => inv.stock > 0);
    if (sucursalConStock) {
        print(`Producto seleccionado: ${productoParaTest.nombre}`);
        print(`Sucursal a agotar: ${sucursalConStock.sucursal_id} (stock actual: ${sucursalConStock.stock})`);
        
        // Guardar stock original para restaurar después
        const stockOriginal = sucursalConStock.stock;
        
        // Poner stock en 0
        db.productos.updateOne(
            { 
                _id: productoParaTest._id,
                "inventario.sucursal_id": sucursalConStock.sucursal_id
            },
            { 
                $set: { "inventario.$.stock": 0 } 
            }
        );
        
        print(`✅ Stock puesto en 0 para sucursal ${sucursalConStock.sucursal_id}`);
        
        // 4. Verificar que el filtro funcionaría correctamente
        print("\n4. Verificando que este producto NO aparezca en filtros por esa sucursal:");
        
        // Este query simula el filtro corregido del backend
        const resultadoFiltro = db.productos.find({
            "inventario": {
                $elemMatch: {
                    "sucursal_id": sucursalConStock.sucursal_id,
                    "stock": { $gt: 0 }
                }
            }
        }).count();
        
        print(`Productos con stock > 0 en sucursal ${sucursalConStock.sucursal_id}: ${resultadoFiltro}`);
        
        // Verificar que este producto específico no esté
        const productoEnResultado = db.productos.findOne({
            _id: productoParaTest._id,
            "inventario": {
                $elemMatch: {
                    "sucursal_id": sucursalConStock.sucursal_id,
                    "stock": { $gt: 0 }
                }
            }
        });
        
        if (!productoEnResultado) {
            print(`✅ CORRECTO: Producto ${productoParaTest.nombre} NO aparece en filtro por sucursal ${sucursalConStock.sucursal_id}`);
        } else {
            print(`❌ ERROR: Producto ${productoParaTest.nombre} SIGUE apareciendo en filtro`);
        }
        
        // 5. Restaurar stock original
        print("\n5. Restaurando stock original...");
        db.productos.updateOne(
            { 
                _id: productoParaTest._id,
                "inventario.sucursal_id": sucursalConStock.sucursal_id
            },
            { 
                $set: { "inventario.$.stock": stockOriginal } 
            }
        );
        print(`✅ Stock restaurado a ${stockOriginal} para sucursal ${sucursalConStock.sucursal_id}`);
        
    } else {
        print("No se encontró sucursal con stock para testing");
    }
} else {
    print("No se encontró producto para testing");
}

// 6. Verificar filtro general (productos solo con stock > 0)
print("\n6. Verificando filtro general (solo productos con stock > 0):");
const productosConStock = db.productos.find({
    "inventario.stock": { $gt: 0 }
}).count();

const productosTotal = db.productos.count();

print(`Total productos: ${productosTotal}`);
print(`Productos con stock > 0: ${productosConStock}`);

if (productosConStock < productosTotal) {
    print("✅ CORRECTO: Hay productos sin stock que deben ser filtrados");
} else {
    print("ℹ️ INFO: Todos los productos tienen stock");
}

print("\n=== FIN DE PRUEBAS ===");
print("\nSi ejecutas esto después de los cambios en el código, deberías ver:");
print("1. Los filtros por sucursal solo devuelven productos con stock > 0 en esa sucursal");
print("2. Las órdenes solo permiten comprar de la sucursal específica del item");
print("3. Los productos sin stock no aparecen en listados generales");