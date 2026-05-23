// MIGRACION: package cambiado de "model" a "com.mundial.app.model"
package com.mundial.app.model;

import java.time.LocalDateTime;

public class PartidoModel {

    private int id;
    private String equipoLocal;
    private String equipoVisitante;
    private LocalDateTime fecha;
    private String estadio;
    private String ciudad;
    private int capacidad;
    private String estado;
    private LocalDateTime creadoEn;

    public PartidoModel() {
    }

    // Sin id para insertar
    public PartidoModel(String equipoLocal, String equipoVisitante,
            LocalDateTime fecha, String estadio,
            String ciudad, int capacidad, String estado) {
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.fecha = fecha;
        this.estadio = estadio;
        this.ciudad = ciudad;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    // para traer desde BD
    public PartidoModel(int id, String equipoLocal, String equipoVisitante,
            LocalDateTime fecha, String estadio,
            String ciudad, int capacidad, String estado,
            LocalDateTime creadoEn) {
        this.id = id;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.fecha = fecha;
        this.estadio = estadio;
        this.ciudad = ciudad;
        this.capacidad = capacidad;
        this.estado = estado;
        this.creadoEn = creadoEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public String getEquipoVisitante() {
        return equipoVisitante;
    }

    public void setEquipoVisitante(String equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstadio() {
        return estadio;
    }

    public void setEstadio(String estadio) {
        this.estadio = estadio;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Override
    public String toString() {
        return equipoLocal + " vs " + equipoVisitante + " — " + estadio;
    }
}
