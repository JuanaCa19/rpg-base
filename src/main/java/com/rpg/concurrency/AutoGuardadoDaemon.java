package com.rpg.concurrency;

public class AutoGuardadoDaemon implements Runnable {

    private final long intervaloMs;
    private final Runnable guardado;

    public AutoGuardadoDaemon(long intervaloMs, Runnable guardado) {
        this.intervaloMs = intervaloMs;
        this.guardado = guardado;
    }

    public Thread iniciar() {
        Thread hilo = new Thread(this, "auto-guardado");
        hilo.setDaemon(true);
        hilo.start();
        return hilo;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                guardado.run();
            } catch (RuntimeException e) {
                System.out.println("[Autoguardado omitido: " + e + "]");
            }
        }
    }
}
