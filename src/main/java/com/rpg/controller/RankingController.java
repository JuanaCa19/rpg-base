package com.rpg.controller;

import com.rpg.dao.VictoriaDAO;
import com.rpg.model.Victoria;
import java.util.List;

public class RankingController {

    private VictoriaDAO victoriaDAO;

    public RankingController() {
        this.victoriaDAO = new VictoriaDAO();
    }

    public String registrarVictoria(int jugadorId, int monstruoId) {
        boolean registrada = victoriaDAO.registrar(jugadorId, monstruoId);
        if (registrada) {
            return "Victoria registrada exitosamente";
        } else {
            return "No se pudo registrar la victoria";
        }
    }

    public List<String[]> obtenerRanking() {
        return victoriaDAO.rankingPorJugador();
    }

    public List<Victoria> obtenerVictoriasDeJugador(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return null;
        }
        return victoriaDAO.victoriasDeJugador(usuario);
    }
}
