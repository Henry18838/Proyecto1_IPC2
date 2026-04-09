package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class ReporteDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Map<String, Object>> reporteVentas(String fechaInicio, String fechaFin) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT r.numero_reservacion, p.nombre as paquete, u.nombre as agente, " +
                     "r.fecha_viaje, r.cantidad_pasajeros, r.costo_total " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN usuarios u ON r.agente_id = u.id " +
                     "WHERE r.estado = 2 " +
                     (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ? " : "") +
                     "ORDER BY r.fecha_creacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("numeroReservacion", rs.getString("numero_reservacion"));
                fila.put("paquete", rs.getString("paquete"));
                fila.put("agente", rs.getString("agente"));
                fila.put("fechaViaje", rs.getString("fecha_viaje"));
                fila.put("cantidadPasajeros", rs.getInt("cantidad_pasajeros"));
                fila.put("costoTotal", rs.getDouble("costo_total"));
                lista.add(fila);
            }
        }
        return lista;
    }

    public List<Map<String, Object>> reporteCancelaciones(String fechaInicio, String fechaFin) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT r.numero_reservacion, c.fecha_cancelacion, " +
                     "c.monto_reembolsado, c.porcentaje_reembolso, c.perdida_agencia " +
                     "FROM cancelaciones c " +
                     "JOIN reservaciones r ON c.reservacion_id = r.id " +
                     (fechaInicio != null && fechaFin != null ? "WHERE DATE(c.fecha_cancelacion) BETWEEN ? AND ? " : "") +
                     "ORDER BY c.fecha_cancelacion DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("numeroReservacion", rs.getString("numero_reservacion"));
                fila.put("fechaCancelacion", rs.getString("fecha_cancelacion"));
                fila.put("montoReembolsado", rs.getDouble("monto_reembolsado"));
                fila.put("porcentajeReembolso", rs.getDouble("porcentaje_reembolso"));
                fila.put("perdidaAgencia", rs.getDouble("perdida_agencia"));
                lista.add(fila);
            }
        }
        return lista;
    }

    public Map<String, Object> reporteGanancias(String fechaInicio, String fechaFin) throws SQLException {
        Map<String, Object> resultado = new LinkedHashMap<>();

        // Total ganancias brutas de ventas confirmadas
        String sqlVentas = "SELECT COALESCE(SUM(r.costo_total - " +
                           "(SELECT COALESCE(SUM(sp.costo), 0) FROM servicios_paquete sp WHERE sp.paquete_id = r.paquete_id)), 0) as ganancia_bruta " +
                           "FROM reservaciones r WHERE r.estado = 2 " +
                           (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ?" : "");
        try (PreparedStatement ps = getConnection().prepareStatement(sqlVentas)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("gananciaBruta", rs.getDouble("ganancia_bruta"));
            }
        }

        // Total reembolsos
        String sqlReembolsos = "SELECT COALESCE(SUM(c.monto_reembolsado), 0) as total_reembolsos " +
                               "FROM cancelaciones c " +
                               (fechaInicio != null && fechaFin != null ? "WHERE DATE(c.fecha_cancelacion) BETWEEN ? AND ?" : "");
        try (PreparedStatement ps = getConnection().prepareStatement(sqlReembolsos)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double reembolsos = rs.getDouble("total_reembolsos");
                resultado.put("totalReembolsos", reembolsos);
                double gananciaBruta = (double) resultado.getOrDefault("gananciaBruta", 0.0);
                resultado.put("gananciaNeta", gananciaBruta - reembolsos);
            }
        }

        return resultado;
    }

    public Map<String, Object> reporteAgenteMasVentas(String fechaInicio, String fechaFin) throws SQLException {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String sql = "SELECT u.nombre as agente, COUNT(r.id) as total_reservaciones, " +
                     "SUM(r.costo_total) as monto_total " +
                     "FROM reservaciones r JOIN usuarios u ON r.agente_id = u.id " +
                     "WHERE r.estado = 2 " +
                     (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ? " : "") +
                     "GROUP BY u.id, u.nombre ORDER BY total_reservaciones DESC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("agente", rs.getString("agente"));
                resultado.put("totalReservaciones", rs.getInt("total_reservaciones"));
                resultado.put("montoTotal", rs.getDouble("monto_total"));
            }
        }
        return resultado;
    }

    public Map<String, Object> reporteAgenteMasGanancias(String fechaInicio, String fechaFin) throws SQLException {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String sql = "SELECT u.nombre as agente, " +
                     "SUM(r.costo_total - (SELECT COALESCE(SUM(sp.costo),0) FROM servicios_paquete sp WHERE sp.paquete_id = r.paquete_id)) as ganancia_total " +
                     "FROM reservaciones r JOIN usuarios u ON r.agente_id = u.id " +
                     "WHERE r.estado = 2 " +
                     (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ? " : "") +
                     "GROUP BY u.id, u.nombre ORDER BY ganancia_total DESC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("agente", rs.getString("agente"));
                resultado.put("gananciaTotal", rs.getDouble("ganancia_total"));
            }
        }
        return resultado;
    }

    public Map<String, Object> reportePaqueteMasVendido(String fechaInicio, String fechaFin) throws SQLException {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String sql = "SELECT p.nombre as paquete, COUNT(r.id) as total_reservaciones, " +
                     "SUM(r.costo_total) as monto_total " +
                     "FROM reservaciones r JOIN paquetes p ON r.paquete_id = p.id " +
                     "WHERE r.estado = 2 " +
                     (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ? " : "") +
                     "GROUP BY p.id, p.nombre ORDER BY total_reservaciones DESC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("paquete", rs.getString("paquete"));
                resultado.put("totalReservaciones", rs.getInt("total_reservaciones"));
                resultado.put("montoTotal", rs.getDouble("monto_total"));
            }
        }
        return resultado;
    }

    public Map<String, Object> reportePaqueteMenosVendido(String fechaInicio, String fechaFin) throws SQLException {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String sql = "SELECT p.nombre as paquete, COUNT(r.id) as total_reservaciones, " +
                     "SUM(r.costo_total) as monto_total " +
                     "FROM reservaciones r JOIN paquetes p ON r.paquete_id = p.id " +
                     "WHERE r.estado = 2 " +
                     (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ? " : "") +
                     "GROUP BY p.id, p.nombre ORDER BY total_reservaciones ASC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("paquete", rs.getString("paquete"));
                resultado.put("totalReservaciones", rs.getInt("total_reservaciones"));
                resultado.put("montoTotal", rs.getDouble("monto_total"));
            }
        }
        return resultado;
    }

    public List<Map<String, Object>> reporteOcupacionPorDestino(String fechaInicio, String fechaFin) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT d.nombre as destino, COUNT(r.id) as total_reservaciones, " +
                     "SUM(r.cantidad_pasajeros) as total_pasajeros " +
                     "FROM reservaciones r " +
                     "JOIN paquetes p ON r.paquete_id = p.id " +
                     "JOIN destinos d ON p.destino_id = d.id " +
                     "WHERE r.estado IN (2,4) " +
                     (fechaInicio != null && fechaFin != null ? "AND DATE(r.fecha_creacion) BETWEEN ? AND ? " : "") +
                     "GROUP BY d.id, d.nombre ORDER BY total_reservaciones DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (fechaInicio != null && fechaFin != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("destino", rs.getString("destino"));
                fila.put("totalReservaciones", rs.getInt("total_reservaciones"));
                fila.put("totalPasajeros", rs.getInt("total_pasajeros"));
                lista.add(fila);
            }
        }
        return lista;
    }
}