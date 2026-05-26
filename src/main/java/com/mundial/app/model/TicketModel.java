package com.mundial.app.model;

import java.math.BigDecimal;

public class TicketModel {

    private int id;
    private int partidoId;
    private String nombrePartido;
    private String numeroAsiento;
    private String seccion;
    private BigDecimal precio;
    private String estado;

    //para mostrar en pantalla con joins
    private String equipoLocal;
    private String equipoVisitante;

    public String getNombrePartido() {
        return nombrePartido;
    }

    public void setNombrePartido(String nombrePartido) {
        this.nombrePartido = nombrePartido;
    }

    public TicketModel(int id, int partidoId, String nombrePartido, String numeroAsiento, String seccion, BigDecimal precio, String estado, String equipoLocal, String equipoVisitante) {
        this.id = id;
        this.partidoId = partidoId;
        this.nombrePartido = nombrePartido;
        this.numeroAsiento = numeroAsiento;
        this.seccion = seccion;
        this.precio = precio;
        this.estado = estado;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
    }

    //vacio
    public TicketModel() {
    }

    //todos
    public TicketModel(int id, int partidoId, String numeroAsiento, String seccion,
            BigDecimal precio, String estado) {
        this.id = id;
        this.partidoId = partidoId;
        this.numeroAsiento = numeroAsiento;
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

    public int getPartidoId() {
        return partidoId;
    }

    public void setPartidoId(int partidoId) {
        this.partidoId = partidoId;
    }

    public String getNumeroAsiento() {
        return numeroAsiento;
    }

    public void setNumeroAsiento(String numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    //para que se vea bien en jcombobox o jlist
    @Override
    public String toString() {
        return "Asiento " + numeroAsiento + " - " + seccion + " - Q" + precio;
    }
}
