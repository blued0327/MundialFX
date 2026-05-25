/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.dao;

import com.mundial.app.connection.CreateConnection;
import com.mundial.app.model.LogModel;
import java.sql.*;
/**
 *
 * @author rchar
 */
public class LogDao {
    
     private final Connection connection;
 
    public LogDao() {
        this.connection = CreateConnection.getInstancia().getConnection();
    }
     public int registrarLog(LogModel log) throws SQLException {
        String sql = "SELECT sp_log_usuario_registrar(?, ?, ?)";
 
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, log.getUsuarioId());
            ps.setString(2, log.getAccion());
            ps.setString(3, log.getIp());
 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    log.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }
}
