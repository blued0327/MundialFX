-----Cambios en el proyecto
----nos falta nit y metodo de pago
---- voy a poner que si se paga con targeta se tiene un 2% mas de comsion
---- tengo que modificar la tabla ventas
----- tengo que modificar el  actualizar sp_venta_registrar + actualizar sp_venta_obtener_recibo y sp_venta_listar_todas
----- en teoria solo esas igual ire revisando en el el futuro que ya tengo muchos sps
-- luego pues tocar la parte de java


---cambios a ventas
-- pondre como default CF asi por si no quiere nit no ponemos nada
ALTER TABLE venta ADD COLUMN IF NOT EXISTS nit VARCHAR(20) DEFAULT 'CF';
--venta como default efectivo
ALTER TABLE venta ADD COLUMN IF NOT EXISTS metodo_pago VARCHAR(20) DEFAULT 'EFECTIVO';
--añadir una comision
ALTER TABLE venta ADD COLUMN IF NOT EXISTS comision NUMERIC(10,2) DEFAULT 0.00;


ALTER TABLE venta DROP CONSTRAINT IF EXISTS venta_metodo_pago_check;
ALTER TABLE venta ADD CONSTRAINT venta_metodo_pago_check
    CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA'));

--Acctualizar las ventas viejas (poner CF y EFECTIVO en las que no tienen)
UPDATE venta SET nit = 'CF' WHERE nit IS NULL;

UPDATE venta SET metodo_pago = 'EFECTIVO' WHERE metodo_pago IS NULL;

UPDATE venta SET comision = 0.00 WHERE comision IS NULL;


---- sps
--------------registrar venta
1️⃣ sp_venta_registrar (de 7 a 10 parámetros)
-- primero dropeamos la version vieja porque cambia la firma
DROP FUNCTION IF EXISTS public.sp_venta_registrar(integer, integer, integer[], numeric, numeric, numeric, numeric);

CREATE OR REPLACE FUNCTION public.sp_venta_registrar(
    p_cliente_id    integer,
    p_usuario_id    integer,
    p_ticket_ids    integer[],
    p_subtotal      numeric,
    p_descuento     numeric,
    p_total_iva     numeric,
    p_total         numeric,
    p_nit           varchar,
    p_metodo_pago   varchar,
    p_comision      numeric
)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

    DECLARE
        v_venta_id int;
        v_ticket_id int;
        v_precio numeric;
        v_iva_unit numeric;
        v_count int;
        v_numero_factura varchar;

    BEGIN

        ---------------- validar tickets disponibles
        SELECT
            COUNT(*)
        INTO
            v_count
        FROM
            ticket
        WHERE
            id = ANY(p_ticket_ids)
            AND estado = 'DISPONIBLE';

        IF v_count <> array_length(p_ticket_ids, 1) THEN
            RAISE EXCEPTION 'Uno o más tickets no están disponibles';
        END IF;

        ---------------- si el nit viene vacio o nulo lo guardamos como CF (consumidor final)
        IF p_nit IS NULL OR TRIM(p_nit) = '' THEN
            p_nit := 'CF';
        END IF;

        ---------------- generar numero de factura
        v_numero_factura := 'FAC-' || LPAD(nextval('seq_factura')::text, 8, '0');

        ---------------- insertar venta con los campos nuevos: nit, metodo_pago y comision
        INSERT INTO venta(
            cliente_id,
            usuario_id,
            subtotal,
            descuento,
            total_iva,
            total,
            numero_factura,
            nit,
            metodo_pago,
            comision
        )
        VALUES(
            p_cliente_id,
            p_usuario_id,
            p_subtotal,
            p_descuento,
            p_total_iva,
            p_total,
            v_numero_factura,
            p_nit,
            p_metodo_pago,
            p_comision
        )
        RETURNING id INTO v_venta_id;

        ---------------- insertar detalle
        --el trigger trg_ticket_vendido se encarga de marcar el ticket como VENDIDO
        FOREACH v_ticket_id IN ARRAY p_ticket_ids
        LOOP
            SELECT
                precio
            INTO
                v_precio
            FROM
                ticket
            WHERE
                id = v_ticket_id;

            v_iva_unit := v_precio * 0.12;

            INSERT INTO detalle_venta(
                venta_id,
                ticket_id,
                precio,
                iva
            )
            VALUES(
                v_venta_id,
                v_ticket_id,
                v_precio,
                v_iva_unit
            );
        END LOOP;

        RETURN v_venta_id;
    END;
$BODY$;

ALTER FUNCTION public.sp_venta_registrar(integer, integer, integer[], numeric, numeric, numeric, numeric, varchar, varchar, numeric)
    OWNER TO neondb_owner;



------obtener recibo
DROP FUNCTION IF EXISTS public.sp_venta_obtener_recibo(integer);

CREATE OR REPLACE FUNCTION public.sp_venta_obtener_recibo(
    p_venta_id integer
)
    RETURNS TABLE(
        recibo_nro integer,
        numero_factura character varying,
        fecha timestamp without time zone,
        cliente_nombre text,
        cliente_email character varying,
        vendedor character varying,
        partido text,
        fecha_partido timestamp without time zone,
        estadio character varying,
        numero_asiento character varying,
        seccion character varying,
        precio numeric,
        iva numeric,
        subtotal numeric,
        descuento numeric,
        total_iva numeric,
        total numeric,
        --campos nuevos para el recibo
        nit character varying,
        metodo_pago character varying,
        comision numeric
    )
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$

    BEGIN

        RETURN query
        SELECT
            v.id,
            v.numero_factura,
            v.fecha,
            (c.nombre || ' ' || c.apellido)::text,
            c.email,
            u.username,
            (p.equipo_local || ' vs ' || p.equipo_visitante)::text,
            p.fecha,
            p.estadio,
            t.numero_asiento,
            t.seccion,
            dv.precio,
            dv.iva,
            v.subtotal,
            v.descuento,
            v.total_iva,
            v.total,
            --campos nuevos
            v.nit,
            v.metodo_pago,
            v.comision
        FROM
            venta v
        JOIN cliente c
            ON v.cliente_id = c.id
        JOIN usuario u
            ON v.usuario_id = u.id
        JOIN detalle_venta dv
            ON dv.venta_id = v.id
        JOIN ticket t
            ON dv.ticket_id = t.id
        JOIN partido p
            ON t.partido_id = p.id
        WHERE
            v.id = p_venta_id
        ORDER BY
            t.seccion,
            t.numero_asiento;

    END;

$BODY$;

ALTER FUNCTION public.sp_venta_obtener_recibo(integer)
    OWNER TO neondb_owner;

---------------listar todas
DROP FUNCTION IF EXISTS public.sp_venta_listar_todas();

CREATE OR REPLACE FUNCTION public.sp_venta_listar_todas()
    RETURNS TABLE(
        id integer,
        numero_factura character varying,
        fecha timestamp without time zone,
        cliente_nombre text,
        vendedor character varying,
        total numeric,
        anulada boolean,
        --campos nuevos
        nit character varying,
        metodo_pago character varying,
        comision numeric
    )
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
BEGIN
    RETURN QUERY
    SELECT
        v.id,
        v.numero_factura,
        v.fecha,
        --juntamos nombre y apellido en uno solo
        (c.nombre || ' ' || c.apellido)::text,
        u.username,
        v.total,
        v.anulada,
        --campos nuevos
        v.nit,
        v.metodo_pago,
        v.comision
    FROM
        venta v
    LEFT JOIN cliente c ON v.cliente_id = c.id
    LEFT JOIN usuario u ON v.usuario_id = u.id
    ORDER BY v.fecha DESC;
END;
$BODY$;

ALTER FUNCTION public.sp_venta_listar_todas()
    OWNER TO neondb_owner;

--buscar factura
DROP FUNCTION IF EXISTS public.sp_venta_buscar_factura(varchar);

CREATE OR REPLACE FUNCTION public.sp_venta_buscar_factura(
    p_numero_factura varchar
)
    RETURNS TABLE(
        id integer,
        numero_factura character varying,
        fecha timestamp without time zone,
        cliente_nombre text,
        vendedor character varying,
        total numeric,
        anulada boolean,
        nit character varying,
        metodo_pago character varying,
        comision numeric
    )
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000
AS $BODY$
BEGIN
    RETURN QUERY
    SELECT
        v.id,
        v.numero_factura,
        v.fecha,
        (c.nombre || ' ' || c.apellido)::text,
        u.username,
        v.total,
        v.anulada,
        v.nit,
        v.metodo_pago,
        v.comision
    FROM
        venta v
    LEFT JOIN cliente c ON v.cliente_id = c.id
    LEFT JOIN usuario u ON v.usuario_id = u.id
    WHERE
        v.numero_factura ILIKE '%' || p_numero_factura || '%'
    ORDER BY v.fecha DESC;
END;
$BODY$;

ALTER FUNCTION public.sp_venta_buscar_factura(varchar)
    OWNER TO neondb_owner;



SELECT * FROM sp_venta_listar_todas() LIMIT 5;



------------tengo un problema con disponibles
-- ver primero cuantos tickets estan inconsistentes (vendidos pero marcados DISPONIBLE)
SELECT COUNT(*) AS tickets_inconsistentes
FROM ticket t
JOIN detalle_venta dv ON dv.ticket_id = t.id
WHERE t.estado <> 'VENDIDO';

-- arreglarlos
UPDATE ticket
SET estado = 'VENDIDO'
WHERE id IN (
    SELECT DISTINCT dv.ticket_id
    FROM detalle_venta dv
    JOIN ticket t ON t.id = dv.ticket_id
    WHERE t.estado <> 'VENDIDO'
);
-- ver si el trigger existe y esta habilitado
SELECT
    tgname AS nombre_trigger,
    tgrelid::regclass AS tabla,
    tgenabled AS habilitado,
    pg_get_triggerdef(oid) AS definicion
FROM pg_trigger
WHERE tgname = 'trg_ticket_vendido'


--- dejar la db en 0
backup rapido

--hcaer el truncate
BEGIN;

-- 1. truncar todas las tablas con cascade
--    RESTART IDENTITY resetea los SERIAL (columnas id) a 1
--    CASCADE se encarga de respetar las foreign keys
TRUNCATE TABLE
    log_usuario,
    detalle_venta,
    venta,
    ticket,
    partido,
    cliente,
    usuario
RESTART IDENTITY CASCADE;

-- 2. resetear secuencias "sueltas" (las que no son SERIAL)
--    seq_factura es la que genera los FAC-00000001
ALTER SEQUENCE seq_factura RESTART WITH 1;

-- 3. insertar el usuario admin inicial para poder loguearse despues del wipe
--    usuario: admin
--    password: admin123 (o la que hayas elegido al generar el hash)
INSERT INTO usuario (username, password, rol, estado)
VALUES (
    'admin_jmarin',
    '$2a$12$BAd7qzvpjzER77ZBR2cGbeFjUN9m9ReA/xPeXscdwCfzdOjzqs5Rm',-- el hash lo saque de el proyecto que se llama PruebaHash o algo asi
    'ADMIN',
    true
);

COMMIT;


-- VERIFICACIONES para saber si todo quedo bien


SELECT 'usuarios:' AS tabla, COUNT(*) AS filas FROM usuario
UNION ALL
SELECT 'clientes:',         COUNT(*) FROM cliente
UNION ALL
SELECT 'partidos:',         COUNT(*) FROM partido
UNION ALL
SELECT 'tickets:',          COUNT(*) FROM ticket
UNION ALL
SELECT 'ventas:',           COUNT(*) FROM venta
UNION ALL
SELECT 'detalle_venta:',    COUNT(*) FROM detalle_venta
UNION ALL
SELECT 'logs:',             COUNT(*) FROM log_usuario;

-- siguiente valor de las secuencias (debe estar en 1)
SELECT 'seq_factura siguiente:', last_value FROM seq_factura;



truncate table ticket restart identity cascade;
truncate table venta restart identity cascade;
truncate table partido restart identity cascade;


CREATE OR REPLACE FUNCTION public.sp_ticket_generar_masivo(
    p_partido_id integer,
    p_cant_vip integer,
    p_precio_vip numeric,
    p_cant_pref integer,
    p_precio_pref numeric,
    p_cant_gen integer,
    p_precio_gen numeric)
    RETURNS integer
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    v_max_vip   int;
    v_max_pref  int;
    v_max_gen   int;
    v_total     int := 0;
    i           int;
BEGIN
    LOCK TABLE ticket IN EXCLUSIVE MODE;

    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_asiento FROM POSITION('-' IN numero_asiento) + 1) AS INT)), 0)
    INTO v_max_vip
    FROM ticket WHERE partido_id = p_partido_id AND seccion = 'VIP';

    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_asiento FROM POSITION('-' IN numero_asiento) + 1) AS INT)), 0)
    INTO v_max_pref
    FROM ticket WHERE partido_id = p_partido_id AND seccion = 'PREFERENCIAL';

    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_asiento FROM POSITION('-' IN numero_asiento) + 1) AS INT)), 0)
    INTO v_max_gen
    FROM ticket WHERE partido_id = p_partido_id AND seccion = 'GENERAL';

    FOR i IN 1..p_cant_vip LOOP
        INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
        VALUES (p_partido_id, 'VIP-' || LPAD((v_max_vip + i)::TEXT, 4, '0'), 'VIP', p_precio_vip, 'DISPONIBLE')
        ON CONFLICT ON CONSTRAINT unique_asiento_partido DO NOTHING;
        v_total := v_total + 1;
    END LOOP;

    FOR i IN 1..p_cant_pref LOOP
        INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
        VALUES (p_partido_id, 'PREF-' || LPAD((v_max_pref + i)::TEXT, 4, '0'), 'PREFERENCIAL', p_precio_pref, 'DISPONIBLE')
        ON CONFLICT ON CONSTRAINT unique_asiento_partido DO NOTHING;
        v_total := v_total + 1;
    END LOOP;

    FOR i IN 1..p_cant_gen LOOP
        INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
        VALUES (p_partido_id, 'GEN-' || LPAD((v_max_gen + i)::TEXT, 4, '0'), 'GENERAL', p_precio_gen, 'DISPONIBLE')
        ON CONFLICT ON CONSTRAINT unique_asiento_partido DO NOTHING;
        v_total := v_total + 1;
    END LOOP;

    RETURN v_total;
END;
$BODY$;


SELECT COUNT(*) AS total_tickets
FROM ticket
WHERE partido_id = 3;


--error de generar tickets ya que mi columna tikcet tiene varchar 1o
ALTER TABLE ticket 
ALTER COLUMN numero_asiento TYPE VARCHAR(20);

--cambiar generar tickets a el lpad a un digito mas
	CREATE OR REPLACE FUNCTION public.sp_ticket_generar_masivo(
    p_partido_id integer,
    p_cant_vip integer,
    p_precio_vip numeric,
    p_cant_pref integer,
    p_precio_pref numeric,
    p_cant_gen integer,
    p_precio_gen numeric)
    RETURNS integer
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    v_max_vip   int;
    v_max_pref  int;
    v_max_gen   int;
    v_total     int := 0;
    i           int;
BEGIN
    LOCK TABLE ticket IN EXCLUSIVE MODE;

    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_asiento FROM POSITION('-' IN numero_asiento) + 1) AS INT)), 0)
    INTO v_max_vip
    FROM ticket WHERE partido_id = p_partido_id AND seccion = 'VIP';

    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_asiento FROM POSITION('-' IN numero_asiento) + 1) AS INT)), 0)
    INTO v_max_pref
    FROM ticket WHERE partido_id = p_partido_id AND seccion = 'PREFERENCIAL';

    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_asiento FROM POSITION('-' IN numero_asiento) + 1) AS INT)), 0)
    INTO v_max_gen
    FROM ticket WHERE partido_id = p_partido_id AND seccion = 'GENERAL';

    FOR i IN 1..p_cant_vip LOOP
        INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
        VALUES (p_partido_id, 'VIP-' || LPAD((v_max_vip + i)::TEXT, 5, '0'), 'VIP', p_precio_vip, 'DISPONIBLE')
        ON CONFLICT ON CONSTRAINT unique_asiento_partido DO NOTHING;
        v_total := v_total + 1;
    END LOOP;

    FOR i IN 1..p_cant_pref LOOP
        INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
        VALUES (p_partido_id, 'PREF-' || LPAD((v_max_pref + i)::TEXT, 5, '0'), 'PREFERENCIAL', p_precio_pref, 'DISPONIBLE')
        ON CONFLICT ON CONSTRAINT unique_asiento_partido DO NOTHING;
        v_total := v_total + 1;
    END LOOP;

    FOR i IN 1..p_cant_gen LOOP
        INSERT INTO ticket (partido_id, numero_asiento, seccion, precio, estado)
        VALUES (p_partido_id, 'GEN-' || LPAD((v_max_gen + i)::TEXT, 5, '0'), 'GENERAL', p_precio_gen, 'DISPONIBLE')
        ON CONFLICT ON CONSTRAINT unique_asiento_partido DO NOTHING;
        v_total := v_total + 1;
    END LOOP;

    RETURN v_total;
END;
$BODY$;


--recrear trigger de anular para que verifique si el partido no esta cancelado
CREATE OR REPLACE FUNCTION fn_anular_venta()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo restaura a DISPONIBLE si el partido NO está cancelado
    -- Si el partido está CANCELADO los tickets quedan como están (no sirven de nada)
    UPDATE ticket t
    SET estado = 'DISPONIBLE'
    WHERE t.id IN (
        SELECT dv.ticket_id 
        FROM detalle_venta dv 
        WHERE dv.venta_id = NEW.venta_id
    )
    AND EXISTS (
        SELECT 1 FROM partido p
        WHERE p.id = t.partido_id
        AND p.estado != 'CANCELADO'
    );

    -- Marcar venta como anulada
    UPDATE venta SET anulada = TRUE WHERE id = NEW.venta_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;