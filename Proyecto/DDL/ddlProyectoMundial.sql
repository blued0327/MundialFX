CREATE DATABASE Mundial;

--Tabla Usuario
CREATE TABLE usuario (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50)  UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    rol      VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'VENDEDOR')),
    estado   BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


--tabla cliente
CREATE TABLE cliente (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    apellido  VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20),
    email     VARCHAR(100) UNIQUE,
    direccion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--tabla partido
CREATE TABLE partido (
    id               SERIAL PRIMARY KEY,
    equipo_local     VARCHAR(100) NOT NULL,
    equipo_visitante VARCHAR(100) NOT NULL,
    fecha            TIMESTAMP   NOT NULL,
    estadio          VARCHAR(100) NOT NULL,
    ciudad           VARCHAR(100),
    capacidad        INT NOT NULL CHECK (capacidad > 0),
    estado           VARCHAR(20) DEFAULT 'DISPONIBLE'
                     CHECK (estado IN ('DISPONIBLE', 'FINALIZADO', 'CANCELADO')),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--tabla ticket
CREATE TABLE ticket (
    id             SERIAL PRIMARY KEY,
    partido_id     INT NOT NULL,
    numero_asiento VARCHAR(10) NOT NULL,
    seccion        VARCHAR(20) NOT NULL
                   CHECK (seccion IN ('VIP', 'PREFERENCIAL', 'GENERAL')),
    precio         NUMERIC(10,2) NOT NULL CHECK (precio > 0),
    estado         VARCHAR(20) DEFAULT 'DISPONIBLE'
                   CHECK (estado IN ('DISPONIBLE', 'VENDIDO', 'RESERVADO')),

    CONSTRAINT fk_ticket_partido
        FOREIGN KEY (partido_id) REFERENCES partido(id) ON DELETE CASCADE,
    CONSTRAINT unique_asiento_partido
        UNIQUE (partido_id, numero_asiento)
);

--venta
CREATE TABLE venta (
    id          SERIAL PRIMARY KEY,
    fecha       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cliente_id  INT,
    usuario_id  INT,
    subtotal    NUMERIC(10,2) NOT NULL DEFAULT 0,
    descuento   NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_iva   NUMERIC(10,2) NOT NULL DEFAULT 0,
    total       NUMERIC(10,2) NOT NULL CHECK (total >= 0),
    anulada     BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL,
    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL
);

--detalle venta
CREATE TABLE detalle_venta (
    id        SERIAL PRIMARY KEY,
    venta_id  INT NOT NULL,
    ticket_id INT NOT NULL,
    precio    NUMERIC(10,2) NOT NULL,
    iva       NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_detalle_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_ticket
        FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE,
    CONSTRAINT unique_ticket_vendido
        UNIQUE (ticket_id)
);

--auditoria

CREATE TABLE auditoria_acceso (
    id         SERIAL PRIMARY KEY,
    usuario_id INT,
    fecha      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion     VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, LOGIN_FALLIDO
    ip         VARCHAR(45),

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL
);

--quizas()
/*
CREATE TABLE precio_seccion (
    id      SERIAL PRIMARY KEY,
    seccion VARCHAR(20) NOT NULL UNIQUE
             CHECK (seccion IN ('VIP', 'PREFERENCIAL', 'GENERAL')),
    precio  NUMERIC(10,2) NOT NULL CHECK (precio > 0)
);

-- Datos base
INSERT INTO precio_seccion (seccion, precio) VALUES
    ('VIP', 500.00),
    ('PREFERENCIAL', 250.00),
    ('GENERAL', 100.00);

*/

CREATE TABLE anulacion_venta (
    id        SERIAL PRIMARY KEY,
    venta_id  INT NOT NULL,
    usuario_id INT,
    fecha     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo    TEXT,

    CONSTRAINT fk_anulacion_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_anulacion_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL
);


	
