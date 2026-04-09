package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapProveedor(rs));
            }
        }
        return lista;
    }

    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM proveedores WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapProveedor(rs);
            }
        }
        return null;
    }

    public Proveedor buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM proveedores WHERE nombre = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapProveedor(rs);
            }
        }
        return null;
    }

    public boolean insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedores (nombre, tipo, pais, contacto) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getTipo());
            ps.setString(3, p.getPais());
            ps.setString(4, p.getContacto());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedores SET nombre=?, tipo=?, pais=?, contacto=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getTipo());
            ps.setString(3, p.getPais());
            ps.setString(4, p.getContacto());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedores WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Proveedor mapProveedor(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setTipo(rs.getInt("tipo"));
        p.setPais(rs.getString("pais"));
        p.setContacto(rs.getString("contacto"));
        return p;
    }
}