/*
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
*/

CREATE OR REPLACE
    FUNCTION sp_ticket_insertar(
    p_partido_id int,
    p_numero_asiento varchar,
    p_seccion varchar,
    p_precio numeric)RETURNS int AS $$

    DECLARE
        v_id int;

    BEGIN

        IF EXISTS(
            SELECT
                1
            FROM
                ticket
            WHERE
                partido_id = p_partido_id
                AND numero_asiento = p_numero_asiento
        )THEN

            RAISE EXCEPTION 'El asiento % ya existe para este partido',
            p_numero_asiento;

        END IF;

        INSERT INTO ticket(
            partido_id,
            numero_asiento,
            seccion,
            precio,
            estado
        )
        VALUES(
            p_partido_id,
            p_numero_asiento,
            p_seccion,
            p_precio,
            'DISPONIBLE'
        )
        RETURNING id INTO v_id;

        RETURN v_id;

    END;
    
    
---actualizar
    CREATE OR REPLACE
    FUNCTION sp_ticket_actualizar_precio(
    p_id int,
    p_precio numeric)RETURNS boolean AS $$

    DECLARE
        v_filas int;

    BEGIN

        UPDATE ticket
        SET
            precio = p_precio
        WHERE
            id = p_id
            AND estado = 'DISPONIBLE';

        GET DIAGNOSTICS v_filas = ROW_COUNT;

        IF v_filas = 0 THEN

            RAISE EXCEPTION 'No se puede modificar: el ticket no existe o ya fue vendido';

        END IF;

        RETURN TRUE;

    END;

$$
LANGUAGE plpgsql;


---eliminar

CREATE OR REPLACE
    FUNCTION sp_ticket_eliminar(p_id int)RETURNS boolean AS $$

    DECLARE
        v_filas int;

    BEGIN

        DELETE FROM ticket
        WHERE
            id = p_id
            AND estado = 'DISPONIBLE';

        GET DIAGNOSTICS v_filas = ROW_COUNT;

        IF v_filas = 0 THEN

            RAISE EXCEPTION 'No se puede eliminar: el ticket no existe o ya fue vendido';

        END IF;

        RETURN TRUE;

    END;

$$
LANGUAGE plpgsql;

---------tickets id
CREATE OR REPLACE
    FUNCTION sp_ticket_por_id(p_id int)RETURNS TABLE(
    id int,
    partido_id int,
    numero_asiento varchar,
    seccion varchar,
    precio numeric,
    estado varchar) AS $$

    BEGIN

        RETURN query
        SELECT
            t.id,
            t.partido_id,
            t.numero_asiento,
            t.seccion,
            t.precio,
            t.estado
        FROM
            ticket t
        WHERE
            t.id = p_id;

    END;

$$
LANGUAGE plpgsql;

--------tickets partido
CREATE OR REPLACE
    FUNCTION sp_ticket_por_partido(
    p_partido_id int)RETURNS TABLE(
    id int,
    numero_asiento varchar,
    seccion varchar,
    precio numeric,
    estado varchar) AS $$

    BEGIN

        RETURN query
        SELECT
            t.id,
            t.numero_asiento,
            t.seccion,
            t.precio,
            t.estado
        FROM
            ticket t
        WHERE
            t.partido_id = p_partido_id
        ORDER BY
            t.seccion,
            t.numero_asiento;

    END;

$$
LANGUAGE plpgsql;

---tickets disponibles
CREATE OR REPLACE
    FUNCTION sp_ticket_disponibles(
    p_partido_id int,
    p_seccion varchar)RETURNS TABLE(
    id int,
    numero_asiento varchar,
    seccion varchar,
    precio numeric) AS $$

    BEGIN

        RETURN query
        SELECT
            t.id,
            t.numero_asiento,
            t.seccion,
            t.precio
        FROM
            ticket t
        WHERE
            t.partido_id = p_partido_id
            AND t.estado = 'DISPONIBLE'
            AND (
                p_seccion IS NULL
                OR t.seccion = p_seccion
            )
        ORDER BY
            t.numero_asiento;

    END;

$$
LANGUAGE plpgsql;
    