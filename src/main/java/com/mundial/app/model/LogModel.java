/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.model;

import java.time.LocalDateTime;

public class LogModel {

    private int id;
    private Integer usuarioId;   //puede ser null en LOGIN_FALLIDO
    private String username;     //viene del JOIN con usuario
    private LocalDateTime fecha;
    private String accion;
    private String ip;

    //vacio
    public LogModel() {
    }

    //todos
    public LogModel(int id, Integer usuarioId, String username, LocalDateTime fecha, String accion, String ip) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.username = username;
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

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
