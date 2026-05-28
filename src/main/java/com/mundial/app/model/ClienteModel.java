

package com.mundial.app.model;

public class ClienteModel {

    private int id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String direccion;
    private boolean estado;

    // vacio
    public ClienteModel() {
    }

    // todos
    public ClienteModel(int id, String nombre, String apellido, String telefono,
            String email, String direccion, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.estado = estado;
    }

    // sin id para registrar nuevo
    public ClienteModel(String nombre, String apellido, String telefono,
            String email, String direccion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    // para que se vea bien en jcombobox
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}