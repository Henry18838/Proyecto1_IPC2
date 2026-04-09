package com.horizontes.servlets;

import com.google.gson.Gson;
import com.horizontes.dao.*;
import com.horizontes.models.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@MultipartConfig
public class CargaServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DestinoDAO destinoDAO = new DestinoDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (!esAdmin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(Map.of("error", "Acceso denegado")));
                return;
            }

            Part filePart = request.getPart("archivo");
            if (filePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Archivo no encontrado")));
                return;
            }

            // Leer archivo
            InputStream inputStream = filePart.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            List<String> errores = new ArrayList<>();
            Map<String, Integer> contadores = new LinkedHashMap<>();
            contadores.put("USUARIO", 0);
            contadores.put("DESTINO", 0);
            contadores.put("PROVEEDOR", 0);
            contadores.put("PAQUETE", 0);
            contadores.put("SERVICIO_PAQUETE", 0);
            contadores.put("CLIENTE", 0);
            contadores.put("RESERVACION", 0);
            contadores.put("PAGO", 0);

            String linea;
            int numeroLinea = 0;

            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();

                // Ignorar lineas vacias o comentarios
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                try {
                    if (linea.startsWith("USUARIO(")) {
                        procesarUsuario(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("DESTINO(")) {
                        procesarDestino(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("PROVEEDOR(")) {
                        procesarProveedor(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("PAQUETE(")) {
                        procesarPaquete(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("SERVICIO_PAQUETE(")) {
                        procesarServicioPaquete(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("CLIENTE(")) {
                        procesarCliente(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("RESERVACION(")) {
                        procesarReservacion(linea, numeroLinea, errores, contadores);
                    } else if (linea.startsWith("PAGO(")) {
                        procesarPago(linea, numeroLinea, errores, contadores);
                    } else {
                        errores.add("Linea " + numeroLinea + ": Instruccion desconocida -> " + linea);
                    }
                } catch (Exception e) {
                    errores.add("Linea " + numeroLinea + ": Error inesperado -> " + e.getMessage());
                }
            }

            // Respuesta con resumen
            Map<String, Object> resumen = new LinkedHashMap<>();
            resumen.put("registrosProcesados", contadores);
            resumen.put("totalErrores", errores.size());
            resumen.put("errores", errores);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(resumen));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Error interno: " + e.getMessage())));
        }
    }

    private String[] parsearParametros(String linea) {
        // Extrae los parametros entre parentesis
        int inicio = linea.indexOf('(') + 1;
        int fin = linea.lastIndexOf(')');
        String contenido = linea.substring(inicio, fin);

        List<String> params = new ArrayList<>();
        boolean dentroComillas = false;
        StringBuilder actual = new StringBuilder();

        for (char c : contenido.toCharArray()) {
            if (c == '"') {
                dentroComillas = !dentroComillas;
            } else if (c == ',' && !dentroComillas) {
                params.add(actual.toString().trim());
                actual = new StringBuilder();
            } else {
                actual.append(c);
            }
        }
        params.add(actual.toString().trim());
        return params.toArray(new String[0]);
    }

    private void procesarUsuario(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 3) {
                errores.add("Linea " + numLinea + " [FORMATO]: USUARIO requiere 3 parametros");
                return;
            }

            String nombre = p[0];
            String password = p[1];
            int tipo;

            try {
                tipo = Integer.parseInt(p[2]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Tipo de usuario invalido -> " + p[2]);
                return;
            }

            if (password.length() < 6) {
                errores.add("Linea " + numLinea + " [FORMATO]: Password debe tener minimo 6 caracteres");
                return;
            }

            if (tipo < 1 || tipo > 3) {
                errores.add("Linea " + numLinea + " [FORMATO]: Tipo debe ser 1, 2 o 3");
                return;
            }

            if (usuarioDAO.buscarPorNombre(nombre) != null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Usuario '" + nombre + "' ya existe");
                return;
            }

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setPassword(password);
            u.setRol(tipo);
            usuarioDAO.insertar(u);
            contadores.put("USUARIO", contadores.get("USUARIO") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarDestino(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 3) {
                errores.add("Linea " + numLinea + " [FORMATO]: DESTINO requiere 3 parametros");
                return;
            }

            if (destinoDAO.buscarPorNombre(p[0]) != null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Destino '" + p[0] + "' ya existe");
                return;
            }

            Destino d = new Destino();
            d.setNombre(p[0]);
            d.setPais(p[1]);
            d.setDescripcion(p[2]);
            destinoDAO.insertar(d);
            contadores.put("DESTINO", contadores.get("DESTINO") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarProveedor(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 3) {
                errores.add("Linea " + numLinea + " [FORMATO]: PROVEEDOR requiere 3 parametros");
                return;
            }

            int tipo;
            try {
                tipo = Integer.parseInt(p[1]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Tipo de proveedor invalido -> " + p[1]);
                return;
            }

            if (tipo < 1 || tipo > 5) {
                errores.add("Linea " + numLinea + " [FORMATO]: Tipo debe ser entre 1 y 5");
                return;
            }

            if (proveedorDAO.buscarPorNombre(p[0]) != null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Proveedor '" + p[0] + "' ya existe");
                return;
            }

            Proveedor prov = new Proveedor();
            prov.setNombre(p[0]);
            prov.setTipo(tipo);
            prov.setPais(p[2]);
            proveedorDAO.insertar(prov);
            contadores.put("PROVEEDOR", contadores.get("PROVEEDOR") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarPaquete(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 5) {
                errores.add("Linea " + numLinea + " [FORMATO]: PAQUETE requiere 5 parametros");
                return;
            }

            Destino destino = destinoDAO.buscarPorNombre(p[1]);
            if (destino == null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Destino '" + p[1] + "' no existe");
                return;
            }

            int duracion;
            double precio;
            int capacidad;

            try {
                duracion = Integer.parseInt(p[2]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Duracion invalida -> " + p[2]);
                return;
            }

            try {
                precio = Double.parseDouble(p[3]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Precio invalido -> " + p[3]);
                return;
            }

            try {
                capacidad = Integer.parseInt(p[4]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Capacidad invalida -> " + p[4]);
                return;
            }

            if (duracion <= 0 || precio <= 0 || capacidad <= 0) {
                errores.add("Linea " + numLinea + " [FORMATO]: Duracion, precio y capacidad deben ser mayores a 0");
                return;
            }

            if (paqueteDAO.buscarPorNombre(p[0]) != null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Paquete '" + p[0] + "' ya existe");
                return;
            }

            Paquete paq = new Paquete();
            paq.setNombre(p[0]);
            paq.setDestinoId(destino.getId());
            paq.setDuracionDias(duracion);
            paq.setPrecioVenta(precio);
            paq.setCapacidadMaxima(capacidad);
            paqueteDAO.insertar(paq);
            contadores.put("PAQUETE", contadores.get("PAQUETE") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarServicioPaquete(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 4) {
                errores.add("Linea " + numLinea + " [FORMATO]: SERVICIO_PAQUETE requiere 4 parametros");
                return;
            }

            Paquete paquete = paqueteDAO.buscarPorNombre(p[0]);
            if (paquete == null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Paquete '" + p[0] + "' no existe");
                return;
            }

            Proveedor proveedor = proveedorDAO.buscarPorNombre(p[1]);
            if (proveedor == null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Proveedor '" + p[1] + "' no existe");
                return;
            }

            double costo;
            try {
                costo = Double.parseDouble(p[3]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Costo invalido -> " + p[3]);
                return;
            }

            if (costo < 0) {
                errores.add("Linea " + numLinea + " [FORMATO]: El costo no puede ser negativo");
                return;
            }

            ServicioPaquete s = new ServicioPaquete();
            s.setPaqueteId(paquete.getId());
            s.setProveedorId(proveedor.getId());
            s.setDescripcion(p[2]);
            s.setCosto(costo);
            paqueteDAO.insertarServicio(s);
            contadores.put("SERVICIO_PAQUETE", contadores.get("SERVICIO_PAQUETE") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarCliente(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 6) {
                errores.add("Linea " + numLinea + " [FORMATO]: CLIENTE requiere 6 parametros");
                return;
            }

            if (clienteDAO.buscarPorDpi(p[0]) != null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Cliente con DPI '" + p[0] + "' ya existe");
                return;
            }

            Cliente c = new Cliente();
            c.setDpi(p[0]);
            c.setNombre(p[1]);
            c.setFechaNacimiento(p[2]);
            c.setTelefono(p[3]);
            c.setEmail(p[4]);
            c.setNacionalidad(p[5]);
            clienteDAO.insertar(c);
            contadores.put("CLIENTE", contadores.get("CLIENTE") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarReservacion(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 4) {
                errores.add("Linea " + numLinea + " [FORMATO]: RESERVACION requiere 4 parametros");
                return;
            }

            Paquete paquete = paqueteDAO.buscarPorNombre(p[0]);
            if (paquete == null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Paquete '" + p[0] + "' no existe");
                return;
            }

            Usuario agente = usuarioDAO.buscarPorNombre(p[1]);
            if (agente == null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Usuario '" + p[1] + "' no existe");
                return;
            }

            // Parsear pasajeros
            String[] dpis = p[3].split("\\|");
            List<Cliente> pasajeros = new ArrayList<>();
            for (String dpi : dpis) {
                Cliente cliente = clienteDAO.buscarPorDpi(dpi.trim());
                if (cliente == null) {
                    errores.add("Linea " + numLinea + " [LOGICO]: Cliente con DPI '" + dpi.trim() + "' no existe");
                    return;
                }
                pasajeros.add(cliente);
            }

            Reservacion r = new Reservacion();
            r.setPaqueteId(paquete.getId());
            r.setAgenteId(agente.getId());
            r.setFechaViaje(p[2]);
            r.setCantidadPasajeros(pasajeros.size());
            r.setCostoTotal(paquete.getPrecioVenta() * pasajeros.size());
            r.setNumeroReservacion(reservacionDAO.generarNumeroReservacion());
            r.setPasajeros(pasajeros);

            int reservacionId = reservacionDAO.insertar(r);
            if (reservacionId == -1) {
                errores.add("Linea " + numLinea + " [ERROR]: No se pudo insertar la reservacion");
                return;
            }

            for (Cliente pasajero : pasajeros) {
                reservacionDAO.agregarPasajero(reservacionId, pasajero.getId());
            }

            contadores.put("RESERVACION", contadores.get("RESERVACION") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private void procesarPago(String linea, int numLinea, List<String> errores, Map<String, Integer> contadores) {
        try {
            String[] p = parsearParametros(linea);
            if (p.length != 4) {
                errores.add("Linea " + numLinea + " [FORMATO]: PAGO requiere 4 parametros");
                return;
            }

            Reservacion reservacion = reservacionDAO.buscarPorNumero(p[0]);
            if (reservacion == null) {
                errores.add("Linea " + numLinea + " [LOGICO]: Reservacion '" + p[0] + "' no existe");
                return;
            }

            double monto;
            int metodo;

            try {
                monto = Double.parseDouble(p[1]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Monto invalido -> " + p[1]);
                return;
            }

            try {
                metodo = Integer.parseInt(p[2]);
            } catch (NumberFormatException e) {
                errores.add("Linea " + numLinea + " [FORMATO]: Metodo invalido -> " + p[2]);
                return;
            }

            if (monto <= 0) {
                errores.add("Linea " + numLinea + " [FORMATO]: El monto debe ser mayor a 0");
                return;
            }

            if (metodo < 1 || metodo > 3) {
                errores.add("Linea " + numLinea + " [FORMATO]: Metodo debe ser 1, 2 o 3");
                return;
            }

            Pago pago = new Pago();
            pago.setReservacionId(reservacion.getId());
            pago.setMonto(monto);
            pago.setMetodo(metodo);
            pago.setFechaPago(p[3]);
            pagoDAO.insertar(pago);

            // Verificar si se completo el pago
            double totalPagado = pagoDAO.getTotalPagado(reservacion.getId());
            if (totalPagado >= reservacion.getCostoTotal()) {
                reservacionDAO.actualizarEstado(reservacion.getId(), 2);
            }

            contadores.put("PAGO", contadores.get("PAGO") + 1);

        } catch (Exception e) {
            errores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
        }
    }

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer rol = (Integer) session.getAttribute("usuarioRol");
        return rol != null && rol == 3;
    }
}