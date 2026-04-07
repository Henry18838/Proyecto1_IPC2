package com.horizontes.models;

public class Usuario {
    private int id;
    private String nombre;
    private String password;
    private int rol;
    private boolean activo;
    private String fechaCreacion;

    public Usuario() {}

    public Usuario(int id, String nombre, String password, int rol, boolean activo, String fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getRol() { return rol; }
    public void setRol(int rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}