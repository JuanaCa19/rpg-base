package com.rpg.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Escuadron con Stream API")
class EscuadronStreamTest {

    private Escuadron escuadron;
    private Personaje guerrero;
    private Personaje mago;

    @BeforeEach
    void setUp() {
        escuadron = new Escuadron("Vanguardia");
        guerrero = new Guerrero("Arthur");
        mago = new Mago("Merlin");
        escuadron.agregar(guerrero);
        escuadron.agregar(mago);
    }

    @Test
    @DisplayName("contarVivos y obtenerVivos reflejan sobrevivientes")
    void testVivos() {
        assertEquals(2, escuadron.contarVivos());
        assertEquals(2, escuadron.obtenerVivos().size());

        guerrero.recibirDanio(guerrero.getVidaMaxima());

        assertEquals(1, escuadron.contarVivos());
        List<Personaje> vivos = escuadron.obtenerVivos();
        assertEquals(1, vivos.size());
        assertEquals("Merlin", vivos.get(0).getNombre());

        List<Personaje> derrotados = escuadron.obtenerDerrotados();
        assertEquals(1, derrotados.size());
        assertEquals("Arthur", derrotados.get(0).getNombre());
    }

    @Test
    @DisplayName("haySobrevivientes, todosVivos y ningunoVivo")
    void testPredicadosBooleanos() {
        assertTrue(escuadron.haySobrevivientes());
        assertTrue(escuadron.todosVivos());
        assertFalse(escuadron.ningunoVivo());

        guerrero.recibirDanio(guerrero.getVidaMaxima());
        assertTrue(escuadron.haySobrevivientes());
        assertFalse(escuadron.todosVivos());
        assertFalse(escuadron.ningunoVivo());

        mago.recibirDanio(mago.getVidaMaxima());
        assertFalse(escuadron.haySobrevivientes());
        assertFalse(escuadron.todosVivos());
        assertTrue(escuadron.ningunoVivo());
    }

    @Test
    @DisplayName("obtenerNombres extrae lista ordenada de identificadores")
    void testObtenerNombres() {
        List<String> nombres = escuadron.obtenerNombres();
        assertEquals(List.of("Arthur", "Merlin"), nombres);
    }

    @Test
    @DisplayName("calcularVidaTotal y calcularVidaPromedio")
    void testMetricasVida() {
        int esperada = guerrero.getVida() + mago.getVida();
        assertEquals(esperada, escuadron.calcularVidaTotal());
        assertEquals(100.0, escuadron.calcularVidaPromedio());
    }

    @Test
    @DisplayName("obtenerMiembroConMasVida encuentra el maximo")
    void testMaxVida() {
        Optional<Personaje> max = escuadron.obtenerMiembroConMasVida();
        assertTrue(max.isPresent());
        assertEquals("Arthur", max.get().getNombre());
    }

    @Test
    @DisplayName("obtenerTodasLasHabilidades aplica flatMap sobre el equipo")
    void testHabilidadesFlatMap() {
        List<String> habilidades = escuadron.obtenerTodasLasHabilidades();
        assertTrue(habilidades.contains("Golpe Brutal"));
        assertTrue(habilidades.contains("Bola de Fuego"));
    }
}
