package com.rpg.functional;

import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.Personaje;
import com.rpg.entities.enums.TipoPersonaje;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Java Stream API (Sesion 16)")
class StreamApiTest {

    @Test
    @DisplayName("filter() y toList() extraen solo los personajes vivos")
    void testFilterYToList() {
        Personaje p1 = new Guerrero("G1");
        Personaje p2 = new Guerrero("G2");
        p2.recibirDanio(p2.getVidaMaxima());
        Personaje p3 = new Mago("M1");

        List<Personaje> lista = List.of(p1, p2, p3);

        List<Personaje> vivos = lista.stream()
                .filter(Personaje::estaVivo)
                .toList();

        assertEquals(2, vivos.size());
        assertTrue(vivos.contains(p1));
        assertTrue(vivos.contains(p3));
        assertFalse(vivos.contains(p2));
    }

    @Test
    @DisplayName("map() transforma Personajes a sus nombres")
    void testMap() {
        List<Personaje> lista = List.of(new Guerrero("Ares"), new Mago("Atenea"));

        List<String> nombres = lista.stream()
                .map(Personaje::getNombre)
                .toList();

        assertEquals(List.of("Ares", "Atenea"), nombres);
    }

    @Test
    @DisplayName("flatMap() aplana listas anidadas de habilidades en un solo Stream")
    void testFlatMap() {
        Guerrero g = new Guerrero("G1");
        Mago m = new Mago("M1");
        g.aprenderHabilidad("Corte");
        m.aprenderHabilidad("Corte");
        m.aprenderHabilidad("Centella");

        List<Personaje> equipo = List.of(g, m);

        List<String> habilidadesUnicas = equipo.stream()
                .flatMap(p -> p.getHabilidades().stream())
                .distinct()
                .sorted()
                .toList();

        assertTrue(habilidadesUnicas.contains("Corte"));
        assertTrue(habilidadesUnicas.contains("Centella"));
        assertTrue(habilidadesUnicas.contains("Golpe Brutal"));
        assertTrue(habilidadesUnicas.contains("Bola de Fuego"));
    }

    @Test
    @DisplayName("anyMatch(), allMatch() y noneMatch() cumplen logica booleana")
    void testMatches() {
        Personaje p1 = new Guerrero("G1");
        Personaje p2 = new Guerrero("G2");
        p2.recibirDanio(p2.getVidaMaxima());

        List<Personaje> lista = List.of(p1, p2);

        assertTrue(lista.stream().anyMatch(Personaje::estaVivo));
        assertFalse(lista.stream().allMatch(Personaje::estaVivo));
        assertFalse(lista.stream().noneMatch(Personaje::estaVivo));
    }

    @Test
    @DisplayName("findFirst() retorna Optional con el primer elemento encontrado")
    void testFindFirst() {
        List<Personaje> lista = List.of(new Guerrero("G1"), new Mago("M1"), new Mago("M2"));

        Optional<Personaje> primerMago = lista.stream()
                .filter(p -> p.getTipo() == TipoPersonaje.MAGO)
                .findFirst();

        assertTrue(primerMago.isPresent());
        assertEquals("M1", primerMago.get().getNombre());
    }

    @Test
    @DisplayName("mapToInt().sum() y reduce() calculan acumulaciones numericas")
    void testReducciones() {
        List<Personaje> lista = List.of(
                new Guerrero("G1"),
                new Mago("M1")
        );

        int vidaTotal = lista.stream()
                .mapToInt(Personaje::getVida)
                .sum();

        assertEquals(200, vidaTotal);

        int maxVida = lista.stream()
                .map(Personaje::getVida)
                .reduce(0, Integer::max);

        assertEquals(120, maxVida);
    }

    @Test
    @DisplayName("Collectors.groupingBy() agrupa entidades segun propiedad")
    void testGroupingBy() {
        List<Personaje> lista = List.of(
                new Guerrero("G1"),
                new Guerrero("G2"),
                new Mago("M1")
        );

        Map<TipoPersonaje, List<Personaje>> agrupados = lista.stream()
                .collect(Collectors.groupingBy(Personaje::getTipo));

        assertEquals(2, agrupados.get(TipoPersonaje.GUERRERO).size());
        assertEquals(1, agrupados.get(TipoPersonaje.MAGO).size());
    }

    @Test
    @DisplayName("Regla de oro: Reutilizar un Stream lanza IllegalStateException")
    void testStreamNoReutilizable() {
        Stream<String> stream = Stream.of("Alfa", "Beta");
        stream.toList();

        assertThrows(IllegalStateException.class, () -> stream.count());
    }
}
