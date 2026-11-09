package com.rpg.functional;

import com.rpg.entities.Enemigo;
import com.rpg.entities.Escuadron;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.Personaje;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias de Interfaces Funcionales (Sesion 15)")
class FunctionalInterfacesTest {

    @Test
    @DisplayName("Consumer<T> aplica accion secundaria sobre Personaje")
    void testConsumer() {
        Personaje guerrero = new Guerrero("Thor");
        guerrero.recibirDanio(50);
        int vidaAntes = guerrero.getVida();

        Consumer<Personaje> sanador = p -> p.curar(25);
        sanador.accept(guerrero);

        assertEquals(vidaAntes + 25, guerrero.getVida());
    }

    @Test
    @DisplayName("Supplier<T> genera una instancia de Personaje bajo demanda")
    void testSupplier() {
        Supplier<Personaje> fabricaMago = () -> new Mago("Merlin");
        Personaje mago = fabricaMago.get();

        assertNotNull(mago);
        assertEquals("Merlin", mago.getNombre());
        assertTrue(mago instanceof Mago);
    }

    @Test
    @DisplayName("Function<T, R> transforma una entidad Personaje en su descripcion")
    void testFunction() {
        Personaje guerrero = new Guerrero("Conan");
        Function<Personaje, String> generadorIdentificador = p -> p.getNombre() + "#" + p.getTipo();

        String resultado = generadorIdentificador.apply(guerrero);
        assertEquals("Conan#GUERRERO", resultado);
    }

    @Test
    @DisplayName("Predicate<T> evalua condiciones logicas sobre Personaje")
    void testPredicate() {
        Personaje vivo = new Guerrero("Vivo");
        Personaje muerto = new Guerrero("Muerto");
        muerto.recibirDanio(muerto.getVidaMaxima());

        Predicate<Personaje> estaVivo = Personaje::estaVivo;

        assertTrue(estaVivo.test(vivo));
        assertFalse(estaVivo.test(muerto));
    }

    @Test
    @DisplayName("ModificadorDanio con @FunctionalInterface y encadenamiento default")
    void testModificadorDanioComposicion() {
        ModificadorDanio sumarCinco = (danio, atacante, defensor) -> danio + 5;
        ModificadorDanio duplicar = (danio, atacante, defensor) -> danio * 2;

        ModificadorDanio pipeline = sumarCinco.encadenar(duplicar);

        Personaje atacante = new Guerrero("Atacante");
        Personaje defensor = new Enemigo("Defensor", 50, 10, 2);

        int resultado = pipeline.aplicar(10, atacante, defensor);
        assertEquals(30, resultado);
    }

    @Test
    @DisplayName("CondicionVictoria evalua el estado de dos escuadrones")
    void testCondicionVictoria() {
        Escuadron escuadronA = new Escuadron("Alfa");
        escuadronA.agregar(new Guerrero("A1"));

        Escuadron escuadronB = new Escuadron("Beta");
        Personaje b1 = new Guerrero("B1");
        b1.recibirDanio(b1.getVidaMaxima());
        escuadronB.agregar(b1);

        CondicionVictoria rivalesEliminados = (aliados, rivales) -> rivales.ningunoVivo();

        assertTrue(rivalesEliminados.evaluar(escuadronA, escuadronB));
    }
}
