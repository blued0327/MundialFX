package com.mundial.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class VentasModel {

    private int id;
    private LocalDateTime fecha;
    private int clienteId;
    private int usuarioId;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal totalIva;
    private BigDecimal total;
    private boolean anulada;
    private String numeroFactura;

    // IDs de tickets comprados (para registrar la venta)
    private List<Integer> ticketIds;
    private String clienteNombre;
    private String vendedor;

    //campos nuevos para facturacion
    //nit del cliente (si va vacio se guarda como CF)
    private String nit;
    //metodo de pago EFECTIVO o TARJETA
    private String metodoPago;
    //comision del 2% si se paga con tarjeta, sino queda en 0
    private BigDecimal comision;

    public VentasModel(String clienteNombre, String vendedor) {
        this.clienteNombre = clienteNombre;
        this.vendedor = vendedor;
    }

    // constructor para registrar una venta nueva
    public VentasModel(int clienteId, int usuarioId, BigDecimal subtotal, BigDecimal descuento, BigDecimal totalIva, BigDecimal total, List<Integer> ticketIds) {
        // id lo genera sql con SERIAL
        // fecha lo genera sql con DEFAULT CURRENT_TIMESTAMP
        // anulada arranca siempre en FALSE por defecto
        // numeroFactura lo genera el SP con la secuencia seq_factura
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalIva = totalIva;
        this.total = total;
        this.ticketIds = ticketIds;
    }

    // Para consultar una venta existente
    public VentasModel(int id, LocalDateTime fecha, int clienteId, int usuarioId, BigDecimal subtotal, BigDecimal descuento, BigDecimal totalIva, BigDecimal total, boolean anulada, String numeroFactura, List<Integer> ticketIds) {
        this.id = id;
        this.fecha = fecha;
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalIva = totalIva;
        this.total = total;
        this.anulada = anulada;
        this.numeroFactura = numeroFactura;
        this.ticketIds = ticketIds;
    }

    //contructor vacio
    public VentasModel() {
    }

    public VentasModel(int id, LocalDateTime fecha, int clienteId, int usuarioId, BigDecimal subtotal, BigDecimal descuento, BigDecimal totalIva, BigDecimal total, boolean anulada, String numeroFactura, List<Integer> ticketIds, String clienteNombre, String vendedor) {
        this.id = id;
        this.fecha = fecha;
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalIva = totalIva;
        this.total = total;
        this.anulada = anulada;
        this.numeroFactura = numeroFactura;
        this.ticketIds = ticketIds;
        this.clienteNombre = clienteNombre;
        this.vendedor = vendedor;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getTotalIva() {
        return totalIva;
    }

    public void setTotalIva(BigDecimal totalIva) {
        this.totalIva = totalIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public List<Integer> getTicketIds() {
        return ticketIds;
    }

    public void setTicketIds(List<Integer> ticketIds) {
        this.ticketIds = ticketIds;
    }

    //getters y setters de los campos nuevos
    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public void setComision(BigDecimal comision) {
        this.comision = comision;
    }

    @Override
    public String toString() {
        return "Factura " + numeroFactura + " - Total: Q" + total + (anulada ? " [ANULADA]" : "");
    }

}
