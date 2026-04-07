package com.horizontes.models;

public class ServicioPaquete {
    private int id;
    private int paqueteId;
    private int proveedorId;
    private String proveedorNombre;
    private String descripcion;
    private double costo;

    public ServicioPaquete() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPaqueteId() { return paqueteId; }
    public void setPaqueteId(int paqueteId) { this.paqueteId = paqueteId; }
    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }
    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }
}