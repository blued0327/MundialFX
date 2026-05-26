-------------------------------ventas
---------------- secuencia para numero de factura
CREATE SEQUENCE IF NOT EXISTS seq_factura START 1;


---------------- registrar venta
drop function sp_venta_registrar;

CREATE OR REPLACE
    FUNCTION sp_venta_registrar(
    p_cliente_id int,
    p_usuario_id int,
    p_ticket_ids int[],
    p_subtotal numeric,
    p_descuento numeric,
    p_total_iva numeric,
    p_total numeric)RETURNS int AS $$

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

        ---------------- generar numero de factura
        v_numero_factura := 'FAC-' || LPAD(nextval('seq_factura')::text, 8, '0');

        ---------------- insertar venta
        INSERT INTO venta(
            cliente_id,
            usuario_id,
            subtotal,
            descuento,
            total_iva,
            total,
            numero_factura
        )
        VALUES(
            p_cliente_id,
            p_usuario_id,
            p_subtotal,
            p_descuento,
            p_total_iva,
            p_total,
            v_numero_factura
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
$$
LANGUAGE plpgsql;

---------------- anular venta
/*
CREATE OR REPLACE
    FUNCTION sp_venta_anular(
    p_venta_id int,
    p_usuario_id int,
    p_motivo text)RETURNS boolean AS $$

    BEGIN
        IF NOT EXISTS(
            SELECT
                1
            FROM
                venta
            WHERE
                id = p_venta_id
                AND anulada = FALSE
        )THEN
            RAISE EXCEPTION 'La venta no existe o ya está anulada';
        END IF;

        ---------------- anular venta
        UPDATE venta
        SET
            anulada = TRUE
        WHERE
            id = p_venta_id;

        ---------------- liberar tickets
        UPDATE ticket
        SET
            estado = 'DISPONIBLE'
        WHERE
            id IN(
                SELECT
                    ticket_id
                FROM
                    detalle_venta
                WHERE
                    venta_id = p_venta_id
            );

        ---------------- registrar anulacion
        INSERT INTO anulacion_venta(
            venta_id,
            usuario_id,
            motivo
        )
        VALUES(
            p_venta_id,
            p_usuario_id,
            p_motivo
        );

        RETURN TRUE;
    END;
$$
LANGUAGE plpgsql;*/
CREATE OR REPLACE
    FUNCTION sp_venta_anular(
    p_venta_id int,
    p_usuario_id int,
    p_motivo text)RETURNS boolean AS $$

    BEGIN
        IF NOT EXISTS(
            SELECT
                1
            FROM
                venta
            WHERE
                id = p_venta_id
                AND anulada = FALSE
        )THEN
            RAISE EXCEPTION 'La venta no existe o ya está anulada';
        END IF;

        ---------------- registrar anulacion
        --el trigger trg_anular_venta se encarga de:
        --  1. marcar la venta como anulada
        --  2. liberar los tickets (estado = DISPONIBLE)
        INSERT INTO anulacion_venta(
            venta_id,
            usuario_id,
            motivo
        )
        VALUES(
            p_venta_id,
            p_usuario_id,
            p_motivo
        );

        RETURN TRUE;
    END;
$$
LANGUAGE plpgsql;


---------------- obtener recibo
CREATE OR REPLACE
    FUNCTION sp_venta_obtener_recibo(
    p_venta_id int)RETURNS TABLE(
    recibo_nro int,
    numero_factura varchar,
    fecha timestamp,
    cliente_nombre text,
    cliente_email varchar,
    vendedor varchar,
    partido text,
    fecha_partido timestamp,
    estadio varchar,
    numero_asiento varchar,
    seccion varchar,
    precio numeric,
    iva numeric,
    subtotal numeric,
    descuento numeric,
    total_iva numeric,
    total numeric) AS $$

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
            v.total
        FROM
            venta v
        JOIN cliente c ON v.cliente_id = c.id
        JOIN usuario u ON v.usuario_id = u.id
        JOIN detalle_venta dv ON dv.venta_id = v.id
        JOIN ticket t ON dv.ticket_id = t.id
        JOIN partido p ON t.partido_id = p.id
        WHERE
            v.id = p_venta_id
        ORDER BY
            t.seccion,
            t.numero_asiento;
    END;
$$
LANGUAGE plpgsql;


------cosas adicionales que me di cuenta despuess

---------------- reporte ventas por fecha
CREATE OR REPLACE
    FUNCTION sp_reporte_ventas_por_fecha(
    p_fecha_inicio timestamp,
    p_fecha_fin timestamp)RETURNS TABLE(
    venta_id int,
    numero_factura varchar,
    fecha timestamp,
    cliente text,
    vendedor varchar,
    total numeric) AS $$
    BEGIN
        RETURN query
        SELECT
            v.id,
            v.numero_factura,
            v.fecha,
            (c.nombre || ' ' || c.apellido)::text,
            u.username,
            v.total
        FROM
            venta v
        JOIN cliente c ON v.cliente_id = c.id
        JOIN usuario u ON v.usuario_id = u.id
        WHERE
            v.fecha BETWEEN p_fecha_inicio AND p_fecha_fin
            AND v.anulada = FALSE
        ORDER BY
            v.fecha;
    END;
$$ LANGUAGE plpgsql;


---------------- reporte ventas por partido
CREATE OR REPLACE
    FUNCTION sp_reporte_ventas_por_partido()RETURNS TABLE(
    partido_id int,
    partido text,
    tickets_vendidos bigint,
    ingreso_total numeric) AS $$
    BEGIN
        RETURN query
        SELECT
            p.id,
            (p.equipo_local || ' vs ' || p.equipo_visitante)::text,
            COUNT(dv.id),
            COALESCE(SUM(dv.precio + dv.iva), 0)
        FROM
            partido p
        LEFT JOIN ticket t ON t.partido_id = p.id
        LEFT JOIN detalle_venta dv ON dv.ticket_id = t.id
        LEFT JOIN venta v ON v.id = dv.venta_id AND v.anulada = FALSE
        GROUP BY
            p.id,
            p.equipo_local,
            p.equipo_visitante
        ORDER BY
            ingreso_total DESC;
    END;
$$ LANGUAGE plpgsql;


---------------- reporte tickets por seccion
CREATE OR REPLACE
    FUNCTION sp_reporte_tickets_por_seccion()RETURNS TABLE(
    seccion varchar,
    cantidad bigint,
    ingreso numeric) AS $$
    BEGIN
        RETURN query
        SELECT
            t.seccion,
            COUNT(dv.id),
            COALESCE(SUM(dv.precio + dv.iva), 0)
        FROM
            ticket t
        JOIN detalle_venta dv ON dv.ticket_id = t.id
        JOIN venta v ON v.id = dv.venta_id AND v.anulada = FALSE
        GROUP BY
            t.seccion
        ORDER BY
            ingreso DESC;
    END;
$$ LANGUAGE plpgsql;


---------------- ingresos totales
CREATE OR REPLACE
    FUNCTION sp_reporte_ingresos_totales(
    p_fecha_inicio timestamp,
    p_fecha_fin timestamp)RETURNS numeric AS $$
    DECLARE
        v_total numeric;
    BEGIN
        SELECT
            COALESCE(SUM(total), 0)
        INTO
            v_total
        FROM
            venta
        WHERE
            fecha BETWEEN p_fecha_inicio AND p_fecha_fin
            AND anulada = FALSE;
        RETURN v_total;
    END;
$$ LANGUAGE plpgsql;

---------------listar todas las ventas para el historial
--las mas nuevas arriba
--usamos left join por si borraron al cliente o usuario, igual queremos ver la venta
CREATE OR REPLACE FUNCTION sp_venta_listar_todas()
RETURNS TABLE(
    id int,
    numero_factura varchar,
    fecha timestamp,
    cliente_nombre text,
    vendedor varchar,
    total numeric,
    anulada boolean
) AS $$
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
        v.anulada
    FROM
        venta v
    LEFT JOIN cliente c ON v.cliente_id = c.id
    LEFT JOIN usuario u ON v.usuario_id = u.id
    ORDER BY v.fecha DESC;
END;
$$ LANGUAGE plpgsql;


-------------
---------------buscar venta por numero de factura
--si buscas 001 te trae FAC-00000001
--ILIKE para que no importe mayus o minus
CREATE OR REPLACE FUNCTION sp_venta_buscar_factura(
    p_numero_factura varchar
)
RETURNS TABLE(
    id int,
    numero_factura varchar,
    fecha timestamp,
    cliente_nombre text,
    vendedor varchar,
    total numeric,
    anulada boolean
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.id,
        v.numero_factura,
        v.fecha,
        (c.nombre || ' ' || c.apellido)::text,
        u.username,
        v.total,
        v.anulada
    FROM
        venta v
    LEFT JOIN cliente c ON v.cliente_id = c.id
    LEFT JOIN usuario u ON v.usuario_id = u.id
    WHERE
        v.numero_factura ILIKE '%' || p_numero_factura || '%'
    ORDER BY v.fecha DESC;
END;
$$ LANGUAGE plpgsql;


