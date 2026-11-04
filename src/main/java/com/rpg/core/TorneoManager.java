package com.rpg.core;

import com.rpg.entities.Escuadron;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.Personaje;

import java.util.List;

public class TorneoManager {

    private static final long ESPERA_MUESTRA_ESTADO_MS = 300;

    private final Arena arena;
    private final RegistroCombate registro;

    public TorneoManager(Arena arena, RegistroCombate registro) {
        this.arena = arena;
        this.registro = registro;
    }

    public Personaje jugarTorneo() {
        List<Personaje> gladiadores = crearGladiadores().getMiembros();

        Personaje[] finalistas = new Personaje[2];
        Thread semifinalA = new Thread(
                () -> finalistas[0] = simularSemifinal(gladiadores.get(0), gladiadores.get(1)), "semifinal-A");
        Thread semifinalB = new Thread(
                () -> finalistas[1] = simularSemifinal(gladiadores.get(2), gladiadores.get(3)), "semifinal-B");

        registro.agregar("Estado de los hilos antes de start(): A=%s, B=%s", semifinalA.getState(), semifinalB.getState());
        semifinalA.start();
        semifinalB.start();
        try {
            Thread.sleep(ESPERA_MUESTRA_ESTADO_MS);
            registro.agregar("Estado de los hilos durante la ejecucion: A=%s, B=%s",
                    semifinalA.getState(), semifinalB.getState());
            semifinalA.join();
            semifinalB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            semifinalA.interrupt();
            semifinalB.interrupt();
            registro.agregar("Torneo interrumpido antes de terminar las semifinales");
            return null;
        }
        registro.agregar("Estado de los hilos despues de join(): A=%s, B=%s", semifinalA.getState(), semifinalB.getState());

        if (finalistas[0] == null || finalistas[1] == null) {
            registro.agregar("Una semifinal fallo: no hay final");
            return null;
        }
        return simularFinal(finalistas[0], finalistas[1]);
    }

    public Personaje simularSemifinal(Personaje a, Personaje b) {
        registro.agregar("=== SEMIFINAL: %s vs %s ===", a.getNombre(), b.getNombre());
        Personaje ganador = arena.duelo(a, b);
        registro.agregar("Clasifica a la final: %s", ganador.getNombre());
        return ganador;
    }

    private Personaje simularFinal(Personaje a, Personaje b) {
        a.restaurarVida();
        b.restaurarVida();
        registro.agregar("=== FINAL: %s vs %s ===", a.getNombre(), b.getNombre());
        return arena.duelo(a, b);
    }

    private Escuadron crearGladiadores() {
        Escuadron escuadron = new Escuadron("Gladiadores");
        escuadron.agregar(new Guerrero("Maximo"));
        escuadron.agregar(new Mago("Circe"));
        escuadron.agregar(new Guerrero("Aquiles"));
        escuadron.agregar(new Mago("Merlina"));
        return escuadron;
    }
}
