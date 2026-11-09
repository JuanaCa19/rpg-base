package com.rpg.core;

import com.rpg.entities.Enemigo;
import com.rpg.entities.EstadoEfecto;
import com.rpg.entities.enums.TipoEfecto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Catalogo de monstruos (Map nombre -> plantilla) y contador de derrotas por monstruo. */
public class Bestiario {

    private final Map<String, Enemigo> catalogo = new LinkedHashMap<>();
    private final Map<String, Integer> derrotados = new LinkedHashMap<>();

    public Bestiario() {
        registrar(new Enemigo("Goblin", 50, 8, 2));
        registrar(new Enemigo("Arana Gigante", 60, 10, 3,
                new EstadoEfecto("Veneno", TipoEfecto.DANIO_CONTINUO, 4, 3), false));
        registrar(new Enemigo("Ogro", 100, 14, 6, null, true));
        registrar(new Enemigo("Dragon Joven", 160, 20, 10,
                new EstadoEfecto("Quemadura", TipoEfecto.DANIO_CONTINUO, 6, 3), false));
    }

    private void registrar(Enemigo enemigo) {
        catalogo.put(enemigo.getNombre(), enemigo);
    }

    public List<String> nombres() {
        return new ArrayList<>(catalogo.keySet());
    }

    /** Busca ignorando mayusculas (equalsIgnoreCase, nunca ==). @return copia nueva o null si no existe */
    public Enemigo crearEnemigo(String nombre) {
        return catalogo.entrySet().stream()
                .filter(entrada -> entrada.getKey().equalsIgnoreCase(nombre))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(Enemigo::copia)
                .orElse(null);
    }

    public void registrarEnemigoDerrotado(String nombre) {
        derrotados.put(nombre, derrotados.getOrDefault(nombre, 0) + 1);
    }

    public Map<String, Integer> getDerrotados() {
        return new LinkedHashMap<>(derrotados);
    }

    public void cargarDerrotados(Map<String, Integer> guardados) {
        derrotados.clear();
        derrotados.putAll(guardados);
    }

    public int vecesDerrotado(String nombre) {
        return derrotados.getOrDefault(nombre, 0);
    }

    public String describir() {
        StringBuilder texto = new StringBuilder();
        int numero = 1;
        for (Map.Entry<String, Enemigo> entrada : catalogo.entrySet()) {
            Enemigo enemigo = entrada.getValue();
            texto.append(String.format("%d. %-14s HP %3d  ATK %2d  DEF %2d  Habilidad: %-9s Derrotado: %d%n",
                    numero++, entrada.getKey(), enemigo.getVidaMaxima(), enemigo.getAtaque(),
                    enemigo.getDefensa(), enemigo.getHabilidad(), vecesDerrotado(entrada.getKey())));
        }
        return texto.toString();
    }

    public String resumenDerrotados() {
        if (derrotados.isEmpty()) {
            return "ninguno";
        }
        return derrotados.toString();
    }

    public List<Enemigo> filtrar(Predicate<Enemigo> criterio) {
        return catalogo.values().stream()
                .filter(criterio)
                .map(Enemigo::copia)
                .toList();
    }

    public List<String> obtenerNombresOrdenados() {
        return catalogo.keySet().stream()
                .sorted()
                .toList();
    }

    public List<Enemigo> obtenerEnemigosConHabilidad() {
        return catalogo.values().stream()
                .filter(enemigo -> !enemigo.getHabilidad().equalsIgnoreCase("Ninguna"))
                .map(Enemigo::copia)
                .toList();
    }

    public double calcularVidaPromedio() {
        return catalogo.values().stream()
                .mapToInt(Enemigo::getVidaMaxima)
                .average()
                .orElse(0.0);
    }

    public Optional<Enemigo> obtenerMasPeligroso() {
        return catalogo.values().stream()
                .max(Comparator.comparingInt(Enemigo::getAtaque))
                .map(Enemigo::copia);
    }

    public Map<Boolean, List<Enemigo>> particionarPorPeligro(int umbralAtaque) {
        return catalogo.values().stream()
                .collect(Collectors.partitioningBy(
                        enemigo -> enemigo.getAtaque() >= umbralAtaque,
                        Collectors.mapping(Enemigo::copia, Collectors.toList())
                ));
    }

    public void registrarConFabrica(Supplier<Enemigo> fabrica) {
        if (fabrica != null) {
            Enemigo nuevo = fabrica.get();
            if (nuevo != null) {
                registrar(nuevo);
            }
        }
    }
}
