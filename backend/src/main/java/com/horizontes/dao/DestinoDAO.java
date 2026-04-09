package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Destino;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinoDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Destino> listarTodos() throws SQLException {
        List<Destino> lista = new ArrayList<>();
        String sql = "SELECT * FROM destinos ORDER BY nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapDestino(rs));
            }
        }
        return lista;
    }

    public Destino buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM destinos WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDestino(rs);
            }
        }
        return null;
    }

    public Destino buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM destinos WHERE nombre = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDestino(rs);
            }
        }
        return null;
    }

    public boolean insertar(Destino d) throws SQLException {
        String sql = "INSERT INTO destinos (nombre, pais, descripcion, clima, imagen_url) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getPais());
            ps.setString(3, d.getDescripcion());
            ps.setString(4, d.getClima());
            ps.setString(5, d.getImagenUrl());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Destino d) throws SQLException {
        String sql = "UPDATE destinos SET nombre=?, pais=?, descripcion=?, clima=?, imagen_url=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getPais());
            ps.setString(3, d.getDescripcion());
            ps.setString(4, d.getClima());
            ps.setString(5, d.getImagenUrl());
            ps.setInt(6, d.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM destinos WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Destino mapDestino(ResultSet rs) throws SQLException {
        Destino d = new Destino();
        d.setId(rs.getInt("id"));
        d.setNombre(rs.getString("nombre"));
        d.setPais(rs.getString("pais"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setClima(rs.getString("clima"));
        d.setImagenUrl(rs.getString("imagen_url"));
        return d;
    }
}