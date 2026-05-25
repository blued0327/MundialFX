/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.model;

import java.time.LocalDateTime;
/**
 *
 * @author rchar
 */
public class LogModel {
    private int id;
    private int usuarioId;
    private LocalDateTime fecha;
    private String accion;
    private String ip;
    
    public static final String ACCION_LOGIN         = "LOGIN";
    public static final String ACCION_LOGOUT        = "LOGOUT";
    public static final String ACCION_LOGIN_FALLIDO = "LOGIN_FALLIDO";
    
    // Para registrar un log nuevo
    public LogModel(int usuarioId, String accion, String ip) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.ip = ip;
        
    }
    
    // Para consultar un log existente
    public LogModel(int id, int usuarioId, LocalDateTime fecha, String accion, String ip) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.accion = accion;
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    @Override
    public String toString() {
        return "[" + accion + "] Usuario: " + usuarioId + " - IP: " + ip + " - " + fecha;
    }
    
}
