-- ============================================
-- DATOS DE PRUEBA - TIENDA GAMER E-COMMERCE
-- ============================================
-- 
-- ⚠️ IMPORTANTE: Las CONFIGURACIONES se crean en InitializationService.java
-- Este archivo solo contiene: Categorías, Marcas, Productos, Clientes, Proveedores
-- ============================================
-- ============================================
-- 1. CATEGORÍAS
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
        'Videojuegos',
        'Juegos para PS5, Xbox, Nintendo Switch y PC',
        1,
        TRUE,
        NOW()
    ),
    (
        'Sillas Gaming',
        'Sillas ergonómicas para largas sesiones de juego',
        2,
        TRUE,
        NOW()
    ),
    (
        'Funkos',
        'Figuras coleccionables Funko Pop de videojuegos y anime',
        3,
        TRUE,
        NOW()
    ),
    (
        'Camisetas Gaming',
        'Ropa gamer y merchandising oficial',
        4,
        TRUE,
        NOW()
    ),
    (
        'Periféricos Gaming',
        'Teclados, mouses, audífonos y accesorios RGB',
        5,
        TRUE,
        NOW()
    ),
    (
        'Consolas',
        'Consolas PlayStation, Xbox y Nintendo',
        6,
        TRUE,
        NOW()
    );

-- ============================================
-- 2. MARCAS
-- ============================================
INSERT IGNORE INTO marcas (nombre, descripcion, activo, fecha_creacion)
VALUES
    -- Consolas y Videojuegos
    (
        'PlayStation',
        'Consolas y juegos PlayStation',
        TRUE,
        NOW()
    ),
    ('Xbox', 'Consolas y juegos Xbox', TRUE, NOW()),
    (
        'Nintendo',
        'Consolas y juegos Nintendo',
        TRUE,
        NOW()
    ),
    -- Funkos
    (
        'Funko Pop',
        'Figuras coleccionables oficiales',
        TRUE,
        NOW()
    ),
    -- Sillas Gaming
    (
        'DXRacer',
        'Sillas gaming profesionales',
        TRUE,
        NOW()
    ),
    (
        'Secretlab',
        'Sillas gaming premium',
        TRUE,
        NOW()
    ),
    (
        'Razer',
        'Periféricos y sillas gaming',
        TRUE,
        NOW()
    ),
    -- Periféricos
    (
        'Logitech',
        'Periféricos gaming de alta calidad',
        TRUE,
        NOW()
    ),
    (
        'HyperX',
        'Audífonos y periféricos gaming',
        TRUE,
        NOW()
    ),
    (
        'Corsair',
        'Componentes y periféricos gaming',
        TRUE,
        NOW()
    ),
    (
        'SteelSeries',
        'Periféricos gaming profesionales',
        TRUE,
        NOW()
    ),
    -- Componentes PC
    ('NVIDIA', 'Tarjetas gráficas', TRUE, NOW()),
    (
        'AMD',
        'Procesadores y tarjetas gráficas',
        TRUE,
        NOW()
    ),
    ('Intel', 'Procesadores para gaming', TRUE, NOW());

-- ============================================
-- 3. PRODUCTOS - VIDEOJUEGOS
-- ============================================
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
    -- PS5
    (
        'SKU-VJ-001',
        'The Last of Us Part II Remastered PS5',
        'Obra maestra remasterizada para PS5 con gráficos mejorados y modo roguelike',
        249.00,
        199.00,
        25,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Videojuegos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'PlayStation'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-VJ-002',
        'God of War Ragnarök PS5',
        'Kratos y Atreus enfrentan el apocalipsis nórdico en esta épica aventura',
        279.00,
        NULL,
        30,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Videojuegos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'PlayStation'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-VJ-003',
        'Spider-Man 2 PS5',
        'Peter Parker y Miles Morales unen fuerzas contra Venom',
        299.00,
        249.00,
        20,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Videojuegos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'PlayStation'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    -- Xbox
    (
        'SKU-VJ-006',
        'Starfield Xbox Series X',
        'RPG espacial de Bethesda, explora el universo',
        279.00,
        229.00,
        22,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Videojuegos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Xbox'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    -- Nintendo Switch
    (
        'SKU-VJ-009',
        'The Legend of Zelda: Tears of the Kingdom',
        'Secuela de Breath of the Wild, la mejor aventura de Switch',
        289.00,
        NULL,
        35,
        8,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Videojuegos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nintendo'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-VJ-010',
        'Super Mario Bros Wonder',
        'Mario en 2D como nunca antes, innovación pura',
        259.00,
        219.00,
        40,
        10,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Videojuegos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nintendo'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    );

-- ============================================
-- 4. PRODUCTOS - SILLAS GAMING
-- ============================================
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
        'SKU-SG-001',
        'DXRacer Formula Series Negro/Rojo',
        'Silla gaming ergonómica con soporte lumbar y cojín cervical, reclinable hasta 135°',
        899.00,
        749.00,
        8,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Sillas Gaming'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'DXRacer'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-SG-002',
        'Secretlab Titan Evo 2024',
        'La mejor silla gaming del mercado, con tecnología NEO Hybrid',
        1499.00,
        NULL,
        5,
        1,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Sillas Gaming'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Secretlab'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-SG-003',
        'Razer Iskur X RGB',
        'Silla gaming con iluminación RGB Chroma y diseño ergonómico',
        1299.00,
        1099.00,
        6,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Sillas Gaming'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Razer'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    );

-- ============================================
-- 5. PRODUCTOS - FUNKOS
-- ============================================
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
        'SKU-FK-001',
        'Funko Pop Master Chief (Halo)',
        'Figura coleccionable del icónico Spartan de Halo, 10cm',
        79.00,
        59.00,
        50,
        10,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Funkos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Funko Pop'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-FK-002',
        'Funko Pop Kratos (God of War)',
        'El Dios de la Guerra en versión chibi, incluye Leviathan Axe',
        79.00,
        NULL,
        45,
        10,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Funkos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Funko Pop'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-FK-003',
        'Funko Pop Link (Zelda TOTK)',
        'Link de Tears of the Kingdom con Master Sword',
        89.00,
        69.00,
        40,
        8,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Funkos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Funko Pop'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-FK-004',
        'Funko Pop Pikachu (Pokémon)',
        'El Pokémon más famoso en versión Funko Pop',
        75.00,
        NULL,
        60,
        15,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Funkos'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Funko Pop'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    );

-- ============================================
-- 6. PRODUCTOS - PERIFÉRICOS GAMING
-- ============================================
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
        'SKU-PG-001',
        'Teclado Razer BlackWidow V4 Pro',
        'Teclado mecánico RGB con switches Green, reposa muñecas magnético',
        899.00,
        749.00,
        15,
        3,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Periféricos Gaming'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Razer'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-PG-002',
        'Mouse Logitech G Pro X Superlight 2',
        'Mouse inalámbrico ultraligero para esports, 80 horas de batería',
        649.00,
        NULL,
        20,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Periféricos Gaming'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Logitech'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-PG-003',
        'Audífonos HyperX Cloud III',
        'Audífonos gaming con sonido 7.1 surround y micrófono desmontable',
        449.00,
        379.00,
        25,
        5,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Periféricos Gaming'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'HyperX'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    );

-- ============================================
-- 7. PRODUCTOS - CONSOLAS
-- ============================================
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
        'SKU-CN-001',
        'PlayStation 5 Slim Digital',
        'Consola PS5 edición Slim sin lector de discos, 1TB SSD',
        2499.00,
        2299.00,
        10,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Consolas'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'PlayStation'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-CN-002',
        'PlayStation 5 Slim Standard',
        'Consola PS5 Slim con lector de discos, 1TB SSD',
        2799.00,
        NULL,
        8,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Consolas'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'PlayStation'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-CN-003',
        'Xbox Series X',
        'Consola Xbox Series X, 1TB, 4K 120fps',
        2699.00,
        2499.00,
        12,
        3,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Consolas'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Xbox'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'SKU-CN-005',
        'Nintendo Switch OLED Zelda Edition',
        'Switch OLED edición limitada de Zelda TOTK',
        1899.00,
        1699.00,
        6,
        2,
        (
            SELECT
                id
            FROM
                categorias
            WHERE
                nombre = 'Consolas'
            LIMIT
                1
        ),
        (
            SELECT
                id
            FROM
                marcas
            WHERE
                nombre = 'Nintendo'
            LIMIT
                1
        ),
        TRUE,
        TRUE,
        NOW(),
        NOW()
    );

-- ============================================
-- 8. CLIENTES DE PRUEBA
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
        'Carlos Alberto',
        'Gómez',
        'Ramírez',
        NULL,
        'Av. Benavides 2500',
        'Miraflores',
        'Lima',
        'Lima',
        '999111222',
        'carlos.gamer@email.com',
        TRUE,
        NOW()
    ),
    (
        'DNI',
        '87654321',
        'María Fernanda',
        'Torres',
        'Silva',
        NULL,
        'Jr. Puno 450',
        'San Isidro',
        'Lima',
        'Lima',
        '999222333',
        'maria.gamer@email.com',
        TRUE,
        NOW()
    ),
    (
        'RUC',
        '20123456789',
        NULL,
        NULL,
        NULL,
        'ESPORTS PERÚ S.A.C.',
        'Av. Javier Prado 2500',
        'San Isidro',
        'Lima',
        'Lima',
        '999444555',
        'ventas@esportsperu.com',
        TRUE,
        NOW()
    );

-- ============================================
-- 9. PROVEEDORES
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
        'DISTRIBUIDORA GAMER TECH S.A.C.',
        'Av. Venezuela 1500, Callao',
        '014567890',
        'ventas@gamertechperu.com',
        'Roberto Díaz',
        '999555666',
        TRUE,
        NOW()
    ),
    (
        '20444555666',
        'IMPORTACIONES GAMING PERÚ E.I.R.L.',
        'Jr. Comercio 789, Lima',
        '014567891',
        'compras@gamingperu.com',
        'Sandra Morales',
        '999666777',
        TRUE,
        NOW()
    );

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
-- ✅ Base de datos lista con productos GAMER
-- ⚠️ Las configuraciones se crean en InitializationService.java
-- ============================================