-- ============================================
-- SCRIPT DE DATOS DE PRUEBA - E-COMMERCE
-- ============================================
-- 
-- Este script inserta datos de prueba para:
-- 1. Configuraciones del sistema
-- 2. Categorías y Marcas
-- 3. Productos con imágenes
-- 4. Clientes
-- 5. Proveedores
--
-- IMPORTANTE: Se ejecuta DESPUÉS de que Hibernate cree las tablas
-- ============================================
-- ============================================
-- 1. CONFIGURACIONES DEL SISTEMA
-- ============================================
-- Insertar configuraciones básicas (usar INSERT IGNORE para evitar duplicados)
INSERT IGNORE INTO configuraciones (
    clave,
    valor,
    descripcion,
    tipo,
    fecha_creacion,
    fecha_actualizacion
)
VALUES
    (
        'nombre_empresa',
        'Mi E-Commerce',
        'Nombre de la empresa',
        'STRING',
        NOW (),
        NOW ()
    ),
    (
        'telefono',
        '+51 999 888 777',
        'Teléfono de contacto',
        'STRING',
        NOW (),
        NOW ()
    ),
    (
        'email',
        'ventas@miecommerce.com',
        'Email de contacto',
        'EMAIL',
        NOW (),
        NOW ()
    ),
    (
        'direccion',
        'Av. Javier Prado 123, San Isidro, Lima',
        'Dirección física',
        'STRING',
        NOW (),
        NOW ()
    ),
    (
        'whatsapp',
        '51999888777',
        'WhatsApp de contacto',
        'STRING',
        NOW (),
        NOW ()
    ),
    (
        'facebook_url',
        'https://facebook.com/miecommerce',
        'URL de Facebook',
        'STRING',
        NOW (),
        NOW ()
    ),
    (
        'instagram_url',
        'https://instagram.com/miecommerce',
        'URL de Instagram',
        'STRING',
        NOW (),
        NOW ()
    ),
    (
        'texto_bienvenida',
        'Bienvenido a nuestra tienda online. Encuentra los mejores productos al mejor precio.',
        'Texto de bienvenida',
        'TEXT',
        NOW (),
        NOW ()
    ),
    (
        'horario_atencion',
        'Lun-Vie 9:00am-6:00pm, Sáb 9:00am-2:00pm',
        'Horario de atención',
        'STRING',
        NOW (),
        NOW ()
    );

-- ============================================
-- 2. CATEGORÍAS
-- ============================================
INSERT IGNORE INTO categorias (
    nombre,
    descripcion,
    orden,
    activo,
    fecha_creacion
)
VALUES
    (
        'Electrónica',
        'Productos electrónicos y tecnología',
        1,
        TRUE,
        NOW ()
    ),
    (
        'Ropa y Moda',
        'Ropa, zapatos y accesorios',
        2,
        TRUE,
        NOW ()
    ),
    (
        'Hogar y Muebles',
        'Muebles y decoración para el hogar',
        3,
        TRUE,
        NOW ()
    ),
    (
        'Deportes',
        'Artículos deportivos y fitness',
        4,
        TRUE,
        NOW ()
    ),
    (
        'Juguetes',
        'Juguetes y juegos para niños',
        5,
        TRUE,
        NOW ()
    );

-- ============================================
-- 3. MARCAS
-- ============================================
INSERT IGNORE INTO marcas (nombre, descripcion, activo, fecha_creacion)
VALUES
    (
        'Samsung',
        'Tecnología e innovación',
        TRUE,
        NOW ()
    ),
    (
        'LG',
        'Electrodomésticos y electrónica',
        TRUE,
        NOW ()
    ),
    ('Sony', 'Electrónica de consumo', TRUE, NOW ()),
    ('Nike', 'Ropa y calzado deportivo', TRUE, NOW ()),
    ('Adidas', 'Artículos deportivos', TRUE, NOW ()),
    ('Zara', 'Moda y accesorios', TRUE, NOW ()),
    ('IKEA', 'Muebles y decoración', TRUE, NOW ()),
    (
        'Lego',
        'Juguetes y entretenimiento',
        TRUE,
        NOW ()
    );

-- ============================================
-- 4. PRODUCTOS
-- ============================================
-- PRODUCTOS DE ELECTRÓNICA
INSERT IGNORE INTO productos (
    codigo_sku,
    nombre,
    descripcion,
    precio_base,
    precio_oferta,
    stock_actual,
    stock_minimo,
    categoria_id,
    marca_id,
    es_destacado,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
VALUES
    (
        'SKU-ELEC-001',
        'Smart TV Samsung 55"',
        'Televisor Samsung 55 pulgadas 4K UHD Smart TV con HDR y sistema operativo Tizen',
        2499.00,
        1999.00,
        15,
        3,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Samsung'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ELEC-002',
        'Laptop Dell Inspiron 15',
        'Laptop Dell Inspiron 15, Intel Core i5, 8GB RAM, 512GB SSD, Windows 11',
        2800.00,
        NULL,
        8,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Samsung'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ELEC-003',
        'Auriculares Sony WH-1000XM5',
        'Auriculares inalámbricos con cancelación de ruido premium',
        1299.00,
        999.00,
        20,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Sony'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ELEC-004',
        'Refrigeradora LG 420L',
        'Refrigeradora LG No Frost 420 litros con tecnología Inverter',
        3500.00,
        2999.00,
        5,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'LG'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ELEC-005',
        'Tablet Samsung Galaxy Tab S8',
        'Tablet Samsung 11 pulgadas, 8GB RAM, 128GB almacenamiento',
        1899.00,
        NULL,
        12,
        3,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Samsung'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ELEC-006',
        'Smartwatch Samsung Galaxy Watch 5',
        'Reloj inteligente con GPS, monitor de salud y resistencia al agua',
        899.00,
        699.00,
        25,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Samsung'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ELEC-007',
        'Lavadora LG 18kg',
        'Lavadora automática LG 18kg con tecnología TurboDrum',
        1799.00,
        NULL,
        7,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Electrónica'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'LG'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    );

-- PRODUCTOS DE ROPA
INSERT IGNORE INTO productos (
    codigo_sku,
    nombre,
    descripcion,
    precio_base,
    precio_oferta,
    stock_actual,
    stock_minimo,
    categoria_id,
    marca_id,
    es_destacado,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
VALUES
    (
        'SKU-ROPA-001',
        'Zapatillas Nike Air Max',
        'Zapatillas deportivas Nike Air Max para running y casual',
        399.00,
        299.00,
        30,
        8,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Ropa y Moda'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nike'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ROPA-002',
        'Polo Adidas Clásico',
        'Polo deportivo Adidas de algodón, varios colores disponibles',
        89.00,
        NULL,
        50,
        15,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Ropa y Moda'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Adidas'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ROPA-003',
        'Jean Zara Slim Fit',
        'Jean de mezclilla Zara corte slim fit para hombre',
        159.00,
        119.00,
        40,
        10,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Ropa y Moda'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Zara'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ROPA-004',
        'Chaqueta Nike Sportswear',
        'Chaqueta cortaviento Nike con capucha',
        249.00,
        199.00,
        18,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Ropa y Moda'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nike'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-ROPA-005',
        'Zapatillas Adidas Superstar',
        'Zapatillas clásicas Adidas Superstar blancas',
        329.00,
        NULL,
        35,
        10,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Ropa y Moda'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Adidas'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    );

-- PRODUCTOS DE HOGAR
INSERT IGNORE INTO productos (
    codigo_sku,
    nombre,
    descripcion,
    precio_base,
    precio_oferta,
    stock_actual,
    stock_minimo,
    categoria_id,
    marca_id,
    es_destacado,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
VALUES
    (
        'SKU-HOGAR-001',
        'Sofá IKEA Kivik 3 plazas',
        'Sofá moderno IKEA de 3 plazas con funda lavable',
        1899.00,
        1599.00,
        6,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Hogar y Muebles'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'IKEA'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-HOGAR-002',
        'Mesa de Centro IKEA Lack',
        'Mesa de centro IKEA Lack, diseño minimalista',
        149.00,
        NULL,
        15,
        4,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Hogar y Muebles'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'IKEA'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-HOGAR-003',
        'Estante IKEA Billy',
        'Estante librería IKEA Billy blanco, 80x28x202 cm',
        349.00,
        279.00,
        10,
        3,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Hogar y Muebles'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'IKEA'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-HOGAR-004',
        'Cama IKEA Malm Queen',
        'Cama Queen IKEA Malm con cajones de almacenamiento',
        899.00,
        NULL,
        8,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Hogar y Muebles'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'IKEA'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    );

-- PRODUCTOS DE DEPORTES
INSERT IGNORE INTO productos (
    codigo_sku,
    nombre,
    descripcion,
    precio_base,
    precio_oferta,
    stock_actual,
    stock_minimo,
    categoria_id,
    marca_id,
    es_destacado,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
VALUES
    (
        'SKU-DEP-001',
        'Balón Nike Fútbol',
        'Balón de fútbol Nike Strike tamaño oficial',
        89.00,
        69.00,
        45,
        10,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Deportes'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nike'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-DEP-002',
        'Pesas Ajustables 20kg',
        'Set de pesas ajustables para ejercicio en casa',
        299.00,
        NULL,
        20,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Deportes'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nike'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-DEP-003',
        'Bicicleta Estática',
        'Bicicleta estática para ejercicio con monitor digital',
        899.00,
        699.00,
        5,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Deportes'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Adidas'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    );

-- PRODUCTOS DE JUGUETES
INSERT IGNORE INTO productos (
    codigo_sku,
    nombre,
    descripcion,
    precio_base,
    precio_oferta,
    stock_actual,
    stock_minimo,
    categoria_id,
    marca_id,
    es_destacado,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
VALUES
    (
        'SKU-JUG-001',
        'LEGO City Estación de Policía',
        'Set LEGO City con estación de policía y vehículos',
        249.00,
        199.00,
        22,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Juguetes'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Lego'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-JUG-002',
        'LEGO Star Wars Millennium Falcon',
        'Set LEGO Star Wars Millennium Falcon con 1351 piezas',
        699.00,
        NULL,
        8,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Juguetes'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Lego'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW (),
        NOW ()
    ),
    (
        'SKU-JUG-003',
        'LEGO Friends Casa de Stephanie',
        'Set LEGO Friends con casa y figuras',
        179.00,
        149.00,
        18,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Juguetes'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Lego'
            LIMIT
                1
        ),
        FALSE,
        TRUE,
        NOW (),
        NOW ()
    );

-- ============================================
-- 5. CLIENTES DE PRUEBA
-- ============================================
INSERT IGNORE INTO clientes (
    tipo_documento,
    numero_documento,
    nombres,
    apellido_paterno,
    apellido_materno,
    razon_social,
    direccion,
    distrito,
    provincia,
    departamento,
    telefono,
    email,
    activo,
    fecha_registro
)
VALUES
    (
        'DNI',
        '12345678',
        'Juan Carlos',
        'Pérez',
        'García',
        NULL,
        'Av. Arequipa 1234',
        'Miraflores',
        'Lima',
        'Lima',
        '999111222',
        'juan.perez@email.com',
        TRUE,
        NOW ()
    ),
    (
        'DNI',
        '87654321',
        'María Elena',
        'López',
        'Fernández',
        NULL,
        'Jr. Las Flores 567',
        'San Isidro',
        'Lima',
        'Lima',
        '999222333',
        'maria.lopez@email.com',
        TRUE,
        NOW ()
    ),
    (
        'DNI',
        '11223344',
        'Pedro Antonio',
        'Ramírez',
        'Soto',
        NULL,
        'Calle Los Olivos 890',
        'Surco',
        'Lima',
        'Lima',
        '999333444',
        'pedro.ramirez@email.com',
        TRUE,
        NOW ()
    ),
    (
        'RUC',
        '20123456789',
        NULL,
        NULL,
        NULL,
        'EMPRESA DE PRUEBAS S.A.C.',
        'Av. Javier Prado 2000',
        'San Isidro',
        'Lima',
        'Lima',
        '999444555',
        'ventas@empresa1.com',
        TRUE,
        NOW ()
    );

-- ============================================
-- 6. PROVEEDORES
-- ============================================
INSERT IGNORE INTO proveedores (
    ruc,
    razon_social,
    direccion,
    telefono,
    email,
    contacto_nombre,
    contacto_telefono,
    activo,
    fecha_registro
)
VALUES
    (
        '20111222333',
        'DISTRIBUIDORA TECH S.A.C.',
        'Av. Argentina 1500, Callao',
        '014567890',
        'ventas@distribuidoratech.com',
        'Carlos Mendoza',
        '999555666',
        TRUE,
        NOW ()
    ),
    (
        '20444555666',
        'IMPORTACIONES GLOBAL E.I.R.L.',
        'Jr. Comercio 789, Lima',
        '014567891',
        'compras@importacionesglobal.com',
        'Ana Torres',
        '999666777',
        TRUE,
        NOW ()
    ),
    (
        '20777888999',
        'MAYORISTA TEXTIL S.A.',
        'Av. Gamarra 2500, La Victoria',
        '014567892',
        'ventas@mayoristatextil.com',
        'Roberto Silva',
        '999777888',
        TRUE,
        NOW ()
    );

-- ============================================
-- NOTAS IMPORTANTES
-- ============================================
-- 
-- 1. Este script usa INSERT IGNORE para evitar errores si los datos ya existen
-- 2. Las fechas se generan automáticamente con NOW()
-- 3. Los pedidos, ventas y créditos se crean mejor desde la aplicación
--    ya que requieren lógica de negocio compleja
-- 4. Las imágenes de productos deben subirse manualmente
-- 
-- CREDENCIALES DE ACCESO (creadas por InitializationService):
-- Usuario Admin: admin / admin123
-- Usuario Trabajador: trabajador / trabajador123
-- 
-- ============================================
-- FIN DEL SCRIPT
-- ============================================