package com.horizontes.models;

public class Proveedor {
    private int id;
    private String nombre;
    private int tipo;
    private String pais;
    private String contacto;

    public Proveedor() {}

    public String getTipoTexto() {
        switch (this.tipo) {
            case 1: return "Aerolinea";
            case 2: return "Hotel";
            case 3: return "Tour";
            case 4: return "Traslado";
            case 5: return "Otro";
            default: return "Desconocido";
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
}