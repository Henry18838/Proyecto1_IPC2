package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapCliente(rs));
            }
        }
        return lista;
    }

    public Cliente buscarPorDpi(String dpi) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE dpi = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dpi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapCliente(rs);
            }
        }
        return null;
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapCliente(rs);
            }
        }
        return null;
    }

    public boolean insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (dpi, nombre, fecha_nacimiento, telefono, email, nacionalidad) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getDpi());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getFechaNacimiento());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getNacionalidad());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET nombre=?, fecha_nacimiento=?, telefono=?, email=?, nacionalidad=? WHERE dpi=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getFechaNacimiento());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getNacionalidad());
            ps.setString(6, c.getDpi());
            return ps.executeUpdate() > 0;
        }
    }

    private Cliente mapCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setDpi(rs.getString("dpi"));
        c.setNombre(rs.getString("nombre"));
        c.setFechaNacimiento(rs.getString("fecha_nacimiento"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setNacionalidad(rs.getString("nacionalidad"));
        return c;
    }
}