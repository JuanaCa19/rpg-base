package com.rpg.model;

public class Victoria {

    private int id;
    private int jugadorId;
    private int monstruoId;
    private String fecha;
    private String usuario;
    private String nombreMonstruo;

    public Victoria() {
    }

    public Victoria(int jugadorId, int monstruoId) {
        this.jugadorId = jugadorId;
        this.monstruoId = monstruoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(int jugadorId) {
        this.jugadorId = jugadorId;
    }

    public int getMonstruoId() {
        return monstruoId;
    }

    public void setMonstruoId(int monstruoId) {
        this.monstruoId = monstruoId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombreMonstruo() {
        return nombreMonstruo;
    }

    public void setNombreMonstruo(String nombreMonstruo) {
        this.nombreMonstruo = nombreMonstruo;
    }
}
