package com.rpg.controller;

import com.rpg.dao.MonstruoDAO;
import com.rpg.dao.VictoriaDAO;
import com.rpg.model.Monstruo;
import java.util.List;

public class MonstruoController {

    private MonstruoDAO monstruoDAO;
    private VictoriaDAO victoriaDAO;

    public MonstruoController() {
        this.monstruoDAO = new MonstruoDAO();
        this.victoriaDAO = new VictoriaDAO();
    }

    public String crear(String nombre, int vida, int ataque) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del monstruo no puede estar vacio";
        }
        if (vida <= 0) {
            return "La vida debe ser mayor a 0";
        }
        if (ataque <= 0) {
            return "El ataque debe ser mayor a 0";
        }

        Monstruo monstruo = new Monstruo();
        monstruo.setNombre(nombre);
        monstruo.setVida(vida);
        monstruo.setAtaque(ataque);

        boolean insertado = monstruoDAO.insertar(monstruo);
        if (insertado) {
            return "Monstruo creado exitosamente";
        } else {
            return "No se pudo crear el monstruo";
        }
    }

    public List<Monstruo> listar() {
        return monstruoDAO.listar();
    }

    public List<Monstruo> listarTodos() {
        return listar();
    }

    public Monstruo buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        return monstruoDAO.buscarPorNombre(nombre);
    }

    public List<Monstruo> listarPorVidaMinima(int vidaMinima) {
        if (vidaMinima <= 0) {
            return monstruoDAO.listar();
        }
        return monstruoDAO.listarPorVidaMinima(vidaMinima);
    }

    public String actualizar(int id, String nombre, int vida, int ataque) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del monstruo no puede estar vacio";
        }
        if (vida <= 0) {
            return "La vida debe ser mayor a 0";
        }
        if (ataque <= 0) {
            return "El ataque debe ser mayor a 0";
        }

        Monstruo monstruo = new Monstruo();
        monstruo.setId(id);
        monstruo.setNombre(nombre);
        monstruo.setVida(vida);
        monstruo.setAtaque(ataque);

        int filasAfectadas = monstruoDAO.actualizar(monstruo);
        if (filasAfectadas > 0) {
            return "Monstruo actualizado exitosamente";
        } else {
            return "No se encontro el monstruo para actualizar";
        }
    }

    public String eliminar(int id) {
        int victorias = victoriaDAO.contarPorMonstruo(id);
        if (victorias > 0) {
            return "No se puede eliminar el monstruo porque tiene " + victorias + " victorias registradas";
        }

        int eliminados = monstruoDAO.eliminar(id);
        if (eliminados > 0) {
            return "Monstruo eliminado exitosamente";
        } else {
            return "No se encontro el monstruo para eliminar";
        }
    }
}
