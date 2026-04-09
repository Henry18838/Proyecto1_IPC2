package com.horizontes.dao;

import com.horizontes.db.DatabaseConnection;
import com.horizontes.models.Paquete;
import com.horizontes.models.ServicioPaquete;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaqueteDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Paquete> listarTodos() throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre as destino_nombre FROM paquetes p JOIN destinos d ON p.destino_id = d.id ORDER BY p.nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapPaquete(rs));
            }
        }
        return lista;
    }

    public List<Paquete> listarActivos() throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre as destino_nombre FROM paquetes p JOIN destinos d ON p.destino_id = d.id WHERE p.activo = true ORDER BY p.nombre";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapPaquete(rs));
            }
        }
        return lista;
    }

    public List<Paquete> listarPorDestino(int destinoId) throws SQLException {
        List<Paquete> lista = new ArrayList<>();
        String sql = "SELECT p.*, d.nombre as destino_nombre FROM paquetes p JOIN destinos d ON p.destino_id = d.id WHERE p.destino_id = ? AND p.activo = true";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, destinoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapPaquete(rs));
            }
        }
        return lista;
    }

    public Paquete buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.*, d.nombre as destino_nombre FROM paquetes p JOIN destinos d ON p.destino_id = d.id WHERE p.id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Paquete p = mapPaquete(rs);
                p.setServicios(listarServiciosPorPaquete(id));
                return p;
            }
        }
        return null;
    }

    public Paquete buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT p.*, d.nombre as destino_nombre FROM paquetes p JOIN destinos d ON p.destino_id = d.id WHERE p.nombre = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Paquete p = mapPaquete(rs);
                p.setServicios(listarServiciosPorPaquete(p.getId()));
                return p;
            }
        }
        return null;
    }

    public boolean insertar(Paquete p) throws SQLException {
        String sql = "INSERT INTO paquetes (nombre, destino_id, duracion_dias, descripcion, precio_venta, capacidad_maxima, activo) VALUES (?, ?, ?, ?, ?, ?, true)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getDestinoId());
            ps.setInt(3, p.getDuracionDias());
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setInt(6, p.getCapacidadMaxima());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Paquete p) throws SQLException {
        String sql = "UPDATE paquetes SET nombre=?, destino_id=?, duracion_dias=?, descripcion=?, precio_venta=?, capacidad_maxima=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getDestinoId());
            ps.setInt(3, p.getDuracionDias());
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecioVenta());
            ps.setInt(6, p.getCapacidadMaxima());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE paquetes SET activo = false WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean insertarServicio(ServicioPaquete s) throws SQLException {
        String sql = "INSERT INTO servicios_paquete (paquete_id, proveedor_id, descripcion, costo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, s.getPaqueteId());
            ps.setInt(2, s.getProveedorId());
            ps.setString(3, s.getDescripcion());
            ps.setDouble(4, s.getCosto());
            return ps.executeUpdate() > 0;
        }
    }

    public List<ServicioPaquete> listarServiciosPorPaquete(int paqueteId) throws SQLException {
        List<ServicioPaquete> lista = new ArrayList<>();
        String sql = "SELECT sp.*, p.nombre as proveedor_nombre FROM servicios_paquete sp JOIN proveedores p ON sp.proveedor_id = p.id WHERE sp.paquete_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, paqueteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ServicioPaquete s = new ServicioPaquete();
                s.setId(rs.getInt("id"));
                s.setPaqueteId(rs.getInt("paquete_id"));
                s.setProveedorId(rs.getInt("proveedor_id"));
                s.setProveedorNombre(rs.getString("proveedor_nombre"));
                s.setDescripcion(rs.getString("descripcion"));
                s.setCosto(rs.getDouble("costo"));
                lista.add(s);
            }
        }
        return lista;
    }

    public int contarPasajerosEnPaquete(int paqueteId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(r.cantidad_pasajeros), 0) as total FROM reservaciones r WHERE r.paquete_id = ? AND r.estado IN (1,2) AND r.fecha_viaje >= CURDATE()";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, paqueteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    private Paquete mapPaquete(ResultSet rs) throws SQLException {
        Paquete p = new Paquete();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDestinoId(rs.getInt("destino_id"));
        p.setDestinoNombre(rs.getString("destino_nombre"));
        p.setDuracionDias(rs.getInt("duracion_dias"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setCapacidadMaxima(rs.getInt("capacidad_maxima"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}