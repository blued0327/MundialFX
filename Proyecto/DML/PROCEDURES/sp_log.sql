--sp_log

drop function sp_auditoria_registrar;
CREATE OR REPLACE FUNCTION sp_log_usuario_registrar(
    p_usuario_id int,
    p_accion varchar,
    p_ip varchar
)
RETURNS int AS $$
DECLARE
    v_id int;
BEGIN
    --validar que la accion sea una de las permitidas
    IF p_accion 
	NOT IN ('LOGIN', 'LOGOUT', 'LOGIN_FALLIDO') 
	THEN
        RAISE EXCEPTION 'La accion % no es valida', p_accion;
    END IF;

    INSERT INTO log_usuario(
        usuario_id,
        accion,
        ip
    )
    VALUES(
        p_usuario_id,
        p_accion,
        p_ip
    )
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$ LANGUAGE 
plpgsql;

--
--listar logs paginados (los mas recientes primero)

create or replace function sp_log_listar(
    p_limite int,   -- cuántos registros quiero ver por pantalla 
    p_offset int    -- cuántos te saltas 
)

returns table(
    id int,
    usuario_id int,
    username varchar,
    fecha timestamp,
    accion varchar,
    ip varchar
) as $$
begin
    
    return query
    select
        l.id,
        l.usuario_id,
        u.username,  -- sacamos el nombre del usuario para no ver solo un número feo
        l.fecha,
        l.accion,
        l.ip
    from
        log_usuario l 
    left join usuario u on l.usuario_id = u.id 
    -- enganchamos con la tabla de usuarios usando el id. 
    -- use  left join por si borramos  al usuario, que el log no desaparezca del mapa.
    order by
        l.fecha desc -- lo más nuevo va primero, para agarrar antes lo ultimp
    limit p_limite   -- el limite que puse como atributo
    offset p_offset; -- saltarme los que ponga
end;
$$ language plpgsql; 



-----------contar log

create or replace function sp_log_contar()
returns int as $$ 
declare
    v_total int;  
begin
    -- cuenta todas las filas de la tabla y mete ese número dentro de v_total
    select count(*) into v_total from log_usuario;
    
   
    return v_total;
end;
$$ 
language plpgsql