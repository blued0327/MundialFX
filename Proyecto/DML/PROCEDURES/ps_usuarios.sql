 
--dml
--procedures
--user

--insert -- devuelve un id para asi saber que id se incerto
    CREATE OR REPLACE
    FUNCTION sp_usuario_insertar( p_username varchar, p_password varchar, p_rol varchar, p_estado boolean )
    RETURNS int AS $$ DECLARE v_id int;
--if para ver si existe
BEGIN IF EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        username = p_username
        --el % marca con p_username
) THEN RAISE EXCEPTION 'El username % ya existe',
p_username;
END IF;

INSERT
    INTO
    usuario (
        username,
        PASSWORD,
        rol,
        estado
    )
VALUES (
    p_username,
    p_password,
    p_rol,
    p_estado
) RETURNING id
INTO
    v_id;

RETURN v_id;
END;

$$
LANGUAGE plpgsql;
/*
--update -- NECESITAMOS ID -- devuelve true si actualizo
CREATE OR REPLACE
    FUNCTION sp_usuario_actualizar(p_id int, p_username varchar, p_password varchar, p_rol varchar, p_estado boolean)
--saber cuantas filas se actualizaron
    RETURNS boolean AS $$ DECLARE v_filas int;

BEGIN 
    UPDATE
    usuario
SET
    username = p_username,
    PASSWORD = p_password,
    rol = p_rol,
    estado = p_estado
WHERE
    id = p_id;
--guarda las que se actualizaron
GET DIAGNOSTICS v_filas = row_count;

RETURN v_filas > 0;
END;

$$ 
LANGUAGE plpgsql;*/



---actualizar 2
-- ACTUALIZAR con validación de existencia
drop function sp_usuario_actualizar;

CREATE OR REPLACE
FUNCTION sp_usuario_actualizar(
    p_id int,
    p_username varchar,
    p_password varchar,
    p_rol varchar,
    p_estado boolean
)
RETURNS boolean AS $$
DECLARE
    v_filas int;

BEGIN
-- Validar que el usuario exista
    IF NOT EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        id = p_id
) THEN
        RAISE EXCEPTION 'El usuario con id % no existe',
p_id;
END IF;
-- Validar que el nuevo username no esté siendo usado por OTRO usuario
    IF EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        username = p_username
        AND id <> p_id
) THEN
        RAISE EXCEPTION 'El username % ya está siendo usado por otro usuario',
p_username;
END IF;

UPDATE
    usuario
SET
    username = p_username,
        PASSWORD = p_password,
        rol = p_rol,
        estado = p_estado
WHERE
    id = p_id;

GET DIAGNOSTICS v_filas = ROW_COUNT;

RETURN v_filas > 0;
END;

$$ LANGUAGE plpgsql;



--actualizar 3

CREATE OR REPLACE
FUNCTION sp_usuario_actualizar(
    p_id int,
    p_username varchar,
    p_password varchar,
    p_rol varchar,
    p_estado boolean
)
RETURNS boolean AS $$
DECLARE
    v_filas int;
    v_rol_actual varchar;
    v_estado_actual boolean;
    v_admins_activos int;
BEGIN
-- Validar que el usuario exista
    IF NOT EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        id = p_id
) THEN
        RAISE EXCEPTION 'El usuario con id % no existe',
p_id;
END IF;
-- Validar que el nuevo username no esté siendo usado por OTRO usuario
    IF EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        username = p_username
        AND id <> p_id
) THEN
        RAISE EXCEPTION 'El username % ya está siendo usado por otro usuario',
p_username;
END IF;
-- Validar que siempre exista al menos un ADMIN activo
    SELECT
        rol,
        estado
    INTO
        v_rol_actual,
        v_estado_actual
    FROM
        usuario
    WHERE
        id = p_id;

    IF v_rol_actual = 'ADMIN' AND v_estado_actual = TRUE THEN
        IF p_rol <> 'ADMIN' OR p_estado = FALSE THEN
            SELECT
                COUNT(*)
            INTO
                v_admins_activos
            FROM
                usuario
            WHERE
                rol = 'ADMIN'
                AND estado = TRUE;

            IF v_admins_activos <= 1 THEN
                RAISE EXCEPTION 'No se puede modificar: debe existir al menos un ADMIN activo en el sistema';
END IF;
END IF;
END IF;
UPDATE
    usuario
SET
    username = p_username,
        PASSWORD = p_password,
        rol = p_rol,
        estado = p_estado
WHERE
    id = p_id;
GET DIAGNOSTICS v_filas = ROW_COUNT;
RETURN v_filas > 0;
END;
$$ LANGUAGE plpgsql;

--delete 1
/*
CREATE OR REPLACE 
    FUNCTION sp_usuario_eliminar(p_id int) RETURNS boolean AS $$ DECLARE v_filas int;

BEGIN 
    UPDATE 
    usuario
SET
    estado = FALSE
WHERE
    id = p_id;

GET DIAGNOSTICS v_filas = ROW_count;

RETURN v_filas >0;
END ;

$$
LANGUAGE plpgsql;*/

--delete 2
-- ELIMINAR con validación de existencia y estado actual
/* Al final no me va a servir este, voy a tener que cambiarlo en todos los que tengan estado
DROP FUNCTION IF EXISTS sp_usuario_eliminar(integer);
CREATE OR REPLACE
FUNCTION sp_usuario_eliminar(p_id INT)
RETURNS BOOLEAN AS $$
DECLARE
    v_filas INT;

BEGIN
-- Validar que el usuario exista
    IF NOT EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        id = p_id
) THEN
        RAISE EXCEPTION 'El usuario con id % no existe',
p_id;
END IF;
-- Validar que no esté ya inactivo 
    IF EXISTS (
    SELECT
        1
    FROM
        usuario
    WHERE
        id = p_id
        AND estado = FALSE
) THEN
        RAISE EXCEPTION 'El usuario con id % ya está inactivo',
p_id;
END IF;

UPDATE
    usuario
SET
    estado = FALSE
WHERE
    id = p_id;

GET DIAGNOSTICS v_filas = ROW_COUNT;

RETURN v_filas > 0;
END;

$$ LANGUAGE plpgsql;

*/
CREATE OR REPLACE FUNCTION sp_usuario_cambiar_estado(
    p_id INT,
    p_estado BOOLEAN
)
RETURNS BOOLEAN AS $$
DECLARE
    v_filas INT;
    v_rol VARCHAR;
    v_admins_activos INT;
BEGIN
    --validar que el usuario exista
    IF NOT EXISTS (
        SELECT 1 FROM usuario WHERE id = p_id
    ) THEN
        RAISE EXCEPTION 'El usuario con id % no existe', p_id;
    END IF;

    --validar que el estado nuevo sea distinto al actual
    IF EXISTS (
        SELECT 1 FROM usuario WHERE id = p_id AND estado = p_estado
    ) THEN
        RAISE EXCEPTION 'El usuario con id % ya tiene ese estado', p_id;
    END IF;

    --obtener el rol del usuario que se va a modificar
    SELECT rol INTO v_rol FROM usuario WHERE id = p_id;

    --si estamos inactivando (p_estado = FALSE) y el usuario es ADMIN,
    --verificar que NO sea el ultimo admin activo
    IF p_estado = FALSE AND v_rol = 'ADMIN' THEN
        SELECT COUNT(*)
        INTO v_admins_activos
        FROM usuario
        WHERE rol = 'ADMIN' AND estado = TRUE;

        IF v_admins_activos <= 1 THEN
            RAISE EXCEPTION 'No se puede inactivar: debe existir al menos un ADMIN activo en el sistema';
        END IF;
    END IF;

    UPDATE usuario
    SET estado = p_estado
    WHERE id = p_id;

    GET DIAGNOSTICS v_filas = ROW_COUNT;
    RETURN v_filas > 0;
END;
$$ LANGUAGE 
plpgsql;


drop function sp_usuario_cambiar_estado;

--select id
CREATE OR REPLACE
FUNCTION sp_usuario_por_id(p_id INT)
--tabla ficticia
RETURNS TABLE(
    id int,
    username varchar,
    PASSWORD varchar,
    rol varchar,
    estado boolean,
    creado_en timestamp
) AS $$
BEGIN
    RETURN QUERY
    SELECT
    u.id,
    u.username,
    u.password,
    u.rol,
    u.estado,
    u.creado_en
FROM
    usuario u
WHERE
    u.id = p_id;
END;

$$ 
LANGUAGE plpgsql;

SELECT sp_usuario_todos();
-------------------------------------------------------------select tods
CREATE OR REPLACE 
FUNCTION sp_usuario_todos() RETURNS TABLE( id int,
    username varchar,
    PASSWORD varchar,
    rol varchar,
    estado boolean,
    creado_en timestamp) AS $$
BEGIN
    RETURN QUERY
    SELECT
    u.id,
    u.username,
    u.password,
    u.rol,
    u.estado,
    u.creado_en
FROM
    usuario u
ORDER BY
    u.id;
--hay que ordenarlo
END;

$$ LANGUAGE plpgsql;


-------------------------------------select username
CREATE OR REPLACE 
    FUNCTION sp_usuario(p_username varchar) 

    RETURNS TABLE (
    id int,
    username varchar,
    PASSWORD varchar,
    rol varchar,
    estado boolean,
    creado_en timestamp
) AS $$ 
    BEGIN   
        RETURN query
        SELECT
        u.id,
        u.username,
        u.PASSWORD,
        u.rol,
        u.estado,
        u.creado_en
FROM
    usuario u
WHERE
    --tuve que agregar que el estado sea true sino es que se elimino
    u.username = p_username
    AND u.estado = TRUE ;
END;

$$ 
LANGUAGE plpgsql;


select sp_usuario_todos();
select *from usuario;
update usuario set 
	estado = 'true'
	where  id = 3;


