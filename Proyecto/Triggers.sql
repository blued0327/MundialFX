

drop function if exists fn_marcar_ticket_vendido;
--trigger para cambiar a vendido
CREATE OR REPLACE FUNCTION fn_marcar_ticket_vendido()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE ticket SET estado = 'VENDIDO'
    WHERE id = NEW.ticket_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ticket_vendido
AFTER INSERT ON detalle_venta
FOR EACH ROW
EXECUTE FUNCTION fn_marcar_ticket_vendido();
--------------------------------------------------------------
--trigger para anular
drop function if exists fn_anular_venta cascade ;
CREATE OR REPLACE FUNCTION fn_anular_venta()
RETURNS TRIGGER AS $$
BEGIN
    -- Restaurar tickets a disponible
    UPDATE ticket SET estado = 'DISPONIBLE'
    WHERE id IN (
        SELECT ticket_id FROM detalle_venta WHERE venta_id = NEW.venta_id
    );

    -- Marcar venta como anulada
    UPDATE venta SET anulada = TRUE WHERE id = NEW.venta_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_anular_venta
AFTER INSERT ON anulacion_venta
FOR EACH ROW
EXECUTE FUNCTION fn_anular_venta();

/*
--------------------------------------------------------------
--trigger para saber los usuarios
drop function if exists SP_log_usuario cascade ;
CREATE OR REPLACE FUNCTION SP_log_usuario()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO log_usuario (usuario_id, accion, ip)
        VALUES (NEW.id, 'LOGIN', NULL);

    ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.estado = FALSE AND OLD.estado = TRUE THEN
            INSERT INTO log_usuario (usuario_id, accion, ip)
            VALUES (NEW.id, 'LOGOUT', NULL);
        ELSIF NEW.estado = TRUE AND OLD.estado = FALSE THEN
            INSERT INTO log_usuario (usuario_id, accion, ip)
            VALUES (NEW.id, 'LOGIN', NULL);
        END IF;

    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO log_usuario (usuario_id, accion, ip)
        VALUES (OLD.id, 'LOGIN_FALLIDO', NULL);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER TR_log_usuario
AFTER INSERT OR UPDATE OR DELETE ON usuario
FOR EACH ROW
EXECUTE PROCEDURE SP_log_usuario();
*/
