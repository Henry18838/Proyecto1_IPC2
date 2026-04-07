package com.horizontes.models;

public class Destino {
    private int id;
    private String nombre;
    private String pais;
    private String descripcion;
    private String clima;
    private String imagenUrl;

    public Destino() {}

    public Destino(int id, String nombre, String pais, String descripcion, String clima, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.descripcion = descripcion;
        this.clima = clima;
        this.imagenUrl = imagenUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getClima() { return clima; }
    public void setClima(String clima) { this.clima = clima; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}