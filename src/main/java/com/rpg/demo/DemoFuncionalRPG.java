package com.rpg.demo;

import com.rpg.core.Bestiario;
import com.rpg.core.TurnoCallback;
import com.rpg.entities.Enemigo;
import com.rpg.entities.Escuadron;
import com.rpg.entities.EstadoEfecto;
import com.rpg.entities.Gremio;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.Personaje;
import com.rpg.entities.enums.TipoEfecto;
import com.rpg.entities.enums.TipoPersonaje;
import com.rpg.functional.CondicionVictoria;
import com.rpg.functional.HabilidadFuncional;
import com.rpg.functional.ModificadorDanio;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DemoFuncionalRPG {

    public static void main(String[] args) {
        imprimirEncabezado("DEMOSTRACION INTEGRAL: EXPRESIONES LAMBDA Y STREAM API EN JAVA RPG");

        demostrarModulo1LambdasVsClasesAnonimas();
        demostrarModulo2InterfacesFuncionalesEstandar();
        demostrarModulo3InterfacesFuncionalesPersonalizadas();
        demostrarModulo4FuentesDeStreamYPipeline();
        demostrarModulo5OperacionesIntermediasYTerminales();
        demostrarModulo6FlatMapYAgrupaciones();
        demostrarModulo7ReglaDeOroNoReutilizacion();

        imprimirEncabezado("FIN DE LA DEMOSTRACION: TODOS LOS MODULOS COMPLETADOS CON EXITO");
    }

    private static void demostrarModulo1LambdasVsClasesAnonimas() {
        imprimirSeccion("1. Expresiones Lambda vs Clases Anonimas Tradicionales");

        System.out.println("--- [Imperativo / Tradicional] Implementacion con Clase Anonima ---");
        TurnoCallback callbackAnonimo = new TurnoCallback() {
            @Override
            public void onTiempoAgotado() {
                System.out.println("[Clase Anonima] El tiempo de turno se ha agotado!");
            }
        };
        callbackAnonimo.onTiempoAgotado();

        System.out.println("\n--- [Declarativo / Moderno] Expresion Lambda equivalente ---");
        TurnoCallback callbackLambda = () -> System.out.println("[Lambda concisa] El tiempo de turno se ha agotado!");
        callbackLambda.onTiempoAgotado();

        System.out.println("\n--- Sintaxis con parametros y bloque de sentencias ---");
        ModificadorDanio criticoPersonalizado = (danioBase, atacante, defensor) -> {
            int bonus = atacante.calcularDanio() > 15 ? 10 : 3;
            System.out.printf("   [Calculo] Danio base %d + Bonus %d por postura de %s%n",
                    danioBase, bonus, atacante.getNombre());
            return danioBase + bonus;
        };

        Personaje guerrero = new Guerrero("Ragnar");
        Personaje objetivo = new Enemigo("Goblin", 50, 10, 2);
        int danioFinal = criticoPersonalizado.aplicar(25, guerrero, objetivo);
        System.out.println("   Resultado del modificador lambda: " + danioFinal);
    }

    private static void demostrarModulo2InterfacesFuncionalesEstandar() {
        imprimirSeccion("2. Interfaces Funcionales Estandar (java.util.function)");

        Personaje heroe = new Guerrero("Arthur");
        heroe.recibirDanio(40);

        System.out.println("A) Consumer<Personaje>: aplicar accion o efecto secundario");
        Consumer<Personaje> curarVeintePuntos = p -> {
            int curado = p.curar(20);
            System.out.printf("   [Consumer] Curando a %s: +%d HP (Vida actual: %d/%d)%n",
                    p.getNombre(), curado, p.getVida(), p.getVidaMaxima());
        };
        curarVeintePuntos.accept(heroe);

        System.out.println("\nB) Supplier<Personaje>: fabrica funcional de enemigos");
        Supplier<Enemigo> fabricaJefe = () -> new Enemigo("Dragon Legendario", 300, 35, 15,
                new EstadoEfecto("Llama Sagrada", TipoEfecto.DANIO_CONTINUO, 10, 4), true);
        Enemigo jefe = fabricaJefe.get();
        System.out.printf("   [Supplier] Invocado nuevo jefe: %s con %d HP y %d ATK%n",
                jefe.getNombre(), jefe.getVidaMaxima(), jefe.getAtaque());

        System.out.println("\nC) Function<Personaje, String>: transformar entidad a ficha formateada");
        Function<Personaje, String> formateadorFicha = p ->
                String.format("   [Function] FICHA: %-12s | ROL: %-8s | HP: %3d/%3d | DEF: %2d",
                        p.getNombre(), p.getTipo(), p.getVida(), p.getVidaMaxima(), p.getDefensa());
        System.out.println(formateadorFicha.apply(heroe));
        System.out.println(formateadorFicha.apply(jefe));

        System.out.println("\nD) Predicate<Personaje>: evaluar condicion de combate");
        Predicate<Personaje> estaHeridoCritico = p -> (p.getVida() * 100 / p.getVidaMaxima()) < 50;
        System.out.printf("   [Predicate] %s esta en estado critico (<50%% HP)? %b%n",
                heroe.getNombre(), estaHeridoCritico.test(heroe));
        System.out.printf("   [Predicate] %s esta en estado critico (<50%% HP)? %b%n",
                jefe.getNombre(), estaHeridoCritico.test(jefe));
    }

    private static void demostrarModulo3InterfacesFuncionalesPersonalizadas() {
        imprimirSeccion("3. Interfaces Funcionales Personalizadas (@FunctionalInterface)");

        Personaje mago = new Mago("Gandalf");
        Personaje orco = new Enemigo("Orco Capataz", 80, 16, 5);

        ModificadorDanio buffBendicion = (danio, atacante, defensor) -> {
            System.out.println("   [Modificador 1 - Bendicion] +8 de danio magico");
            return danio + 8;
        };

        ModificadorDanio debuffPerforante = (danio, atacante, defensor) -> {
            if (defensor.getDefensa() > 4) {
                System.out.println("   [Modificador 2 - Perforante] Multiplicador x1.25 por armadura pesada");
                return (int) Math.round(danio * 1.25);
            }
            return danio;
        };

        ModificadorDanio pipelineTotal = buffBendicion.encadenar(debuffPerforante);

        System.out.println("--- Aplicando cadena funcional de danio ---");
        int danioBase = 20;
        int danioCalculado = pipelineTotal.aplicar(danioBase, mago, orco);
        System.out.printf("Danio base: %d -> Danio compuesto final: %d%n%n", danioBase, danioCalculado);

        HabilidadFuncional rayoArcano = (lanzador, receptor) -> {
            int impacto = 30;
            receptor.defender(impacto);
            return String.format("%s fulmina con Rayo Arcano a %s (-%d HP)",
                    lanzador.getNombre(), receptor.getNombre(), impacto);
        };
        System.out.println("   [HabilidadFuncional] " + rayoArcano.ejecutar(mago, orco));

        CondicionVictoria aniquilacionRival = (aliados, rivales) -> rivales.ningunoVivo();
        Escuadron escAliados = new Escuadron("Luz");
        escAliados.agregar(mago);
        Escuadron escRivales = new Escuadron("Sombras");
        escRivales.agregar(orco);
        System.out.println("   [CondicionVictoria] El escuadron rival fue totalmente eliminado? " +
                aniquilacionRival.evaluar(escAliados, escRivales));
    }

    private static void demostrarModulo4FuentesDeStreamYPipeline() {
        imprimirSeccion("4. Fuentes de Creacion de Streams y Estructura del Pipeline");

        List<String> armas = List.of("Espada Larga", "Baston Mistico", "Arco Elfico", "Hacha de Guerra");
        System.out.println("A) Stream desde Coleccion:");
        armas.stream()
                .filter(nombre -> nombre.length() > 11)
                .map(String::toUpperCase)
                .forEach(nombre -> System.out.println("   - Arma destacada: " + nombre));

        int[] puntuaciones = {120, 450, 310, 890, 50};
        System.out.println("\nB) Stream desde Arrays.stream():");
        int puntuacionMaxima = Arrays.stream(puntuaciones).max().orElse(0);
        System.out.println("   Puntuacion record del torneo: " + puntuacionMaxima);

        System.out.println("\nC) Stream directo con Stream.of(...):");
        long pocionesEspeciales = Stream.of("Pocion Mayor", "Elixir de Mana", "Antidoto Milagroso", "Pocion Menor")
                .filter(p -> p.startsWith("Pocion"))
                .count();
        System.out.println("   Cantidad de pociones curativas: " + pocionesEspeciales);

        System.out.println("\nD) Stream construido dinamicamente con Stream.<T>builder():");
        Stream.<Personaje>builder()
                .add(new Guerrero("Hercules"))
                .add(new Mago("Merlin"))
                .add(new Guerrero("Leonidas"))
                .build()
                .map(p -> String.format("   * %s (%s, HP %d)", p.getNombre(), p.getTipo(), p.getVidaMaxima()))
                .forEach(System.out::println);
    }

    private static void demostrarModulo5OperacionesIntermediasYTerminales() {
        imprimirSeccion("5. Operaciones Intermedias y Terminales en Profundidad");

        Escuadron escuadron = new Escuadron("Campeones de la Alianza");
        Personaje g1 = new Guerrero("Garen");
        Personaje g2 = new Guerrero("Darius");
        Personaje m1 = new Mago("Ryze");
        Personaje m2 = new Mago("Lux");

        g1.recibirDanio(40);
        g2.recibirDanio(120);
        m1.recibirDanio(30);

        escuadron.agregar(g1);
        escuadron.agregar(g2);
        escuadron.agregar(m1);
        escuadron.agregar(m2);

        System.out.println("--- 1. filter() y toList() (Terminal en Java 16+) ---");
        List<Personaje> supervivientes = escuadron.getMiembros().stream()
                .filter(Personaje::estaVivo)
                .toList();
        System.out.println("Supervivientes vivos: " +
                supervivientes.stream().map(Personaje::getNombre).toList());

        System.out.println("\n--- 2. map() y Method References ---");
        List<String> nombresMayusculas = escuadron.getMiembros().stream()
                .map(Personaje::getNombre)
                .map(String::toUpperCase)
                .toList();
        System.out.println("Nombres transformados a mayusculas: " + nombresMayusculas);

        System.out.println("\n--- 3. sorted(), distinct(), limit() y skip() ---");
        List<Integer> vidasOrdenadasDesc = escuadron.getMiembros().stream()
                .map(Personaje::getVida)
                .sorted(Comparator.reverseOrder())
                .distinct()
                .limit(3)
                .toList();
        System.out.println("Top 3 valores de vida unicos ordenados desc: " + vidasOrdenadasDesc);

        System.out.println("\n--- 4. anyMatch(), allMatch(), noneMatch() (Short-circuiting) ---");
        boolean hayMuertos = escuadron.getMiembros().stream().anyMatch(p -> !p.estaVivo());
        boolean todosVivos = escuadron.getMiembros().stream().allMatch(Personaje::estaVivo);
        boolean ningunoTieneMana = escuadron.getMiembros().stream()
                .filter(p -> p instanceof Mago)
                .map(p -> (Mago) p)
                .noneMatch(m -> m.getMana() > 0);

        System.out.printf("   anyMatch(!estaVivo): %b%n", hayMuertos);
        System.out.printf("   allMatch(estaVivo): %b%n", todosVivos);
        System.out.printf("   noneMatch(mana > 0 en magos): %b%n", ningunoTieneMana);

        System.out.println("\n--- 5. findFirst() y findAny() con Optional<T> ---");
        Optional<Personaje> primerMago = escuadron.getMiembros().stream()
                .filter(p -> p.getTipo() == TipoPersonaje.MAGO)
                .findFirst();
        primerMago.ifPresent(m -> System.out.println("   Primer mago encontrado: " + m.getNombre()));

        System.out.println("\n--- 6. Reduccion: mapToInt().sum() y reduce() ---");
        int sumaVida = escuadron.getMiembros().stream()
                .filter(Personaje::estaVivo)
                .mapToInt(Personaje::getVida)
                .sum();
        System.out.println("   Suma total de vida de los vivos: " + sumaVida);

        int maximoAtaque = escuadron.getMiembros().stream()
                .map(Personaje::calcularDanio)
                .reduce(0, Integer::max);
        System.out.println("   Ataque maximo del escuadron (reduce): " + maximoAtaque);
    }

    private static void demostrarModulo6FlatMapYAgrupaciones() {
        imprimirSeccion("6. flatMap y Colectores Avanzados (groupingBy, joining)");

        Gremio gremio = new Gremio("Guardianes de la Nieve");
        Guerrero g1 = new Guerrero("Thor");
        Mago m1 = new Mago("Freya");
        Guerrero g2 = new Guerrero("Baldur");

        g1.aprenderHabilidad("Torbellino de Acero");
        m1.aprenderHabilidad("Ventisca Helada");
        m1.aprenderHabilidad("Escudo de Escarcha");
        g2.aprenderHabilidad("Torbellino de Acero");

        gremio.agregarMiembro(g1);
        gremio.agregarMiembro(m1);
        gremio.agregarMiembro(g2);

        System.out.println("--- 1. Demostracion de flatMap(): aplanar Set<String> de habilidades ---");
        List<String> habilidadesUnicas = gremio.getMiembros().stream()
                .flatMap(personaje -> personaje.getHabilidades().stream())
                .distinct()
                .sorted()
                .toList();
        System.out.println("Habilidades unicas de todo el gremio (aplanadas con flatMap):");
        habilidadesUnicas.forEach(h -> System.out.println("   * " + h));

        System.out.println("\n--- 2. Collectors.joining(): generar cadenas concatenadas ---");
        String resumenNombres = gremio.getMiembros().stream()
                .map(Personaje::getNombre)
                .collect(Collectors.joining(" -> ", "[Inicio Gremio: ", " - Fin]"));
        System.out.println(resumenNombres);

        System.out.println("\n--- 3. Collectors.groupingBy(): agrupar por clase ---");
        Map<TipoPersonaje, List<Personaje>> agrupados = gremio.agruparPorTipo();
        agrupados.forEach((tipo, lista) -> {
            String miembrosStr = lista.stream().map(Personaje::getNombre).collect(Collectors.joining(", "));
            System.out.printf("   Categoria [%s]: %s%n", tipo, miembrosStr);
        });

        System.out.println("\n--- 4. Bestiario con Stream queries ---");
        Bestiario bestiario = new Bestiario();
        List<String> enemigosPeligrosos = bestiario.filtrar(enemigo -> enemigo.getAtaque() >= 12).stream()
                .map(Enemigo::getNombre)
                .toList();
        System.out.println("Monstruos de alta peligrosidad (ATK >= 12): " + enemigosPeligrosos);
        System.out.printf("Vida promedio del bestiario: %.1f HP%n", bestiario.calcularVidaPromedio());
    }

    private static void demostrarModulo7ReglaDeOroNoReutilizacion() {
        imprimirSeccion("7. Regla de Oro: Un Stream se Consume una Sola Vez");

        Stream<String> streamPociones = Stream.of("Pocion de Vida", "Elixir de Mana", "Antidoto");

        System.out.println("Primer consumo del stream (Terminal: toList()):");
        List<String> resultado = streamPociones.toList();
        System.out.println("   Lista obtenida: " + resultado);

        System.out.println("\nIntentando reutilizar el mismo Stream (debe lanzar IllegalStateException):");
        try {
            streamPociones.count();
            System.out.println("ERROR: El stream se reutilizo inesperadamente!");
        } catch (IllegalStateException e) {
            System.out.printf("   [CAPTURADA EXCEPCION ESPERADA] %s: %s%n",
                    e.getClass().getSimpleName(), e.getMessage());
            System.out.println("   -> Correcto: Un Stream en Java es una cinta transportadora que solo fluye una vez.");
        }

        System.out.println("\n--- Buenas practicas de legibilidad destacadas en la presentacion ---");
        System.out.println("1. Usar nombres descriptivos de parametros en Lambdas:");
        System.out.println("   Recomendado: personaje -> personaje.estaVivo()");
        System.out.println("   Evitar:      x -> x.estaVivo()");
        System.out.println("2. Preferir referencias a metodos (Method References) cuando aumente la claridad:");
        System.out.println("   Recomendado: Personaje::estaVivo, String::toUpperCase");
    }

    private static void imprimirEncabezado(String titulo) {
        System.out.println("\n" + "=".repeat(75));
        System.out.println("  " + titulo);
        System.out.println("=".repeat(75));
    }

    private static void imprimirSeccion(String subtitulo) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println(">>> " + subtitulo);
        System.out.println("-".repeat(60));
    }
}
