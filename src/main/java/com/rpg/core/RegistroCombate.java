package com.rpg.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RegistroCombate {

    private final StringBuilder texto = new StringBuilder();
    private final List<String> lineas = new ArrayList<>();

    public synchronized void agregar(String linea) {
        System.out.println(linea);
        texto.append(linea).append(System.lineSeparator());
        lineas.add(linea);
    }

    public void agregar(String formato, Object... argumentos) {
        agregar(String.format(formato, argumentos));
    }

    public synchronized void limpiar() {
        texto.setLength(0);
        lineas.clear();
    }

    public synchronized String getTexto() {
        return texto.toString();
    }

    public synchronized List<String> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public synchronized List<String> filtrar(Predicate<String> criterio) {
        return lineas.stream()
                .filter(criterio)
                .toList();
    }

    public synchronized long contarMenciones(String termino) {
        return lineas.stream()
                .filter(linea -> linea.toLowerCase().contains(termino.toLowerCase()))
                .count();
    }

    public synchronized boolean contieneEvento(Predicate<String> criterio) {
        return lineas.stream()
                .anyMatch(criterio);
    }

    public synchronized void paraCadaLinea(Consumer<String> accion) {
        lineas.forEach(accion);
    }
}
