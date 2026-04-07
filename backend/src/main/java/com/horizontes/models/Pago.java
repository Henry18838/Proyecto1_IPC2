package com.horizontes.models;

public class Pago {
    private int id;
    private int reservacionId;
    private String numeroReservacion;
    private double monto;
    private int metodo;
    private String fechaPago;
    private String fechaRegistro;

    public Pago() {}

    public String getMetodoTexto() {
        switch (this.metodo) {
            case 1: return "Efectivo";
            case 2: return "Tarjeta";
            case 3: return "Transferencia";
            default: return "Desconocido";
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReservacionId() { return reservacionId; }
    public void setReservacionId(int reservacionId) { this.reservacionId = reservacionId; }
    public String getNumeroReservacion() { return numeroReservacion; }
    public void setNumeroReservacion(String numeroReservacion) { this.numeroReservacion = numeroReservacion; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public int getMetodo() { return metodo; }
    public void setMetodo(int metodo) { this.metodo = metodo; }
    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}