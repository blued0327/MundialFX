package com.mundial.app.dao;

import com.mundial.app.connection.CreateConnection;
import com.mundial.app.model.LogModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LogDao {

    //registrar un log (LOGIN, LOGOUT, LOGIN_FALLIDO)
    public int registrar(Integer usuarioId, String accion, String ip) {
        String query = "SELECT sp_log_usuario_registrar(?, ?, ?)";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            //usuario_id puede ser null en LOGIN_FALLIDO
            if (usuarioId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);//este null es de tipo interger
            } else {
                ps.setInt(1, usuarioId);
            }
            ps.setString(2, accion);
            ps.setString(3, ip);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    //listar logs paginados limit + offset
    public List<LogModel> listar(int limit, int offset) {
        List<LogModel> lista = new ArrayList<>();
        String query = "SELECT * FROM sp_log_listar(?, ?)";

        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LogModel log = new LogModel();
                //guardar id del log
                log.setId(rs.getInt("id"));

                //usuario_id puede venir null
                //esto pasa por ejemplo en LOGIN_FALLIDO
                int uid = rs.getInt("usuario_id");

                //si venia null guardar null
                //si no guardar el id normal
                log.setUsuarioId(
                        rs.wasNull() ? null : uid
                );

                //guardar username
                log.setUsername(
                        rs.getString("username")
                );

                //obtener fecha timestamp de sql
                Timestamp fecha = rs.getTimestamp("fecha");

                //si fecha existe convertirla a LocalDateTime
                if (fecha != null) {

                    log.setFecha(
                            fecha.toLocalDateTime()
                    );
                }

                //guardar accion realizada
                //ejemplo LOGIN LOGOUT etc
                log.setAccion(
                        rs.getString("accion")
                );

                //guardar ip desde donde se hizo la accion
                log.setIp(
                        rs.getString("ip")
                );

                //agregar log a la lista final
                lista.add(log);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    //contar total de logs (para saber si quedan mas por cargar)
    public int contar() {
        String query = "SELECT sp_log_contar()";
        try (Connection conn = CreateConnection.getInstancia().getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
