package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.PagoDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.models.Pago;
import com.horizontes.models.Reservacion;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class PagoServlet extends HttpServlet {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final Gson gson = new Gson();

    // GET /api/pagos?reservacionId={id} -> listar pagos de una reservacion
    // GET /api/pagos/comprobante/{numero} -> generar PDF
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAtencionOAdmin(request)) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();

            // GET /api/pagos/comprobante/{numero}
            if (pathInfo != null && pathInfo.startsWith("/comprobante/")) {
                String numero = pathInfo.substring("/comprobante/".length());
                Reservacion reservacion = reservacionDAO.buscarPorNumero(numero);

                if (reservacion == null) {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Reservacion no encontrada")));
                    return;
                }

                List<Pago> pagos = pagoDAO.listarPorReservacion(reservacion.getId());
                double totalPagado = pagoDAO.getTotalPagado(reservacion.getId());

                // Generar PDF
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=comprobante-" + numero + ".pdf");

                Document document = new Document();
                PdfWriter.getInstance(document, response.getOutputStream());
                document.open();

                // Titulo
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

                Paragraph titulo = new Paragraph("HORIZONTES SIN LIMITES", titleFont);
                titulo.setAlignment(Element.ALIGN_CENTER);
                document.add(titulo);

                Paragraph subtitulo = new Paragraph("Comprobante de Pago", headerFont);
                subtitulo.setAlignment(Element.ALIGN_CENTER);
                document.add(subtitulo);

                document.add(new Paragraph(" "));

                // Datos de la reservacion
                document.add(new Paragraph("Numero de Reservacion: " + reservacion.getNumeroReservacion(), headerFont));
                document.add(new Paragraph("Paquete: " + reservacion.getPaqueteNombre(), normalFont));
                document.add(new Paragraph("Agente: " + reservacion.getAgenteNombre(), normalFont));
                document.add(new Paragraph("Fecha de Viaje: " + reservacion.getFechaViaje(), normalFont));
                document.add(new Paragraph("Cantidad de Pasajeros: " + reservacion.getCantidadPasajeros(), normalFont));
                document.add(new Paragraph("Costo Total: Q." + String.format("%.2f", reservacion.getCostoTotal()), normalFont));
                document.add(new Paragraph("Estado: " + reservacion.getEstadoTexto(), normalFont));

                document.add(new Paragraph(" "));
                document.add(new Paragraph("DETALLE DE PAGOS", headerFont));

                // Tabla de pagos
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Fecha", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Metodo", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Monto", headerFont)));
                table.addCell(new PdfPCell(new Phrase("Registrado", headerFont)));

                for (Pago pago : pagos) {
                    table.addCell(pago.getFechaPago());
                    table.addCell(pago.getMetodoTexto());
                    table.addCell("Q." + String.format("%.2f", pago.getMonto()));
                    table.addCell(pago.getFechaRegistro());
                }

                document.add(table);

                document.add(new Paragraph(" "));
                document.add(new Paragraph("Total Pagado: Q." + String.format("%.2f", totalPagado), headerFont));
                document.add(new Paragraph("Saldo Pendiente: Q." + String.format("%.2f", reservacion.getCostoTotal() - totalPagado), headerFont));

                document.close();
                return;
            }

            // GET /api/pagos?reservacionId={id}
            response.setContentType("application/json");
            String reservacionId = request.getParameter("reservacionId");
            if (reservacionId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "reservacionId es requerido")));
                return;
            }

            List<Pago> pagos = pagoDAO.listarPorReservacion(Integer.parseInt(reservacionId));
            double totalPagado = pagoDAO.getTotalPagado(Integer.parseInt(reservacionId));

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(Map.of(
                "pagos", pagos,
                "totalPagado", totalPagado
            )));

        } catch (Exception e) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // POST /api/pagos -> registrar pago
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

            Pago pago = gson.fromJson(sb.toString(), Pago.class);

            // Validaciones
            if (pago.getMonto() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El monto debe ser mayor a 0")));
                return;
            }
            if (pago.getMetodo() < 1 || pago.getMetodo() > 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Metodo de pago invalido")));
                return;
            }
            if (pago.getFechaPago() == null || pago.getFechaPago().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "La fecha de pago es requerida")));
                return;
            }

            // Verificar reservacion
            Reservacion reservacion = reservacionDAO.buscarPorId(pago.getReservacionId());
            if (reservacion == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Reservacion no encontrada")));
                return;
            }

            // Verificar que no este cancelada o completada
            if (reservacion.getEstado() == 3 || reservacion.getEstado() == 4) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "No se puede pagar una reservacion " + reservacion.getEstadoTexto())));
                return;
            }

            // Registrar pago
            boolean ok = pagoDAO.insertar(pago);
            if (!ok) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo registrar el pago")));
                return;
            }

            // Verificar si ya se cubrió el total
            double totalPagado = pagoDAO.getTotalPagado(pago.getReservacionId());
            boolean pagadoCompleto = totalPagado >= reservacion.getCostoTotal();

            if (pagadoCompleto) {
                // Cambiar estado a Confirmada
                reservacionDAO.actualizarEstado(reservacion.getId(), 2);
            }

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(Map.of(
                "mensaje", "Pago registrado correctamente",
                "totalPagado", totalPagado,
                "costoTotal", reservacion.getCostoTotal(),
                "saldoPendiente", reservacion.getCostoTotal() - totalPagado,
                "reservacionConfirmada", pagadoCompleto
            )));

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