package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.ReporteDAO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class ReporteServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final Gson gson = new Gson();

    // GET /api/reportes/{tipo}?fechaInicio=&fechaFin=&formato=csv
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAdmin(request)) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Tipo de reporte requerido")));
                return;
            }

            String tipo = pathInfo.substring(1);
            String fechaInicio = request.getParameter("fechaInicio");
            String fechaFin = request.getParameter("fechaFin");
            String formato = request.getParameter("formato");
            boolean esCSV = "csv".equalsIgnoreCase(formato);

            Object resultado = null;

            switch (tipo) {
                case "ventas":
                    resultado = reporteDAO.reporteVentas(fechaInicio, fechaFin);
                    break;
                case "cancelaciones":
                    resultado = reporteDAO.reporteCancelaciones(fechaInicio, fechaFin);
                    break;
                case "ganancias":
                    resultado = reporteDAO.reporteGanancias(fechaInicio, fechaFin);
                    break;
                case "agente-mas-ventas":
                    resultado = reporteDAO.reporteAgenteMasVentas(fechaInicio, fechaFin);
                    break;
                case "agente-mas-ganancias":
                    resultado = reporteDAO.reporteAgenteMasGanancias(fechaInicio, fechaFin);
                    break;
                case "paquete-mas-vendido":
                    resultado = reporteDAO.reportePaqueteMasVendido(fechaInicio, fechaFin);
                    break;
                case "paquete-menos-vendido":
                    resultado = reporteDAO.reportePaqueteMenosVendido(fechaInicio, fechaFin);
                    break;
                case "ocupacion-destino":
                    resultado = reporteDAO.reporteOcupacionPorDestino(fechaInicio, fechaFin);
                    break;
                default:
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(Map.of("error", "Tipo de reporte invalido")));
                    return;
            }

            if (esCSV) {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=reporte-" + tipo + ".csv");
                out.print(convertirACSV(tipo, resultado));
            } else {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(resultado));
            }

        } catch (Exception e) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    private String convertirACSV(String tipo, Object datos) {
        String json = gson.toJson(datos);
        // Conversion basica a CSV usando el JSON
        StringBuilder csv = new StringBuilder();
        csv.append("Reporte: ").append(tipo).append("\n");
        csv.append(json);
        return csv.toString();
    }

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer rol = (Integer) session.getAttribute("usuarioRol");
        return rol != null && rol == 3;
    }
}