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
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Leer el body JSON que manda Angular
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            Map<?, ?> body = gson.fromJson(sb.toString(), Map.class);
            String nombre = (String) body.get("nombre");
            String password = (String) body.get("password");

            if (nombre == null || password == null || nombre.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Usuario y password son requeridos")));
                return;
            }

            Usuario usuario = usuarioDAO.login(nombre, password);

            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson(Map.of("error", "Usuario o password incorrectos")));
                return;
            }

            // Crear sesion
            HttpSession session = request.getSession(true);
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNombre", usuario.getNombre());
            session.setAttribute("usuarioRol", usuario.getRol());

            // Responder con datos del usuario (sin password)
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", usuario.getId());
            resp.put("nombre", usuario.getNombre());
            resp.put("rol", usuario.getRol());
            resp.put("mensaje", "Login exitoso");

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(resp));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(Map.of("mensaje", "Sesion cerrada correctamente")));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error al cerrar sesion")));
        }
    }
}