--tabla partido
/*CREATE TABLE partido (
    id               SERIAL PRIMARY KEY,
    equipo_local     VARCHAR(100) NOT NULL,
    equipo_visitante VARCHAR(100) NOT NULL,
    fecha            TIMESTAMP   NOT NULL,
    estadio          VARCHAR(100) NOT NULL,
    ciudad           VARCHAR(100),
    capacidad        INT NOT NULL CHECK (capacidad > 0),
    estado           VARCHAR(20) DEFAULT 'DISPONIBLE'
                     CHECK (estado IN ('DISPONIBLE', 'FINALIZADO', 'CANCELADO')),
    created_en       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
*/

-------------------procedures partidos



------insertar
CREATE OR REPLACE 
    FUNCTION sp_partido_insertar(p_equipo_local varchar, p_equipo_visitante varchar, p_fecha TIMESTAMP,
    p_estadio varchar,  p_ciudad varchar, p_capacidad int, p_estado varchar)
    RETURNS int AS $$ DECLARE v_id int;

BEGIN IF p_equipo_local = p_equipo_visitante 
THEN RAISE EXCEPTION 'El equipo local y visitante no pueden ser el mismo';
end if;

INSERT
    INTO
    partido (
        equipo_local,
        equipo_visitante,
        fecha,
        estadio,
        ciudad,
        capacidad,
        estado
    )
VALUES (
    p_equipo_local,
    p_equipo_visitante,
    p_fecha,
    p_estadio,
    p_ciudad,
    p_capacidad,
    p_estado
) RETURNING  id
INTO
    v_id;

RETURN v_id;
END;

$$
LANGUAGE plpgsql;


---------------actualizar
CREATE OR REPLACE
FUNCTION sp_partido_actualizar(
    p_id int,
    p_equipo_local varchar,
    p_equipo_visitante varchar,
    p_fecha timestamp,
    p_estadio varchar,
    p_ciudad varchar,
    p_capacidad int,
    p_estado varchar
)
RETURNS boolean AS $$
DECLARE
    v_filas int;

BEGIN
    IF NOT EXISTS (
    SELECT
        1
    FROM
        partido
    WHERE
        id = p_id
) THEN
        RAISE EXCEPTION 'No existe el partido con id %',
p_id;
END IF;

IF p_equipo_local = p_equipo_visitante THEN
        RAISE EXCEPTION 'El equipo local y visitante no pueden ser el mismo';
END IF;

UPDATE
    partido
SET
    equipo_local = p_equipo_local,
        equipo_visitante = p_equipo_visitante,
        fecha = p_fecha,
        estadio = p_estadio,
        ciudad = p_ciudad,
        capacidad = p_capacidad,
        estado = p_estado
WHERE
    id = p_id;

GET DIAGNOSTICS v_filas = ROW_COUNT;

RETURN v_filas > 0;
END;

$$ LANGUAGE plpgsql;


/*
---------------eliminar
drop function sp_partido_eliminar();
CREATE OR REPLACE 
    FUNCTION sp_partido_eliminar(p_id int) RETURNS boolean AS $$ DECLARE v_filas int;

BEGIN
--verificar existencia
    IF NOT EXISTS (
    SELECT
        1
    FROM
        partido
    WHERE
        id = p_id
) THEN
        RAISE EXCEPTION 'No existe el partido con id %',
p_id;
END IF;
--validar si ya esta inactivo
IF EXISTS(
    SELECT 1
    FROM partido
    WHERE id = p_id AND estado = 'CANCELADO'
)THEN 
    RAISE EXCEPTION 'El partido con id % fue cancelado' ,
p_id;
END IF;
-- si pasa cambiamos a false
UPDATE
    partido
SET
    estado = 'CANCELADO'
WHERE
    id = p_id;

GET DIAGNOSTICS v_filas = row_count;

RETURN v_filas >0;
END;

$$
    LANGUAGE plpgsql;
*/

--eliminar 2
CREATE OR REPLACE FUNCTION sp_partido_cambiar_estado(
    p_id int,
    p_estado varchar
)
RETURNS BOOLEAN AS $$
DECLARE
    v_filas int;
BEGIN
    --validar que exista
    IF NOT EXISTS (
        SELECT 1 FROM partido
		WHERE id = p_id
    ) THEN
        RAISE EXCEPTION 'El partido con id % no existe', p_id;
    END IF;

    --validar que el estado nuevo sea valido
    IF p_estado NOT IN ('DISPONIBLE', 'FINALIZADO', 'CANCELADO') THEN
        RAISE EXCEPTION 'El estado % no es valido', p_estado;
    END IF;

    --validar que el estado nuevo sea distinto al actual
    IF EXISTS (
        SELECT 1 FROM partido WHERE id = p_id AND estado = p_estado
    ) THEN
        RAISE EXCEPTION 'El partido con id % ya tiene el estado %', p_id, p_estado;
    END IF;

    UPDATE partido
    SET estado = p_estado
    WHERE id = p_id;

    GET DIAGNOSTICS v_filas = ROW_COUNT;
    RETURN v_filas > 0;
END;
$$ 
LANGUAGE plpgsql;
---------buscar todos
CREATE OR REPLACE 
    FUNCTION sp_partido_todos() RETURNS TABLE(
    id int,
    equipo_local varchar,
    equipo_visitante varchar,
    fecha TIMESTAMP,
    estadio varchar,
    ciudad varchar,
    capacidad int,
    estado varchar,
    creado_en TIMESTAMP) AS $$
    BEGIN
        RETURN query
SELECT 
    p.id,
    p.equipo_local,
    p.equipo_visitante,
    p.fecha,
    p.estadio,
    p.ciudad,
    p.capacidad,
    p.estado,
    p.created_en
FROM 
    partido p
ORDER BY
    p.id;
END;

$$
    LANGUAGE plpgsql;
    
-----------------------buscar id
CREATE OR REPLACE
    FUNCTION sp_partido_id(p_id int) RETURNS TABLE(
  id int,
    equipo_local varchar,
    equipo_visitante varchar,
    fecha TIMESTAMP,
    estadio varchar,
    ciudad varchar,
    capacidad int,
    estado varchar,
    creado_en TIMESTAMP) AS $$
    BEGIN
        RETURN query
SELECT 
    p.id,
    p.equipo_local,
    p.equipo_visitante,
    p.fecha,
    p.estadio,
    p.ciudad,
    p.capacidad,
    p.estado,
    p.created_en
FROM 
    partido p
WHERE
    p.id = p_id;
END;
$$
LANGUAGE plpgsql;

----------------bucar por equipos 
CREATE OR REPLACE 
    FUNCTION sp_partido_equipo(p_texto varchar)RETURNS TABLE(
    id int,
    equipo_local varchar,
    equipo_visitante varchar,
    fecha TIMESTAMP,
    estadio varchar,
    ciudad varchar,
    capacidad int,
    estado varchar,
    creado_en TIMESTAMP) AS $$
    
    BEGIN
        RETURN query
        SELECT 
    p.id,
    p.equipo_local,
    p.equipo_visitante,
    p.fecha,
    p.estadio,
    p.ciudad,
    p.capacidad,
    p.estado,
    p.created_en
FROM 
    partido p
WHERE 
    p.equipo_local ILIKE '%' || p_texto || '%'
    OR p.equipo_visitante ILIKE '%' || p_texto || '%'
ORDER BY
    p.equipo_local,
    p.equipo_visitante;
END;

$$
LANGUAGE plpgsql;

SELECT sp_partido_insertar('México', 'Argentina', '2026-06-11 19:00:00', 'Estadio Azteca', 'CDMX', 87000, 'DISPONIBLE');
SELECT * FROM sp_partido_todos();
SELECT * FROM sp_partido_id(1);
SELECT * FROM sp_partido_equipo('méx');
SELECT sp_partido_actualizar(1, 'México', 'Argentina', '2026-06-11 20:00:00', 'Estadio Azteca', 'Ciudad de México', 87000, 'DISPONIBLE');
SELECT sp_partido_eliminar(1);


