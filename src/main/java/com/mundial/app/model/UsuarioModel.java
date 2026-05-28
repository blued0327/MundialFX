

package com.mundial.app.model;

import java.time.LocalDateTime;

public class UsuarioModel {

    private int id;
    private String username;
    private String password;
    private String rol;
    private boolean estado;
    private LocalDateTime creadoCuando;

    //vacio
    public UsuarioModel() {

    }

    //insertar
    public UsuarioModel(String username, String password, String rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    //actulizar
    public UsuarioModel(String username, String password, String rol, boolean estado) {
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.estado = estado;
    }

    //todos
    public UsuarioModel(int id, String username, String password, String rol, boolean estado, LocalDateTime creadoCuando) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.estado = estado;
        this.creadoCuando = creadoCuando;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoCuando() {
        return creadoCuando;
    }

    public void setCreadoCuando(LocalDateTime creadoCuando) {
        this.creadoCuando = creadoCuando;
    }

}
