package com.rpg.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Agregacion: disolver el escuadron no destruye a los personajes. */
public class Escuadron {

    private final String nombre;
    private final List<Personaje> miembros = new ArrayList<>();

    public Escuadron(String nombre) {
        this.nombre = nombre;
    }

    public boolean agregar(Personaje personaje) {
        if (personaje == null || miembros.contains(personaje)) {
            return false;
        }
        return miembros.add(personaje);
    }

    public boolean remover(Personaje personaje) {
        return miembros.remove(personaje);
    }

    public int contarVivos() {
        return (int) miembros.stream()
                .filter(Personaje::estaVivo)
                .count();
    }

    public List<Personaje> obtenerVivos() {
        return miembros.stream()
                .filter(Personaje::estaVivo)
                .toList();
    }

    public List<Personaje> obtenerDerrotados() {
        return miembros.stream()
                .filter(Predicate.not(Personaje::estaVivo))
                .toList();
    }

    public List<String> obtenerNombres() {
        return miembros.stream()
                .map(Personaje::getNombre)
                .toList();
    }

    public List<Personaje> filtrar(Predicate<Personaje> criterio) {
        return miembros.stream()
                .filter(criterio)
                .toList();
    }

    public Optional<Personaje> buscarMiembro(Predicate<Personaje> criterio) {
        return miembros.stream()
                .filter(criterio)
                .findFirst();
    }

    public boolean haySobrevivientes() {
        return miembros.stream()
                .anyMatch(Personaje::estaVivo);
    }

    public boolean todosVivos() {
        return miembros.stream()
                .allMatch(Personaje::estaVivo);
    }

    public boolean ningunoVivo() {
        return miembros.stream()
                .noneMatch(Personaje::estaVivo);
    }

    public void aplicarATodos(Consumer<Personaje> accion) {
        miembros.forEach(accion);
    }

    public int calcularVidaTotal() {
        return miembros.stream()
                .mapToInt(Personaje::getVida)
                .sum();
    }

    public double calcularVidaPromedio() {
        return miembros.stream()
                .filter(Personaje::estaVivo)
                .mapToInt(Personaje::getVida)
                .average()
                .orElse(0.0);
    }

    public Optional<Personaje> obtenerMiembroConMasVida() {
        return miembros.stream()
                .max(Comparator.comparingInt(Personaje::getVida));
    }

    public List<String> obtenerTodasLasHabilidades() {
        return miembros.stream()
                .flatMap(personaje -> personaje.getHabilidades().stream())
                .distinct()
                .sorted()
                .toList();
    }

    public void disolver() {
        miembros.clear();
    }

    public String getNombre() {
        return nombre;
    }

    public List<Personaje> getMiembros() {
        return Collections.unmodifiableList(miembros);
    }
}
