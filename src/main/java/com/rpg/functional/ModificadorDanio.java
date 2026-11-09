package com.rpg.functional;

import com.rpg.entities.Personaje;

@FunctionalInterface
public interface ModificadorDanio {

    int aplicar(int danioBase, Personaje atacante, Personaje defensor);

    default ModificadorDanio encadenar(ModificadorDanio siguiente) {
        return (danioBase, atacante, defensor) -> {
            int intermedio = this.aplicar(danioBase, atacante, defensor);
            return siguiente.aplicar(intermedio, atacante, defensor);
        };
    }
}
