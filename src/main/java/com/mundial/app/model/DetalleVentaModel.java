// MIGRACION: package cambiado de "model" a "com.mundial.app.model"
package com.mundial.app.model;

import java.math.BigDecimal;

public class DetalleVentaModel {
    private int id;
    private int ventaId;
    private int ticketId;
    private BigDecimal precio;
    private BigDecimal iva;
    
    // para mostrar en pantalla con joins
    private String numeroAsiento;
    private String seccion;

    
    // todos
    public DetalleVentaModel(int id, int ventaId, int ticketId, BigDecimal precio, BigDecimal iva) {
        this.id       = id;
        this.ventaId  = ventaId;
        this.ticketId = ticketId;
        this.precio   = precio;
        this.iva      = iva;
    }

    // sin id, lo genera la bd
    public DetalleVentaModel(int ventaId, int ticketId, BigDecimal precio, BigDecimal iva) {
        this.ventaId  = ventaId;
        this.ticketId = ticketId;
        this.precio   = precio;
        this.iva      = iva;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVentaId() {
        return ventaId;
    }

    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
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

   
}
