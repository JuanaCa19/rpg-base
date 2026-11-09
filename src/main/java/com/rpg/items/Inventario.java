package com.rpg.items;

import com.rpg.exceptions.CapacidadExcedidaException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Contenedor generico con capacidad limitada: Inventario&lt;Pocion&gt; solo acepta pociones. */
public class Inventario<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final List<T> elementos = new ArrayList<>();
    private final int capacidad;

    public Inventario(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva");
        }
        this.capacidad = capacidad;
    }

    public void agregar(T elemento) throws CapacidadExcedidaException {
        if (elementos.size() >= capacidad) {
            throw new CapacidadExcedidaException(capacidad);
        }
        elementos.add(elemento);
    }

    public T obtener(int indice) {
        return elementos.get(indice);
    }

    public T quitar(int indice) {
        return elementos.remove(indice);
    }

    public int tamanio() {
        return elementos.size();
    }

    public boolean estaVacio() {
        return elementos.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return elementos.iterator();
    }

    public Stream<T> stream() {
        return elementos.stream();
    }

    public List<T> filtrar(Predicate<T> criterio) {
        return elementos.stream()
                .filter(criterio)
                .toList();
    }

    public Optional<T> buscarPrimero(Predicate<T> criterio) {
        return elementos.stream()
                .filter(criterio)
                .findFirst();
    }

    public boolean existe(Predicate<T> criterio) {
        return elementos.stream()
                .anyMatch(criterio);
    }

    public void paraCada(Consumer<T> accion) {
        elementos.forEach(accion);
    }

    public <R> List<R> transformar(Function<T, R> transformador) {
        return elementos.stream()
                .map(transformador)
                .toList();
    }

    public long contar(Predicate<T> criterio) {
        return elementos.stream()
                .filter(criterio)
                .count();
    }
}
