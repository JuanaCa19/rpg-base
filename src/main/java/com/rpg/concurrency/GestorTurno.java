package com.rpg.concurrency;

import com.rpg.core.TurnoCallback;

public class GestorTurno {

    private final int segundos;
    private final TurnoCallback callback;
    private Thread hilo;

    public GestorTurno(int segundos, TurnoCallback callback) {
        this.segundos = segundos;
        this.callback = callback;
    }

    public void iniciar() {
        if (hilo != null) {
            throw new IllegalStateException("El temporizador de este turno ya se inicio");
        }
        hilo = new Thread(new TemporizadorTurnoTask(segundos, callback), "temporizador-turno");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void cancelar() {
        if (hilo == null) {
            return;
        }
        hilo.interrupt();
        try {
            hilo.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
