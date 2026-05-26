-------------------------------ventas
---------------- secuencia para numero de factura
CREATE SEQUENCE IF NOT EXISTS seq_factura START 1;


---------------- registrar venta
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
		-- ejecuta seq_factura que esta empieza en 1, pasa a texto, lpad rellena con 0 hasta 8 y por ulltmo se concatena con fac
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

        ---------------- insertar detalle y vender tickets
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

            UPDATE ticket
            SET
                estado = 'VENDIDO'
            WHERE
                id = v_ticket_id;
        END LOOP;

        RETURN v_venta_id;
    END;
$$
LANGUAGE plpgsql;


---------------- anular venta
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


