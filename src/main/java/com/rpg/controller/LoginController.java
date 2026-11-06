package com.rpg.controller;

import com.rpg.dao.JugadorDAO;
import com.rpg.model.Jugador;

public class LoginController {

    private JugadorDAO jugadorDAO;
    private int intentosFallidos;
    private Jugador jugadorActual;

    public LoginController() {
        this.jugadorDAO = new JugadorDAO();
        this.intentosFallidos = 0;
        this.jugadorActual = null;
    }

    public String registrar(String usuario, String clave) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return "El nombre de usuario no puede estar vacio";
        }
        if (clave == null || clave.trim().isEmpty()) {
            return "La clave no puede estar vacia";
        }
        if (clave.length() < 4) {
            return "La clave debe tener al menos 4 caracteres";
        }

        Jugador nuevoJugador = new Jugador(usuario, clave, "jugador");
        boolean registrado = jugadorDAO.registrar(nuevoJugador);
        if (registrado) {
            return "Jugador registrado exitosamente";
        } else {
            return "No se pudo registrar el jugador. El usuario ya existe";
        }
    }

    public String login(String usuario, String clave) {
        if (intentosFallidos >= 3) {
            return "Sesion bloqueada por demasiados intentos fallidos";
        }

        Jugador jugador = jugadorDAO.autenticar(usuario, clave);

        if (jugador == null) {
            intentosFallidos++;
            return "Usuario o clave incorrectos. Intento " + intentosFallidos + " de 3";
        } else {
            intentosFallidos = 0;
            jugadorActual = jugador;
            return "Bienvenido " + usuario;
        }
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public boolean esAdmin() {
        return jugadorActual != null && jugadorActual.getRol().equals("admin");
    }

    public boolean estaLogueado() {
        return jugadorActual != null;
    }

    public boolean estaBloqueado() {
        return intentosFallidos >= 3;
    }

    public String cerrarSesion() {
        jugadorActual = null;
        return "Sesion cerrada";
    }
}
