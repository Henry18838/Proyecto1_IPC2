package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Pago;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Pago> listarPorReservacion(int reservacionId) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT p.*, r.numero_reservacion FROM pagos p JOIN reservaciones r ON p.reservacion_id = r.id WHERE p.reservacion_id = ? ORDER BY p.fecha_pago";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapPago(rs));
            }
        }
        return lista;
    }

    public double getTotalPagado(int reservacionId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM pagos WHERE reservacion_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public boolean insertar(Pago p) throws SQLException {
        String sql = "INSERT INTO pagos (reservacion_id, monto, metodo, fecha_pago) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, p.getReservacionId());
            ps.setDouble(2, p.getMonto());
            ps.setInt(3, p.getMetodo());
            ps.setString(4, p.getFechaPago());
            return ps.executeUpdate() > 0;
        }
    }

    private Pago mapPago(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setId(rs.getInt("id"));
        p.setReservacionId(rs.getInt("reservacion_id"));
        p.setNumeroReservacion(rs.getString("numero_reservacion"));
        p.setMonto(rs.getDouble("monto"));
        p.setMetodo(rs.getInt("metodo"));
        p.setFechaPago(rs.getString("fecha_pago"));
        p.setFechaRegistro(rs.getString("fecha_registro"));
        return p;
    }
}