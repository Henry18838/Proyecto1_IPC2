package com.horizontes.models;

import java.util.List;

public class Paquete {
    private int id;
    private String nombre;
    private int destinoId;
    private String destinoNombre;
    private int duracionDias;
    private String descripcion;
    private double precioVenta;
    private int capacidadMaxima;
    private boolean activo;
    private List<ServicioPaquete> servicios;

    public Paquete() {}

    public double getCostoTotalAgencia() {
        double total = 0;
        if (servicios != null) {
            for (ServicioPaquete s : servicios) {
                total += s.getCosto();
            }
        }
        return total;
    }

    public double getGananciaBruta() {
        return this.precioVenta - getCostoTotalAgencia();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getDestinoId() { return destinoId; }
    public void setDestinoId(int destinoId) { this.destinoId = destinoId; }
    public String getDestinoNombre() { return destinoNombre; }
    public void setDestinoNombre(String destinoNombre) { this.destinoNombre = destinoNombre; }
    public int getDuracionDias() { return duracionDias; }
    public void setDuracionDias(int duracionDias) { this.duracionDias = duracionDias; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public List<ServicioPaquete> getServicios() { return servicios; }
    public void setServicios(List<ServicioPaquete> servicios) { this.servicios = servicios; }
}