package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Cliente;
import com.horizontes.models.Reservacion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservacionDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Reservacion> listarTodas() throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = "SELECT r.*, p.nombre as paquete_nombre, u.nombre as agente_nombre " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN usuarios u ON r.agente_id = u.id " +
                     "ORDER BY r.fecha_creacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapReservacion(rs));
            }
        }
        return lista;
    }

    public List<Reservacion> listarPorFechaHoy() throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = "SELECT r.*, p.nombre as paquete_nombre, u.nombre as agente_nombre " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN usuarios u ON r.agente_id = u.id " +
                     "WHERE DATE(r.fecha_creacion) = CURDATE() " +
                     "ORDER BY r.fecha_creacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapReservacion(rs));
            }
        }
        return lista;
    }

    public List<Reservacion> listarPorCliente(int clienteId) throws SQLException {
        List<Reservacion> lista = new ArrayList<>();
        String sql = "SELECT r.*, p.nombre as paquete_nombre, u.nombre as agente_nombre " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN usuarios u ON r.agente_id = u.id " +
                     "JOIN reservacion_pasajeros rp ON r.id = rp.reservacion_id " +
                     "WHERE rp.cliente_id = ? ORDER BY r.fecha_creacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapReservacion(rs));
            }
        }
        return lista;
    }

    public Reservacion buscarPorNumero(String numeroReservacion) throws SQLException {
        String sql = "SELECT r.*, p.nombre as paquete_nombre, u.nombre as agente_nombre " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN usuarios u ON r.agente_id = u.id " +
                     "WHERE r.numero_reservacion = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, numeroReservacion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservacion r = mapReservacion(rs);
                r.setPasajeros(listarPasajeros(r.getId()));
                return r;
            }
        }
        return null;
    }

    public Reservacion buscarPorId(int id) throws SQLException {
        String sql = "SELECT r.*, p.nombre as paquete_nombre, u.nombre as agente_nombre " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN usuarios u ON r.agente_id = u.id " +
                     "WHERE r.id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservacion r = mapReservacion(rs);
                r.setPasajeros(listarPasajeros(r.getId()));
                return r;
            }
        }
        return null;
    }

    public int insertar(Reservacion r) throws SQLException {
        String sql = "INSERT INTO reservaciones (numero_reservacion, paquete_id, agente_id, fecha_viaje, cantidad_pasajeros, costo_total, estado) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNumeroReservacion());
            ps.setInt(2, r.getPaqueteId());
            ps.setInt(3, r.getAgenteId());
            ps.setString(4, r.getFechaViaje());
            ps.setInt(5, r.getCantidadPasajeros());
            ps.setDouble(6, r.getCostoTotal());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean agregarPasajero(int reservacionId, int clienteId) throws SQLException {
        String sql = "INSERT INTO reservacion_pasajeros (reservacion_id, cliente_id) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ps.setInt(2, clienteId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarEstado(int id, int estado) throws SQLException {
        String sql = "UPDATE reservaciones SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public String generarNumeroReservacion() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM reservaciones";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total") + 1;
                return String.format("RES-%05d", total);
            }
        }
        return "RES-00001";
    }

    public List<Cliente> listarPasajeros(int reservacionId) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.* FROM clientes c JOIN reservacion_pasajeros rp ON c.id = rp.cliente_id WHERE rp.reservacion_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setDpi(rs.getString("dpi"));
                c.setNombre(rs.getString("nombre"));
                c.setFechaNacimiento(rs.getString("fecha_nacimiento"));
                c.setTelefono(rs.getString("telefono"));
                c.setEmail(rs.getString("email"));
                c.setNacionalidad(rs.getString("nacionalidad"));
                lista.add(c);
            }
        }
        return lista;
    }

    private Reservacion mapReservacion(ResultSet rs) throws SQLException {
        Reservacion r = new Reservacion();
        r.setId(rs.getInt("id"));
        r.setNumeroReservacion(rs.getString("numero_reservacion"));
        r.setPaqueteId(rs.getInt("paquete_id"));
        r.setPaqueteNombre(rs.getString("paquete_nombre"));
        r.setAgenteId(rs.getInt("agente_id"));
        r.setAgenteNombre(rs.getString("agente_nombre"));
        r.setFechaCreacion(rs.getString("fecha_creacion"));
        r.setFechaViaje(rs.getString("fecha_viaje"));
        r.setCantidadPasajeros(rs.getInt("cantidad_pasajeros"));
        r.setCostoTotal(rs.getDouble("costo_total"));
        r.setEstado(rs.getInt("estado"));
        return r;
    }
}