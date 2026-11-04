package com.rpg.exceptions;

/** Base de las excepciones verificadas del dominio del juego. */
public abstract class JuegoException extends Exception {

    protected JuegoException(String mensaje) {
        super(mensaje);
    }

    protected JuegoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
