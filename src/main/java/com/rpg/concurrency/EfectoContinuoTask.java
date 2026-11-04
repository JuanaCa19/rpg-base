package com.rpg.concurrency;

import com.rpg.entities.EstadoEfecto;
import com.rpg.entities.Personaje;

public class EfectoContinuoTask implements Runnable {

    private final Personaje objetivo;
    private final EstadoEfecto efecto;
    private final long intervaloMs;

    public EfectoContinuoTask(Personaje objetivo, EstadoEfecto efecto, long intervaloMs) {
        this.objetivo = objetivo;
        this.efecto = efecto;
        this.intervaloMs = intervaloMs;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !efecto.haTerminado()) {
                try {
                    Thread.sleep(intervaloMs);
                } catch (InterruptedException e) {
                    objetivo.finalizarEfecto(efecto);
                    Thread.currentThread().interrupt();
                    break;
                }
                if (!objetivo.estaVivo()) {
                    break;
                }
                aplicarTick();
            }
        } finally {
            objetivo.finalizarEfecto(efecto);
        }
    }

    private void aplicarTick() {
        String nombre = objetivo.getNombre();
        int valor = efecto.getValorPorTick();
        if (efecto.esDanio()) {
            objetivo.recibirDanio(valor);
            objetivo.notificar(String.format("%s sufre %d de dano por %s", nombre, valor, efecto.getNombre()));
        } else {
            int curado = objetivo.curar(valor);
            objetivo.notificar(String.format("%s recupera %d de vida por %s", nombre, curado, efecto.getNombre()));
        }
        efecto.consumirTurno();
        if (!objetivo.estaVivo()) {
            objetivo.notificar(String.format("%s cae derrotado por sus efectos", nombre));
        } else if (efecto.haTerminado()) {
            objetivo.notificar(String.format("%s: termina %s", nombre, efecto.getNombre()));
        }
    }
}
