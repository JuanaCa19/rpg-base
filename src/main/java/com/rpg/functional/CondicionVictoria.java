package com.rpg.functional;

import com.rpg.entities.Escuadron;

@FunctionalInterface
public interface CondicionVictoria {

    boolean evaluar(Escuadron aliados, Escuadron rivales);
}
