package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Cancelacion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CancelacionDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Cancelacion c) throws SQLException {
        String sql = "INSERT INTO cancelaciones (reservacion_id, monto_reembolsado, porcentaje_reembolso, perdida_agencia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.getReservacionId());
            ps.setDouble(2, c.getMontoReembolsado());
            ps.setDouble(3, c.getPorcentajeReembolso());
            ps.setDouble(4, c.getPerdidaAgencia());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Cancelacion> listarTodas() throws SQLException {
        List<Cancelacion> lista = new ArrayList<>();
        String sql = "SELECT c.*, r.numero_reservacion FROM cancelaciones c JOIN reservaciones r ON c.reservacion_id = r.id ORDER BY c.fecha_cancelacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapCancelacion(rs));
            }
        }
        return lista;
    }

    public List<Cancelacion> listarPorIntervalo(String fechaInicio, String fechaFin) throws SQLException {
        List<Cancelacion> lista = new ArrayList<>();
        String sql = "SELECT c.*, r.numero_reservacion FROM cancelaciones c JOIN reservaciones r ON c.reservacion_id = r.id WHERE DATE(c.fecha_cancelacion) BETWEEN ? AND ? ORDER BY c.fecha_cancelacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapCancelacion(rs));
            }
        }
        return lista;
    }

    private Cancelacion mapCancelacion(ResultSet rs) throws SQLException {
        Cancelacion c = new Cancelacion();
        c.setId(rs.getInt("id"));
        c.setReservacionId(rs.getInt("reservacion_id"));
        c.setNumeroReservacion(rs.getString("numero_reservacion"));
        c.setFechaCancelacion(rs.getString("fecha_cancelacion"));
        c.setMontoReembolsado(rs.getDouble("monto_reembolsado"));
        c.setPorcentajeReembolso(rs.getDouble("porcentaje_reembolso"));
        c.setPerdidaAgencia(rs.getDouble("perdida_agencia"));
        return c;
    }
}
