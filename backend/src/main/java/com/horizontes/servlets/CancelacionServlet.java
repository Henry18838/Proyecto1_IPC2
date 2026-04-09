package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.CancelacionDAO;
import com.horizontes.dao.PagoDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.models.Cancelacion;
import com.horizontes.models.Reservacion;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class CancelacionServlet extends HttpServlet {

    private final CancelacionDAO cancelacionDAO = new CancelacionDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final Gson gson = new Gson();

    // POST /api/cancelaciones -> procesar cancelacion
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

            Map<?, ?> body = gson.fromJson(sb.toString(), Map.class);
            String numeroReservacion = (String) body.get("numeroReservacion");

            if (numeroReservacion == null || numeroReservacion.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Numero de reservacion requerido")));
                return;
            }

            // Buscar reservacion
            Reservacion reservacion = reservacionDAO.buscarPorNumero(numeroReservacion);
            if (reservacion == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Reservacion no encontrada")));
                return;
            }

            // Verificar estado (solo Pendiente o Confirmada)
            if (reservacion.getEstado() != 1 && reservacion.getEstado() != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Solo se pueden cancelar reservaciones Pendientes o Confirmadas")));
                return;
            }

            // Calcular dias hasta el viaje
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fechaViaje = LocalDate.parse(reservacion.getFechaViaje().substring(0, 10), formatter);
            LocalDate hoy = LocalDate.now();
            long diasDiferencia = ChronoUnit.DAYS.between(hoy, fechaViaje);

            // Verificar que falten al menos 7 dias
            if (diasDiferencia < 7) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "No se puede cancelar con menos de 7 dias de anticipacion")));
                return;
            }

            // Calcular porcentaje de reembolso
            double porcentaje;
            if (diasDiferencia > 30) {
                porcentaje = 100.0;
            } else if (diasDiferencia >= 15) {
                porcentaje = 70.0;
            } else {
                porcentaje = 40.0;
            }

            // Calcular montos
            double totalPagado = pagoDAO.getTotalPagado(reservacion.getId());
            double montoReembolsado = totalPagado * (porcentaje / 100.0);
            double perdidaAgencia = totalPagado - montoReembolsado;

            // Registrar cancelacion
            Cancelacion cancelacion = new Cancelacion();
            cancelacion.setReservacionId(reservacion.getId());
            cancelacion.setMontoReembolsado(montoReembolsado);
            cancelacion.setPorcentajeReembolso(porcentaje);
            cancelacion.setPerdidaAgencia(perdidaAgencia);

            boolean okCancelacion = cancelacionDAO.insertar(cancelacion);
            if (!okCancelacion) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo registrar la cancelacion")));
                return;
            }

            // Cambiar estado a Cancelada
            reservacionDAO.actualizarEstado(reservacion.getId(), 3);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(Map.of(
                "mensaje", "Cancelacion procesada correctamente",
                "numeroReservacion", numeroReservacion,
                "diasAnticipacion", diasDiferencia,
                "porcentajeReembolso", porcentaje,
                "totalPagado", totalPagado,
                "montoReembolsado", montoReembolsado,
                "perdidaAgencia", perdidaAgencia
            )));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // GET /api/cancelaciones -> listar cancelaciones
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

            String fechaInicio = request.getParameter("fechaInicio");
            String fechaFin = request.getParameter("fechaFin");

            if (fechaInicio != null && fechaFin != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(cancelacionDAO.listarPorIntervalo(fechaInicio, fechaFin)));
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(cancelacionDAO.listarTodas()));
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