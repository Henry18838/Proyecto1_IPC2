package com.horizontes.models;

import java.util.List;

public class Reservacion {
    private int id;
    private String numeroReservacion;
    private int paqueteId;
    private String paqueteNombre;
    private int agenteId;
    private String agenteNombre;
    private String fechaCreacion;
    private String fechaViaje;
    private int cantidadPasajeros;
    private double costoTotal;
    private int estado;
    private List<Cliente> pasajeros;

    public Reservacion() {}

    public String getEstadoTexto() {
        switch (this.estado) {
            case 1: return "Pendiente";
            case 2: return "Confirmada";
            case 3: return "Cancelada";
            case 4: return "Completada";
            default: return "Desconocido";
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumeroReservacion() { return numeroReservacion; }
    public void setNumeroReservacion(String numeroReservacion) { this.numeroReservacion = numeroReservacion; }
    public int getPaqueteId() { return paqueteId; }
    public void setPaqueteId(int paqueteId) { this.paqueteId = paqueteId; }
    public String getPaqueteNombre() { return paqueteNombre; }
    public void setPaqueteNombre(String paqueteNombre) { this.paqueteNombre = paqueteNombre; }
    public int getAgenteId() { return agenteId; }
    public void setAgenteId(int agenteId) { this.agenteId = agenteId; }
    public String getAgenteNombre() { return agenteNombre; }
    public void setAgenteNombre(String agenteNombre) { this.agenteNombre = agenteNombre; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getFechaViaje() { return fechaViaje; }
    public void setFechaViaje(String fechaViaje) { this.fechaViaje = fechaViaje; }
    public int getCantidadPasajeros() { return cantidadPasajeros; }
    public void setCantidadPasajeros(int cantidadPasajeros) { this.cantidadPasajeros = cantidadPasajeros; }
    public double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(double costoTotal) { this.costoTotal = costoTotal; }
    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }
    public List<Cliente> getPasajeros() { return pasajeros; }
    public void setPasajeros(List<Cliente> pasajeros) { this.pasajeros = pasajeros; }
}