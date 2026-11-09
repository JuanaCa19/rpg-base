package com.rpg.items;

import com.rpg.entities.enums.RaridadItem;
import com.rpg.exceptions.CapacidadExcedidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Inventario con Streams y Lambdas")
class InventarioStreamTest {

    private Inventario<Pocion> inventario;
    private Pocion vidaComun;
    private Pocion manaRaro;
    private Pocion antidoto;

    @BeforeEach
    void setUp() throws CapacidadExcedidaException {
        inventario = new Inventario<>(5);
        vidaComun = new Pocion("Pocion de Vida", 30, RaridadItem.COMUN);
        manaRaro = new Pocion("Elixir Arcano", 50, RaridadItem.RARO);
        antidoto = new Pocion("Antidoto", 0, RaridadItem.COMUN, null, true);

        inventario.agregar(vidaComun);
        inventario.agregar(manaRaro);
        inventario.agregar(antidoto);
    }

    @Test
    @DisplayName("filtrar con Predicate<T> retorna los elementos coincidentes")
    void testFiltrar() {
        List<Pocion> curativas = inventario.filtrar(p -> p.getCuracionReal() > 0);
        assertEquals(2, curativas.size());
        assertTrue(curativas.contains(vidaComun));
        assertTrue(curativas.contains(manaRaro));
    }

    @Test
    @DisplayName("buscarPrimero con Predicate<T> retorna Optional con la primera coincidencia")
    void testBuscarPrimero() {
        Optional<Pocion> encontrada = inventario.buscarPrimero(Pocion::isPurifica);
        assertTrue(encontrada.isPresent());
        assertEquals("Antidoto", encontrada.get().getNombre());
    }

    @Test
    @DisplayName("existe con anyMatch valida presencia sin iterar innecesariamente")
    void testExiste() {
        assertTrue(inventario.existe(p -> p.getNombre().contains("Vida")));
        assertFalse(inventario.existe(p -> p.getNombre().contains("Veneno")));
    }

    @Test
    @DisplayName("paraCada ejecuta Consumer<T> sobre todos los elementos")
    void testParaCada() {
        AtomicInteger totalCuracion = new AtomicInteger();
        inventario.paraCada(p -> totalCuracion.addAndGet(p.getCuracionReal()));

        int esperada = vidaComun.getCuracionReal() + manaRaro.getCuracionReal() + antidoto.getCuracionReal();
        assertEquals(esperada, totalCuracion.get());
    }

    @Test
    @DisplayName("transformar con Function<T, R> mapea a nueva lista")
    void testTransformar() {
        List<String> nombres = inventario.transformar(Pocion::getNombre);
        assertEquals(List.of("Pocion de Vida", "Elixir Arcano", "Antidoto"), nombres);
    }

    @Test
    @DisplayName("contar evalua la cantidad de coincidencias")
    void testContar() {
        long sinCuracionDirecta = inventario.contar(p -> p.getCuracionReal() == 0);
        assertEquals(1, sinCuracionDirecta);
    }
}
