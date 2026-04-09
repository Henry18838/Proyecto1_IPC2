package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.PaqueteDAO;
import com.horizontes.dao.DestinoDAO;
import com.horizontes.dao.ProveedorDAO;
import com.horizontes.models.Paquete;
import com.horizontes.models.ServicioPaquete;
import com.horizontes.models.Proveedor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaqueteServlet extends HttpServlet {

    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    private final DestinoDAO destinoDAO = new DestinoDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final Gson gson = new Gson();

    // GET /api/paquetes -> listar todos
    // GET /api/paquetes/{id} -> buscar por id
    // GET /api/paquetes?destino={id} -> listar por destino
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            String destinoParam = request.getParameter("destino");

            if (pathInfo == null || pathInfo.equals("/")) {
                if (destinoParam != null) {
                    // Listar por destino
                    int destinoId = Integer.parseInt(destinoParam);
                    List<Paquete> paquetes = paqueteDAO.listarPorDestino(destinoId);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(paquetes));
                } else {
                    // Listar todos
                    List<Paquete> paquetes = paqueteDAO.listarTodos();
                    // Agregar info de demanda a cada paquete
                    for (Paquete p : paquetes) {
                        int ocupados = paqueteDAO.contarPasajerosEnPaquete(p.getId());
                        double porcentaje = p.getCapacidadMaxima() > 0
                                ? (ocupados * 100.0 / p.getCapacidadMaxima()) : 0;
                        // Se agrega como campo extra usando Map
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(paquetes));
                }
            } else {
                // Buscar por id con sus servicios
                int id = Integer.parseInt(pathInfo.substring(1));
                Paquete paquete = paqueteDAO.buscarPorId(id);
                if (paquete != null) {
                    // Calcular demanda
                    int ocupados = paqueteDAO.contarPasajerosEnPaquete(id);
                    double porcentaje = paquete.getCapacidadMaxima() > 0
                            ? (ocupados * 100.0 / paquete.getCapacidadMaxima()) : 0;

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("paquete", paquete);
                    resp.put("pasajerosOcupados", ocupados);
                    resp.put("porcentajeOcupacion", Math.round(porcentaje * 100.0) / 100.0);
                    resp.put("altaDemanda", porcentaje >= 80);
                    resp.put("costoTotalAgencia", paquete.getCostoTotalAgencia());
                    resp.put("gananciaBruta", paquete.getGananciaBruta());

                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(resp));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Paquete no encontrado")));
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // POST /api/paquetes -> crear paquete
    // POST /api/paquetes/{id}/servicios -> agregar servicio a paquete
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esOperacionesOAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            // POST /api/paquetes/{id}/servicios
            if (pathInfo != null && pathInfo.contains("/servicios")) {
                String[] parts = pathInfo.split("/");
                int paqueteId = Integer.parseInt(parts[1]);

                Paquete paquete = paqueteDAO.buscarPorId(paqueteId);
                if (paquete == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Paquete no encontrado")));
                    return;
                }

                ServicioPaquete servicio = gson.fromJson(sb.toString(), ServicioPaquete.class);
                servicio.setPaqueteId(paqueteId);

                // Validar proveedor
                Proveedor proveedor = proveedorDAO.buscarPorId(servicio.getProveedorId());
                if (proveedor == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Proveedor no encontrado")));
                    return;
                }

                if (servicio.getCosto() < 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(Map.of("error", "El costo no puede ser negativo")));
                    return;
                }

                boolean ok = paqueteDAO.insertarServicio(servicio);
                if (ok) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(gson.toJson(Map.of("mensaje", "Servicio agregado correctamente")));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "No se pudo agregar el servicio")));
                }
                return;
            }

            // POST /api/paquetes -> crear paquete
            Paquete paquete = gson.fromJson(sb.toString(), Paquete.class);

            // Validaciones
            if (paquete.getNombre() == null || paquete.getNombre().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El nombre es requerido")));
                return;
            }
            if (paquete.getPrecioVenta() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El precio debe ser mayor a 0")));
                return;
            }
            if (paquete.getCapacidadMaxima() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "La capacidad debe ser mayor a 0")));
                return;
            }
            if (paquete.getDuracionDias() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "La duracion debe ser mayor a 0")));
                return;
            }

            // Verificar destino
            if (destinoDAO.buscarPorId(paquete.getDestinoId()) == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Destino no encontrado")));
                return;
            }

            // Verificar nombre unico
            if (paqueteDAO.buscarPorNombre(paquete.getNombre()) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson(Map.of("error", "Ya existe un paquete con ese nombre")));
                return;
            }

            boolean ok = paqueteDAO.insertar(paquete);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("mensaje", "Paquete creado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo crear el paquete")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // PUT /api/paquetes/{id} -> actualizar paquete
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esOperacionesOAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "ID de paquete requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Paquete paquete = gson.fromJson(sb.toString(), Paquete.class);
            paquete.setId(id);

            boolean ok = paqueteDAO.actualizar(paquete);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Paquete actualizado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Paquete no encontrado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // DELETE /api/paquetes/{id} -> desactivar paquete
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esOperacionesOAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "ID de paquete requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            boolean ok = paqueteDAO.desactivar(id);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Paquete desactivado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Paquete no encontrado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    private boolean esOperacionesOAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer rol = (Integer) session.getAttribute("usuarioRol");
        return rol != null && (rol == 2 || rol == 3);
    }
}