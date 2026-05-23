/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
// MIGRACION: package cambiado de "model" a "com.mundial.app.model"
package com.mundial.app.model;

/**
 *
 * @author rchar
 */
public class BoletosModel {

    private int id;
    private String partidoAsociado;
    private int asiento;
    private String seccion;
    private double precio;
    private String estado;

    public BoletosModel(int id, String partidoAsociado, int asiento, String seccion, double precio, String estado) {
        this.id = id;
        this.partidoAsociado = partidoAsociado;
        this.asiento = asiento;
        this.seccion = seccion;
        this.precio = precio;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPartidoAsociado() {
        return partidoAsociado;
    }

    public void setPartidoAsociado(String partidoAsociado) {
        this.partidoAsociado = partidoAsociado;
    }

    public int getAsiento() {
        return asiento;
    }

    public void setAsiento(int asiento) {
        this.asiento = asiento;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


}
