package com.rpg.exceptions;

public class PersistenciaException extends JuegoException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
