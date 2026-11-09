package com.rpg.functional;

import com.rpg.entities.Personaje;

@FunctionalInterface
public interface HabilidadFuncional {

    String ejecutar(Personaje lanzador, Personaje objetivo);
}
