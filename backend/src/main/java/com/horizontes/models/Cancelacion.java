package com.horizontes.models;

public class Cancelacion {
    private int id;
    private int reservacionId;
    private String numeroReservacion;
    private String fechaCancelacion;
    private double montoReembolsado;
    private double porcentajeReembolso;
    private double perdidaAgencia;

    public Cancelacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReservacionId() { return reservacionId; }
    public void setReservacionId(int reservacionId) { this.reservacionId = reservacionId; }
    public String getNumeroReservacion() { return numeroReservacion; }
    public void setNumeroReservacion(String numeroReservacion) { this.numeroReservacion = numeroReservacion; }
    public String getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(String fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }
    public double getMontoReembolsado() { return montoReembolsado; }
    public void setMontoReembolsado(double montoReembolsado) { this.montoReembolsado = montoReembolsado; }
    public double getPorcentajeReembolso() { return porcentajeReembolso; }
    public void setPorcentajeReembolso(double porcentajeReembolso) { this.porcentajeReembolso = porcentajeReembolso; }
    public double getPerdidaAgencia() { return perdidaAgencia; }
    public void setPerdidaAgencia(double perdidaAgencia) { this.perdidaAgencia = perdidaAgencia; }
}