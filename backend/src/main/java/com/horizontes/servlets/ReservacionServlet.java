package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.ClienteDAO;
import com.horizontes.dao.PaqueteDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.models.Cliente;
import com.horizontes.models.Paquete;
import com.horizontes.models.Reservacion;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ReservacionServlet extends HttpServlet {

    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    private final Gson gson = new Gson();

    // GET /api/reservaciones -> listar todas
    // GET /api/reservaciones/{numero} -> buscar por numero
    // GET /api/reservaciones?hoy=true -> reservaciones del dia
    // GET /api/reservaciones?clienteId={id} -> historial de cliente
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAtencionOAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            String hoy = request.getParameter("hoy");
            String clienteId = request.getParameter("clienteId");

            if (pathInfo == null || pathInfo.equals("/")) {
                if ("true".equals(hoy)) {
                    // Reservaciones del dia
                    List<Reservacion> lista = reservacionDAO.listarPorFechaHoy();
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(lista));
                } else if (clienteId != null) {
                    // Historial de cliente
                    List<Reservacion> lista = reservacionDAO.listarPorCliente(Integer.parseInt(clienteId));
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(lista));
                } else {
                    // Listar todas
                    List<Reservacion> lista = reservacionDAO.listarTodas();
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(lista));
                }
            } else {
                // Buscar por numero de reservacion
                String numero = pathInfo.substring(1);
                Reservacion reservacion = reservacionDAO.buscarPorNumero(numero);
                if (reservacion != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(reservacion));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Reservacion no encontrada")));
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // POST /api/reservaciones -> crear reservacion
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAtencionOAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Reservacion reservacion = gson.fromJson(sb.toString(), Reservacion.class);

            // Validaciones basicas
            if (reservacion.getFechaViaje() == null || reservacion.getFechaViaje().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "La fecha de viaje es requerida")));
                return;
            }

            // Verificar paquete
            Paquete paquete = paqueteDAO.buscarPorId(reservacion.getPaqueteId());
            if (paquete == null || !paquete.isActivo()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Paquete no encontrado o inactivo")));
                return;
            }

            // Verificar capacidad
            int ocupados = paqueteDAO.contarPasajerosEnPaquete(paquete.getId());
            if (ocupados + reservacion.getCantidadPasajeros() > paquete.getCapacidadMaxima()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "No hay suficiente capacidad en el paquete")));
                return;
            }

            // Verificar pasajeros
            if (reservacion.getPasajeros() == null || reservacion.getPasajeros().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Debe incluir al menos un pasajero")));
                return;
            }

            // Obtener agente de la sesion
            HttpSession session = request.getSession(false);
            int agenteId = (Integer) session.getAttribute("usuarioId");
            reservacion.setAgenteId(agenteId);

            // Calcular costo total
            double costoTotal = paquete.getPrecioVenta() * reservacion.getCantidadPasajeros();
            reservacion.setCostoTotal(costoTotal);

            // Generar numero de reservacion
            String numero = reservacionDAO.generarNumeroReservacion();
            reservacion.setNumeroReservacion(numero);

            // Insertar reservacion
            int reservacionId = reservacionDAO.insertar(reservacion);
            if (reservacionId == -1) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo crear la reservacion")));
                return;
            }

            // Agregar pasajeros
            for (Cliente pasajero : reservacion.getPasajeros()) {
                Cliente clienteExistente = clienteDAO.buscarPorDpi(pasajero.getDpi());
                if (clienteExistente == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Cliente con DPI " + pasajero.getDpi() + " no encontrado")));
                    return;
                }
                reservacionDAO.agregarPasajero(reservacionId, clienteExistente.getId());
            }

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(Map.of(
                "mensaje", "Reservacion creada correctamente",
                "numeroReservacion", numero,
                "costoTotal", costoTotal
            )));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // PUT /api/reservaciones/{numero} -> actualizar estado
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAtencionOAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Numero de reservacion requerido")));
                return;
            }

            String numero = pathInfo.substring(1);
            Reservacion reservacion = reservacionDAO.buscarPorNumero(numero);

            if (reservacion == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Reservacion no encontrada")));
                return;
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Map<?, ?> body = gson.fromJson(sb.toString(), Map.class);
            int estado = ((Double) body.get("estado")).intValue();

            boolean ok = reservacionDAO.actualizarEstado(reservacion.getId(), estado);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Estado actualizado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo actualizar el estado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    private boolean esAtencionOAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer rol = (Integer) session.getAttribute("usuarioRol");
        return rol != null && (rol == 1 || rol == 3);
    }
}