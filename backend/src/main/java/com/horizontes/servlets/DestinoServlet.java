package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.DestinoDAO;
import com.horizontes.models.Destino;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class DestinoServlet extends HttpServlet {

    private final DestinoDAO destinoDAO = new DestinoDAO();
    private final Gson gson = new Gson();

    // GET /api/destinos -> listar todos
    // GET /api/destinos/{id} -> buscar por id
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Listar todos
                List<Destino> destinos = destinoDAO.listarTodos();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(destinos));
            } else {
                // Buscar por id
                int id = Integer.parseInt(pathInfo.substring(1));
                Destino destino = destinoDAO.buscarPorId(id);
                if (destino != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(destino));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Destino no encontrado")));
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // POST /api/destinos -> crear destino
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

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Destino destino = gson.fromJson(sb.toString(), Destino.class);

            // Validaciones
            if (destino.getNombre() == null || destino.getNombre().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El nombre es requerido")));
                return;
            }
            if (destino.getPais() == null || destino.getPais().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El pais es requerido")));
                return;
            }

            // Verificar que no exista
            if (destinoDAO.buscarPorNombre(destino.getNombre()) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson(Map.of("error", "Ya existe un destino con ese nombre")));
                return;
            }

            boolean ok = destinoDAO.insertar(destino);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("mensaje", "Destino creado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo crear el destino")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // PUT /api/destinos/{id} -> actualizar destino
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
                out.print(gson.toJson(Map.of("error", "ID de destino requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Destino destino = gson.fromJson(sb.toString(), Destino.class);
            destino.setId(id);

            boolean ok = destinoDAO.actualizar(destino);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Destino actualizado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Destino no encontrado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // DELETE /api/destinos/{id} -> eliminar destino
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
                out.print(gson.toJson(Map.of("error", "ID de destino requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            boolean ok = destinoDAO.eliminar(id);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Destino eliminado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Destino no encontrado")));
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