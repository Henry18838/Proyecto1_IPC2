package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.UsuarioDAO;
import com.horizontes.models.Usuario;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();

    // GET /api/usuarios -> listar todos
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Verificar que sea administrador
            if (!esAdministrador(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            List<Usuario> usuarios = usuarioDAO.listarTodos();
            // Limpiar passwords antes de enviar
            usuarios.forEach(u -> u.setPassword(null));
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(usuarios));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // POST /api/usuarios -> crear usuario
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAdministrador(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Usuario usuario = gson.fromJson(sb.toString(), Usuario.class);

            // Validaciones
            if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El nombre es requerido")));
                return;
            }
            if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "La password debe tener minimo 6 caracteres")));
                return;
            }
            if (usuario.getRol() < 1 || usuario.getRol() > 3) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "El rol debe ser 1, 2 o 3")));
                return;
            }

            // Verificar que no exista
            if (usuarioDAO.buscarPorNombre(usuario.getNombre()) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson(Map.of("error", "Ya existe un usuario con ese nombre")));
                return;
            }

            boolean ok = usuarioDAO.insertar(usuario);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("mensaje", "Usuario creado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "No se pudo crear el usuario")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // PUT /api/usuarios/{id} -> actualizar rol
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAdministrador(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "ID de usuario requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Map<?, ?> body = gson.fromJson(sb.toString(), Map.class);
            int rol = ((Double) body.get("rol")).intValue();

            boolean ok = usuarioDAO.actualizarRol(id, rol);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Rol actualizado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Usuario no encontrado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    // DELETE /api/usuarios/{id} -> desactivar usuario
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAdministrador(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "ID de usuario requerido")));
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));

            boolean ok = usuarioDAO.desactivar(id);
            if (ok) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(Map.of("mensaje", "Usuario desactivado correctamente")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Usuario no encontrado")));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer rol = (Integer) session.getAttribute("usuarioRol");
        return rol != null && rol == 3;
    }
}
