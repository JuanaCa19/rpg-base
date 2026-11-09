package com.rpg.entities;

import com.rpg.entities.enums.TipoPersonaje;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Agregacion: el gremio guarda referencias a personajes que existen por su cuenta. */
public class Gremio implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final List<Personaje> miembros = new ArrayList<>();

    public Gremio(String nombre) {
        this.nombre = nombre;
    }

    public void agregarMiembro(Personaje personaje) {
        if (personaje == null || miembros.contains(personaje)) {
            return;
        }
        miembros.add(personaje);
        if (personaje.getGremio() != this) {
            personaje.unirseAGremio(this);
        }
    }

    public void removerMiembro(Personaje personaje) {
        if (!miembros.remove(personaje)) {
            return;
        }
        if (personaje.getGremio() == this) {
            personaje.abandonarGremio();
        }
    }

    public String getNombre() {
        return nombre;
    }

    public List<Personaje> getMiembros() {
        return Collections.unmodifiableList(miembros);
    }

    public Optional<Personaje> buscarPorNombre(String nombre) {
        return miembros.stream()
                .filter(miembro -> miembro.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public List<Personaje> filtrarPorTipo(TipoPersonaje tipo) {
        return miembros.stream()
                .filter(miembro -> miembro.getTipo() == tipo)
                .toList();
    }

    public List<Personaje> obtenerVivos() {
        return miembros.stream()
                .filter(Personaje::estaVivo)
                .toList();
    }

    public List<String> obtenerNombres() {
        return miembros.stream()
                .map(Personaje::getNombre)
                .toList();
    }

    public Map<TipoPersonaje, List<Personaje>> agruparPorTipo() {
        return miembros.stream()
                .collect(Collectors.groupingBy(Personaje::getTipo));
    }

    public List<String> obtenerTodasLasHabilidades() {
        return miembros.stream()
                .flatMap(miembro -> miembro.getHabilidades().stream())
                .distinct()
                .sorted()
                .toList();
    }

    public void ejecutarParaCadaMiembro(Consumer<Personaje> accion) {
        miembros.forEach(accion);
    }
}
