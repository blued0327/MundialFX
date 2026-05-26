--PROCEDURES CLIENTES


/*
--tabla cliente
CREATE TABLE cliente (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    apellido  VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20),
    email     VARCHAR(100) UNIQUE,
    direccion TEXT,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
DROP TABLE IF EXISTS partido CASCADE;

*/
-------------------------------------------------------------------insert


    CREATE OR REPLACE
    FUNCTION sp_cliente_insertar( p_nombre varchar, p_apellido varchar, p_telefono varchar, p_email varchar, p_direccion text ) 
RETURNS int AS $$ DECLARE v_id int;
--validamos por email ya que es unico
BEGIN IF p_email IS NOT NULL
AND EXISTS (
    SELECT
        1
    FROM
        cliente
    WHERE
        email = p_email
) THEN RAISE EXCEPTION 'El email % ya está registrado',
p_email;
END IF;

INSERT
    INTO
    cliente (
        nombre,
        apellido,
        telefono,
        email,
        direccion
    )
VALUES (
    p_nombre,
    p_apellido,
    p_telefono,
    p_email,
    p_direccion
) RETURNING id
INTO
    v_id;

RETURN v_id;
END;

$$ 
LANGUAGE plpgsql;
---------------------------------------------------------actualizar

/*
CREATE OR REPLACE 
    FUNCTION sp_actualizar_cliente(p_id int, p_nombre varchar, p_apellido varchar, p_telefono varchar, 
    p_email varchar, p_direccion text ) RETURNS boolean AS $$ DECLARE v_filas int;

BEGIN
--verificar existencia
    IF NOT EXISTS(
    SELECT 1 FROM cliente 
    WHERE id = p_id

)THEN RAISE EXCEPTION 'El cliente con id % no existe',
p_id;
END IF;
-- Validación 2: el nuevo email no debe pisar el de otro cliente
    IF p_email IS NOT NULL
AND EXISTS (
    SELECT
        1
    FROM
        cliente
    WHERE
        email = p_email
        AND id <> p_id
) THEN RAISE EXCEPTION 'El email % ya está registrado por otro cliente',
p_email;
END IF;
--el update
UPDATE
    cliente
SET
    nombre = p_nombre,
    apellido = p_apellido,
    telefono = p_telefono,
    email = p_email,
    direccion = p_direccion
WHERE
    id = p_id ;
  
    GET DIAGNOSTICS v_filas = ROW_COUNT;

RETURN v_filas > 0;
END;

$$
LANGUAGE plpgsql;
*/

--------------------

------------eliminar
CREATE OR REPLACE FUNCTION sp_cliente_eliminar(p_id INT)
RETURNS BOOLEAN AS $$
DECLARE
    v_filas INT;
BEGIN
    --  Validar que el cliente exista en la tabla
    IF NOT EXISTS (
        SELECT 1 
        FROM cliente 
        WHERE id = p_id
    ) THEN
        RAISE EXCEPTION 'El cliente con id % no existe', p_id;
    END IF;

    -- Validar que no esté ya inactivo 
    IF EXISTS (
        SELECT 1 
        FROM cliente 
        WHERE id = p_id AND estado = FALSE
    ) THEN
        RAISE EXCEPTION 'El cliente con id % ya está inactivo', p_id;
    END IF;

    -- Si pasa las validaciones, hacemos el borrado 
    UPDATE cliente
    SET estado = FALSE
    WHERE id = p_id;

    
    GET DIAGNOSTICS v_filas = ROW_COUNT;

    RETURN v_filas > 0;
END;
$$ 
LANGUAGE plpgsql;


-------mostrar todos
CREATE OR REPLACE 
    FUNCTION sp_clientes_todos() RETURNS TABLE(
    id int,
    nombre varchar,
    apellido varchar,
    telefono varchar,
    email varchar,
    direccion text,
     creado_en TIMESTAMP ) AS $$
     
     BEGIN
         RETURN query
         SELECT 
         c.id,
         c.nombre,
         c.apellido,
         c.telefono,
         c.email,
         c.direccion,
         c.creado_en
FROM
    cliente c
ORDER BY
    c.id;
END;
$$
LANGUAGE plpgsql;

----------mostrar por id
CREATE OR REPLACE 
    FUNCTION sp_cliente_id(p_id int) RETURNS TABLE(
    id int,
    nombre varchar,
    apellido varchar,
    telefono varchar,
    email varchar,
    direccion text,
    creado_en TIMESTAMP ) AS $$
     
     BEGIN
         RETURN query
         SELECT 
         c.id,
         c.nombre,
         c.apellido,
         c.telefono,
         c.email,
         c.direccion,
         c.creado_en
FROM
    cliente c
WHERE
    c.id = p_id;
END;

$$
LANGUAGE plpgsql;

----buscar por nombre -- ya que aqui los nombres varian vamos a usar like
CREATE OR REPLACE
FUNCTION sp_cliente_buscar_nombre(p_texto varchar) RETURNS TABLE(
    id int,
    nombre varchar,
    apellido varchar,
    telefono varchar,
    email varchar,
    direccion text,
    creado_en TIMESTAMP ) AS $$
 
BEGIN RETURN QUERY
SELECT
    c.id,
    c.nombre,
    c.apellido,
    c.telefono,
    c.email,
    c.direccion,
    c.creado_en
FROM
    cliente c
WHERE
    --ILIKE es la misma madre que like pero sin case sensitive(mejor asi para no importe como este ingresado lo encuentre 
    ---igualmente luego validare que solo se pueda meter de ciertas formas)
    c.nombre ILIKE '%' || p_texto || '%' 
    OR c.apellido ILIKE '%' || p_texto || '%'
ORDER BY
    c.apellido,
    c.nombre;
END;

$$ LANGUAGE plpgsql;

--selects
SELECT sp_cliente_insertar('Carlos', 'Mendoza', '555-1234', 'carlos@email.com', 'Calle Flores 123');
SELECT sp_cliente_insertar('María Ana', 'Gómez', '555-5678', 'maria@email.com', 'Av. Central 456');

--
SELECT * FROM sp_clientes_todos();

SELECT * FROM sp_cliente_id(1);

-- Prueba A: Buscando "los" en minúsculas (Debería encontrar a CarLOS Mendoza)
SELECT * FROM sp_cliente_buscar_nombre('los');

-- Prueba B: Buscando "GÓMEZ" completamente en mayúsculas (Debería encontrar a María Ana Gómez)
SELECT * FROM sp_cliente_buscar_nombre('GÓMEZ');

-- Prueba C: Buscando "Ana" que está en medio del nombre (Debería encontrar a María Ana Gómez)
SELECT * FROM sp_cliente_buscar_nombre('Ana');

--devuelve true
SELECT sp_actualizar_cliente(3, 'Carlos', 'Mendoza', '999-0000', 'carlos@email.com', 'Calle Flores 123');

SELECT *FROM cliente;






