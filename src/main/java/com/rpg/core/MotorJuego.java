package com.rpg.core;

import com.rpg.entities.Enemigo;
import com.rpg.entities.EstadoEfecto;
import com.rpg.entities.Gremio;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.PerfilJugador;
import com.rpg.entities.Personaje;
import com.rpg.entities.enums.RaridadItem;
import com.rpg.entities.enums.TipoEfecto;
import com.rpg.concurrency.AutoGuardadoDaemon;
import com.rpg.concurrency.GestorTurno;
import com.rpg.exceptions.CapacidadExcedidaException;
import com.rpg.exceptions.PersistenciaException;
import com.rpg.items.Inventario;
import com.rpg.items.Pocion;
import com.rpg.persistencia.Archivos;
import com.rpg.persistencia.GestorPartida;
import com.rpg.persistencia.LogCombate;
import com.rpg.persistencia.PartidaGuardada;
import com.rpg.dao.JugadorDAO;
import com.rpg.dao.MonstruoDAO;
import com.rpg.dao.VictoriaDAO;
import com.rpg.model.Jugador;
import com.rpg.model.Monstruo;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/** Coordina el flujo del juego: crear heroe, combatir, torneo, bestiario y guardado. */
public class MotorJuego {

    private static final String NOMBRE_GREMIO = "Guardianes del Alba";
    private static final int CURACION_POCION_VIDA = 30;
    private static final int DURACION_TURNO_SEGUNDOS = Integer.getInteger("rpg.turno.segundos", 15);
    private static final long INTERVALO_AUTOGUARDADO_MS = Long.getLong("rpg.autoguardado.ms", 30_000L);

    private final Consola consola;
    private final RegistroCombate registro = new RegistroCombate();
    private final Arena arena = new Arena(registro, new Random());
    private final Bestiario bestiario = new Bestiario();
    private final TorneoManager torneo = new TorneoManager(arena, registro);
    private final LogCombate logCombate = new LogCombate(new File(GestorPartida.DIRECTORIO, "combates.txt"));
    private final GestorPartida gestorPartida = new GestorPartida();
    private final Object lockEstado = new Object();
    private PerfilJugador perfil = new PerfilJugador();
    private Gremio gremio = new Gremio(NOMBRE_GREMIO);
    private volatile String ultimoGuardado = "";
    private Thread hiloAutoGuardado;

    public MotorJuego(Consola consola) {
        this.consola = consola;
    }

    // ---- opciones del menu principal

    public void combateRapido() {
        registro.limpiar();
        Personaje jugador = crearJugador();
        Enemigo enemigo = elegirEnemigo();
        arena.preparar(jugador);
        arena.preparar(enemigo);
        registro.agregar("%n=== %s vs %s ===", jugador.getNombre(), enemigo.getNombre());

        boolean huyo;
        try {
            huyo = combatir(jugador, enemigo);
        } finally {
            jugador.cancelarEfectos();
            enemigo.cancelarEfectos();
        }

        cerrarCombate(jugador, enemigo, huyo);
        guardarLog();
        guardarProgreso();
    }

    public void jugarTorneo() {
        registro.limpiar();
        Personaje campeon = torneo.jugarTorneo();
        if (campeon == null) {
            registro.agregar("%nEl torneo no se pudo completar.");
        } else {
            registro.agregar("%nCAMPEON DEL TORNEO: %s", campeon.getNombre());
            synchronized (lockEstado) {
                perfil.desbloquearLogro("Campeon del Torneo");
            }
        }
        guardarLog();
        guardarProgreso();
    }

    public void mostrarBestiario() {
        System.out.println("\n=== BESTIARIO ===");
        synchronized (lockEstado) {
            System.out.print(bestiario.describir());
            System.out.printf("Victorias: %d | Derrotas: %d | Logros: %s%n",
                    perfil.getVictorias(), perfil.getDerrotas(), perfil.getLogros());
        }
    }

    public void verHistorial() {
        try {
            List<String> lineas = logCombate.leer();
            System.out.printf("%n=== HISTORIAL DE COMBATES (%d lineas) ===%n", lineas.size());
            lineas.forEach(System.out::println);
        } catch (PersistenciaException e) {
            System.out.println(e.getMessage());
        }
    }

    public void mostrarArchivos() {
        System.out.println("\n=== ARCHIVOS DE LA PARTIDA ===");
        System.out.print(Archivos.describir(logCombate.getArchivo()));
        System.out.print(Archivos.describir(GestorPartida.PARTIDA));
        System.out.print(Archivos.describir(GestorPartida.AUTOGUARDADO));
    }

    public void cargarPartida() {
        int origen = consola.leerEntero("Cargar: 1. Partida guardada  2. Autoguardado  0. Cancelar: ", 0, 2);
        if (origen == 0) {
            return;
        }
        File archivo = origen == 1 ? GestorPartida.PARTIDA : GestorPartida.AUTOGUARDADO;
        try {
            PartidaGuardada partida = gestorPartida.cargar(archivo);
            synchronized (lockEstado) {
                perfil = partida.getPerfil();
                gremio = partida.getGremio();
                bestiario.cargarDerrotados(partida.getDerrotados());
            }
            System.out.printf("Partida cargada (guardada el %s).%n", partida.getFecha());
            System.out.printf("Victorias: %d | Derrotas: %d | Logros: %s%n",
                    perfil.getVictorias(), perfil.getDerrotas(), perfil.getLogros());
            System.out.printf("Gremio %s: %d miembros%n", gremio.getNombre(), gremio.getMiembros().size());
            for (Personaje miembro : gremio.getMiembros()) {
                System.out.printf("  - %s (%s) vida %d/%d, gremio: %s%n", miembro.getNombre(), miembro.getTipo(),
                        miembro.getVida(), miembro.getVidaMaxima(), miembro.getGremio().getNombre());
            }
        } catch (PersistenciaException e) {
            System.out.println(e.getMessage());
        }
    }

    public void borrarPartida() {
        int confirmacion = consola.leerEntero("Seguro que quieres borrar la partida guardada? (1. Si, 0. No): ", 0, 1);
        if (confirmacion == 0) {
            System.out.println("No se borro nada.");
            return;
        }
        try {
            boolean borrado = gestorPartida.borrar(GestorPartida.PARTIDA);
            System.out.println(borrado ? "Partida borrada." : "No habia ninguna partida guardada.");
        } catch (PersistenciaException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean hayPartidaGuardada() {
        return GestorPartida.PARTIDA.exists();
    }

    public void iniciarAutoGuardado() {
        if (hiloAutoGuardado != null && hiloAutoGuardado.isAlive()) {
            return;
        }
        hiloAutoGuardado = new AutoGuardadoDaemon(INTERVALO_AUTOGUARDADO_MS, this::autoGuardar).iniciar();
    }

    // ---- creacion de personajes

    private Personaje crearJugador() {
        int clase = consola.leerEntero("Clase (1. Guerrero, 2. Mago): ", 1, 2);
        String nombre = consola.leerNombre("Nombre del heroe: ");
        Personaje jugador = switch (clase) {
            case 1 -> new Guerrero(nombre);
            default -> new Mago(nombre);
        };
        entregarKitInicial(jugador);
        synchronized (lockEstado) {
            jugador.unirseAGremio(gremio);
            System.out.printf("%s se une al gremio %s (%d miembros).%n",
                    jugador.getNombre(), gremio.getNombre(), gremio.getMiembros().size());
        }
        return jugador;
    }

    private void entregarKitInicial(Personaje jugador) {
        List<Pocion> kit = List.of(
                new Pocion("Pocion de Vida", CURACION_POCION_VIDA, RaridadItem.COMUN),
                new Pocion("Elixir de Regeneracion", 0, RaridadItem.RARO,
                        new EstadoEfecto("Regeneracion", TipoEfecto.CURACION_CONTINUA, 6, 3), false),
                new Pocion("Antidoto", 0, RaridadItem.COMUN, null, true));
        kit.forEach(pocion -> {
            try {
                jugador.getInventario().agregar(pocion);
            } catch (CapacidadExcedidaException e) {
                System.out.println("No se pudo entregar todo el kit: " + e.getMessage());
            }
        });
    }

    private Enemigo elegirEnemigo() {
        List<String> nombres = bestiario.nombres();
        System.out.println("Rivales disponibles:");
        for (int i = 0; i < nombres.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, nombres.get(i));
        }
        while (true) {
            String texto = consola.leerLinea("Rival (numero o nombre): ");
            Enemigo enemigo = resolverRival(texto, nombres);
            if (enemigo != null) {
                return enemigo;
            }
            System.out.println("No existe ese rival.");
        }
    }

    private Enemigo resolverRival(String texto, List<String> nombres) {
        try {
            int numero = Integer.parseInt(texto);
            boolean valido = numero >= 1 && numero <= nombres.size();
            return valido ? bestiario.crearEnemigo(nombres.get(numero - 1)) : null;
        } catch (NumberFormatException e) {
            return bestiario.crearEnemigo(texto);
        }
    }

    // ---- bucle de combate

    /** @return true si el jugador huyo */
    private boolean combatir(Personaje jugador, Enemigo enemigo) {
        int ronda = 1;
        while (jugador.estaVivo() && enemigo.estaVivo()) {
            registro.agregar("%n-- Ronda %d --", ronda);
            if (arena.iniciarTurno(jugador) && turnoJugador(jugador, enemigo)) {
                return true;
            }
            arena.turnoAutomatico(enemigo, jugador);
            ronda++;
        }
        return false;
    }

    /**
     * Turno del jugador. Las acciones que no gastan turno (ver estado, cancelar) vuelven a pedir.
     * @return true si el jugador decidio huir
     */
    private boolean turnoJugador(Personaje jugador, Enemigo enemigo) {
        boolean turnoGastado;
        do {
            registro.agregar(FormatoCombate.resumen(jugador));
            registro.agregar(FormatoCombate.resumen(enemigo));
            Accion accion = pedirAccion();
            if (accion == Accion.HUIR) {
                return true;
            }
            if (!jugador.estaVivo() || !enemigo.estaVivo()) {
                return false;
            }
            if (accion == Accion.TIEMPO_AGOTADO) {
                registro.agregar("%s tardo demasiado y pierde el turno", jugador.getNombre());
                return false;
            }
            turnoGastado = switch (accion) {
                case ATACAR -> {
                    arena.atacar(jugador, enemigo);
                    yield true;
                }
                case ESPECIAL -> arena.intentarEspecial(jugador, enemigo);
                case POCION -> usarPocion(jugador);
                default -> {
                    mostrarEstado(jugador);
                    yield false;
                }
            };
        } while (!turnoGastado && enemigo.estaVivo());
        return false;
    }

    private Accion pedirAccion() {
        System.out.printf("Tienes %d segundos. 1. Atacar  2. Habilidad especial  3. Usar pocion  4. Ver estado  0. Huir%n",
                DURACION_TURNO_SEGUNDOS);
        AtomicBoolean tiempoAgotado = new AtomicBoolean(false);
        GestorTurno gestorTurno = new GestorTurno(DURACION_TURNO_SEGUNDOS, () -> {
            tiempoAgotado.set(true);
            System.out.println("\nSe acabo el tiempo! Pierdes el turno (pulsa Enter para continuar).");
        });
        gestorTurno.iniciar();
        int opcion;
        try {
            opcion = consola.leerEntero("Accion: ", 0, 4, tiempoAgotado::get);
        } finally {
            gestorTurno.cancelar();
        }
        if (tiempoAgotado.get()) {
            return Accion.TIEMPO_AGOTADO;
        }
        return switch (opcion) {
            case 1 -> Accion.ATACAR;
            case 2 -> Accion.ESPECIAL;
            case 3 -> Accion.POCION;
            case 4 -> Accion.ESTADO;
            default -> Accion.HUIR;
        };
    }

    private boolean usarPocion(Personaje jugador) {
        Inventario<Pocion> inventario = jugador.getInventario();
        if (inventario.estaVacio()) {
            System.out.println("No tienes pociones.");
            return false;
        }
        int numero = 1;
        for (Pocion pocion : inventario) {
            System.out.printf("  %d. %s%n", numero++, pocion.getNombre());
        }
        int eleccion = consola.leerEntero("Pocion (0 = cancelar): ", 0, inventario.tamanio());
        if (eleccion == 0) {
            return false;
        }
        synchronized (lockEstado) {
            registro.agregar(jugador.usarPocion(inventario.quitar(eleccion - 1)));
        }
        return true;
    }

    private void mostrarEstado(Personaje jugador) {
        System.out.printf("Habilidades: %s | Arma: %s | Defensa: %d | Pociones: %d%n",
                jugador.getHabilidades(),
                jugador.getArma() != null ? jugador.getArma().getNombre() : "ninguna",
                jugador.getDefensa(), jugador.getInventario().tamanio());
        for (EstadoEfecto efecto : jugador.getEfectos()) {
            System.out.printf("Efecto activo: %s (%d turnos)%n", efecto.getNombre(), efecto.getTurnosRestantes());
        }
    }

    // ---- resultado del combate

    private void cerrarCombate(Personaje jugador, Enemigo enemigo, boolean huyo) {
        synchronized (lockEstado) {
            cerrarCombateSincronizado(jugador, enemigo, huyo);
        }
    }

    private void cerrarCombateSincronizado(Personaje jugador, Enemigo enemigo, boolean huyo) {
        if (huyo) {
            registro.agregar("%s huye del combate.", jugador.getNombre());
        } else if (jugador.estaVivo()) {
            registro.agregar("%nVictoria de %s!", jugador.getNombre());
            perfil.registrarVictoria();
            bestiario.registrarEnemigoDerrotado(enemigo.getNombre());
            desbloquearLogros(jugador, enemigo);
            repartirBotin(jugador);
            registrarVictoriaEnBD(jugador.getNombre(), enemigo.getNombre());
        } else {
            registro.agregar("%n%s ha caido ante %s.", jugador.getNombre(), enemigo.getNombre());
            perfil.registrarDerrota();
        }
    }

    private void desbloquearLogros(Personaje jugador, Enemigo enemigo) {
        boolean intacto = jugador.getVida() == jugador.getVidaMaxima();
        List<String> candidatos = List.of(
                "Primera Victoria",
                "Cazador de " + enemigo.getNombre(),
                intacto ? "Intocable" : "");

        candidatos.stream()
                .filter(logro -> !logro.isBlank())
                .filter(perfil::desbloquearLogro)
                .forEach(logro -> registro.agregar("Logro desbloqueado: %s", logro));
    }

    private void repartirBotin(Personaje jugador) {
        try {
            jugador.getInventario().agregar(new Pocion("Pocion de Vida", CURACION_POCION_VIDA, RaridadItem.COMUN));
            registro.agregar("Botin: Pocion de Vida.");
        } catch (CapacidadExcedidaException e) {
            registro.agregar("Botin perdido: %s", e.getMessage());
        }
    }

    // ---- guardado

    private void guardarLog() {
        try {
            logCombate.guardar(registro.getTexto(), true);
        } catch (PersistenciaException e) {
            System.out.println("No se pudo guardar el historial: " + e.getMessage());
        }
    }

    public void guardarProgreso() {
        synchronized (lockEstado) {
            ultimoGuardado = construirResumen();
            System.out.println("[Progreso guardado en memoria]");
            try {
                gestorPartida.guardar(crearPartida(), GestorPartida.PARTIDA);
                System.out.println("[Partida guardada en " + GestorPartida.PARTIDA.getPath() + "]");
            } catch (PersistenciaException e) {
                System.out.println("No se pudo guardar la partida: " + e.getMessage());
            }
        }
    }

    private void autoGuardar() {
        synchronized (lockEstado) {
            if (perfil.getVictorias() + perfil.getDerrotas() == 0 && perfil.getLogros().isEmpty()) {
                return;
            }
            ultimoGuardado = construirResumen();
            try {
                gestorPartida.guardar(crearPartida(), GestorPartida.AUTOGUARDADO);
                System.out.println("[Autoguardado hecho]");
            } catch (PersistenciaException e) {
                System.out.println("[Autoguardado fallido: " + e.getMessage() + "]");
            }
        }
    }

    private PartidaGuardada crearPartida() {
        return new PartidaGuardada(perfil, gremio, bestiario.getDerrotados());
    }

    private String construirResumen() {
        StringBuilder contenido = new StringBuilder();
        contenido.append("=== PARTIDA RPG ===").append(System.lineSeparator());
        contenido.append("Victorias: ").append(perfil.getVictorias()).append(System.lineSeparator());
        contenido.append("Derrotas: ").append(perfil.getDerrotas()).append(System.lineSeparator());
        contenido.append("Logros: ").append(String.join(", ", perfil.getLogros())).append(System.lineSeparator());
        contenido.append("Gremio ").append(gremio.getNombre()).append(": ")
                .append(gremio.getMiembros().size()).append(" miembros").append(System.lineSeparator());
        for (Personaje miembro : gremio.getMiembros()) {
            contenido.append("  - ").append(miembro.getNombre()).append(" (").append(miembro.getTipo()).append(") ")
                    .append(miembro.getVida()).append("/").append(miembro.getVidaMaxima())
                    .append(" ").append(miembro.getEstado()).append(System.lineSeparator());
        }
        contenido.append("Enemigos derrotados: ").append(bestiario.resumenDerrotados()).append(System.lineSeparator());
        contenido.append(System.lineSeparator()).append("--- Ultimo combate ---").append(System.lineSeparator());
        contenido.append(registro.getTexto());
        return contenido.toString();
    }

    public String getUltimoGuardado() {
        return ultimoGuardado;
    }

    private void registrarVictoriaEnBD(String nombreJugador, String nombreEnemigo) {
        try {
            MonstruoDAO monstruoDAO = new MonstruoDAO();
            Monstruo monstruo = monstruoDAO.buscarPorNombre(nombreEnemigo);
            if (monstruo != null) {
                JugadorDAO jugadorDAO = new JugadorDAO();
                Jugador jugador = jugadorDAO.buscarPorUsuario(nombreJugador);
                if (jugador == null) {
                    jugador = new Jugador(nombreJugador, "1234", "jugador");
                    jugadorDAO.registrar(jugador);
                    jugador = jugadorDAO.buscarPorUsuario(nombreJugador);
                }
                if (jugador != null) {
                    VictoriaDAO victoriaDAO = new VictoriaDAO();
                    boolean registrada = victoriaDAO.registrar(jugador.getId(), monstruo.getId());
                    if (registrada) {
                        System.out.println("[BD] Victoria registrada automaticamente en la base de datos.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[BD] No se pudo conectar a la base de datos para registrar la victoria.");
        }
    }
}
