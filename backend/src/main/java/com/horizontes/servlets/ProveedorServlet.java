package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.ProveedorDAO;
import com.horizontes.models.Proveedor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ProveedorServlet extends HttpServlet {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final Gson gson = new Gson();

    // GET /api/proveedores -> listar todos
    // GET /api/proveedores/{id} -> buscar por id
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                List<Proveedor> proveedores = proveedorDAO.listarTodos();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(proveedores));
            } else {
                int id = Integer.parseInt(pathInfo.substring(1));
                Proveedor proveedor = proveedorDAO.buscarPorId(id);
                if (proveedor != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(proveedor));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(Map.of("error", "Proveedor no encontrado")));
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // POST /api/proveedores -> crear proveedor
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

            Proveedor proveedor = gson.fromJson(sb.toString(), Proveedor.class);

            // Validaciones
            if (proveedor.getNombre() == null || proveedor.getNombre().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El nombre es requerido")));
                return;
            }
            if (proveedor.getTipo() < 1 || proveedor.getTipo() > 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El tipo debe ser entre 1 y 5")));
                return;
            }

            // Verificar que no exista
            if (proveedorDAO.buscarPorNombre(proveedor.getNombre()) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson(Map.of("error", "Ya existe un proveedor con ese nombre")));
                return;
            }

            boolean ok = proveedorDAO.insertar(proveedor);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("mensaje", "Proveedor creado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo crear el proveedor")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // PUT /api/proveedores/{id} -> actualizar proveedor
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
                out.print(gson.toJson(Map.of("error", "ID de proveedor requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Proveedor proveedor = gson.fromJson(sb.toString(), Proveedor.class);
            proveedor.setId(id);

            boolean ok = proveedorDAO.actualizar(proveedor);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Proveedor actualizado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Proveedor no encontrado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // DELETE /api/proveedores/{id} -> eliminar proveedor
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
                out.print(gson.toJson(Map.of("error", "ID de proveedor requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            boolean ok = proveedorDAO.eliminar(id);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Proveedor eliminado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Proveedor no encontrado")));
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