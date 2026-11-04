package com.rpg.persistencia;

import com.rpg.concurrency.AutoGuardadoDaemon;
import com.rpg.entities.EstadoEfecto;
import com.rpg.entities.Gremio;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.PerfilJugador;
import com.rpg.entities.Personaje;
import com.rpg.entities.enums.RaridadItem;
import com.rpg.entities.enums.TipoEfecto;
import com.rpg.exceptions.CapacidadExcedidaException;
import com.rpg.exceptions.PersistenciaException;
import com.rpg.items.Pocion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PruebasPersistencia {

    private PruebasPersistencia() {
    }

    public static void main(String[] args) throws IOException, PersistenciaException, ClassNotFoundException,
            CapacidadExcedidaException, InterruptedException {
        File carpeta = Files.createTempDirectory("rpg-pruebas").toFile();
        System.out.println("Carpeta de pruebas: " + carpeta.getAbsolutePath());
        seccionFile(carpeta);
        escrituraYLectura(carpeta);
        borrado(carpeta);
        serializacion(carpeta);
        errores(carpeta);
        integracionConConcurrencia(carpeta);
    }


    private static void seccionFile(File carpeta) throws IOException {
        System.out.println("\n== File ==");
        File relativa = new File("partidas/demo.txt");
        File absoluta = relativa.getAbsoluteFile();
        System.out.println("   relativa: " + relativa.getPath() + " (depende de desde donde se lance el programa)");
        System.out.println("   absoluta: " + absoluta.getAbsolutePath() + " (siempre el mismo lugar)");

        File subcarpeta = new File(carpeta, "a/b");
        Archivos.asegurarCarpeta(subcarpeta);
        Archivos.asegurarCarpeta(subcarpeta);
        System.out.println("   mkdirs dos veces sin error: " + subcarpeta.isDirectory());

        File nuevo = new File(carpeta, "nuevo.txt");
        System.out.println("   createNewFile 1a vez: " + nuevo.createNewFile() + ", 2a vez: " + nuevo.createNewFile());
        System.out.print(Archivos.describir(nuevo));
    }

    private static void escrituraYLectura(File carpeta) throws PersistenciaException {
        System.out.println("\n== BufferedWriter / BufferedReader ==");
        LogCombate log = new LogCombate(new File(carpeta, "log.txt"));
        try {
            log.leer();
        } catch (PersistenciaException e) {
            System.out.println("   Archivo inexistente -> " + e.getMessage());
        }
        log.guardar("combate 1", false);
        log.guardar("combate 2", true);
        System.out.println("   Con append (true):       " + log.leer().size() + " lineas (conserva el combate 1)");
        log.guardar("combate 3", false);
        System.out.println("   Sobrescribiendo (false): " + log.leer().size() + " lineas (solo queda el ultimo)");
    }


    private static void borrado(File carpeta) throws IOException, PersistenciaException {
        System.out.println("\n== Borrado: File.delete() vs Files.deleteIfExists() ==");
        GestorPartida gestor = new GestorPartida();
        File inexistente = new File(carpeta, "no-existe.dat");
        System.out.println("   Archivo inexistente: delete()=" + inexistente.delete()
                + ", deleteIfExists()=" + gestor.borrar(inexistente) + " (los dos dicen false)");

        File noVacia = new File(carpeta, "noVacia");
        Archivos.asegurarCarpeta(noVacia);
        Files.writeString(new File(noVacia, "dentro.txt").toPath(), "hola");
        System.out.println("   Carpeta no vacia:    delete()=" + noVacia.delete() + " (false, sin decir por que)");
        try {
            gestor.borrar(noVacia);
        } catch (PersistenciaException e) {
            System.out.println("                        deleteIfExists() -> " + e.getMessage());
        }
        LogCombate log = new LogCombate(new File(carpeta, "log.txt"));
        System.out.println("   Borrar log existente con File.delete(): " + log.borrar());
    }


    private static final class MochilaRota implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String duenio = "Thor";
        private final Object talisman = new Object();
    }

    private static final class MochilaArreglada implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String duenio = "Thor";
        private final transient Object talisman = new Object();
    }

    private static void serializacion(File carpeta) throws IOException, PersistenciaException,
            ClassNotFoundException, CapacidadExcedidaException {
        System.out.println("\n== Serializacion ==");

        try {
            serializarEnMemoria(new MochilaRota());
        } catch (NotSerializableException e) {
            System.out.println("   NotSerializableException por el objeto anidado: " + e.getMessage());
        }
        Object recargada = deserializarEnMemoria(serializarEnMemoria(new MochilaArreglada()));
        MochilaArreglada arreglada = (MochilaArreglada) recargada;
        System.out.println("   Con transient: duenio=" + arreglada.duenio + ", talisman=" + arreglada.talisman
                + " (vuelve a su valor por defecto, null)");

        Gremio gremio = new Gremio("Guardianes del Alba");
        Personaje thor = new Guerrero("Thor");
        Personaje circe = new Mago("Circe");
        thor.unirseAGremio(gremio);
        circe.unirseAGremio(gremio);
        thor.getInventario().agregar(new Pocion("Elixir", 0, RaridadItem.RARO,
                new EstadoEfecto("Regeneracion", TipoEfecto.CURACION_CONTINUA, 6, 3), false));
        thor.aplicarEfecto(new EstadoEfecto("Veneno", TipoEfecto.DANIO_CONTINUO, 4, 10));
        PerfilJugador perfil = new PerfilJugador();
        perfil.registrarVictoria();
        perfil.desbloquearLogro("Primera Victoria");
        Map<String, Integer> derrotados = new LinkedHashMap<>();
        derrotados.put("Goblin", 2);

        GestorPartida gestor = new GestorPartida();
        File archivo = new File(carpeta, "partida.dat");
        System.out.println("   Thor antes de guardar: estado " + thor.getEstado() + ", efectos activos "
                + thor.getEfectos().size());
        gestor.guardar(new PartidaGuardada(perfil, gremio, derrotados), archivo);
        thor.cancelarEfectos();

        PartidaGuardada cargada = gestor.cargar(archivo);
        Gremio gremioCargado = cargada.getGremio();
        Personaje thorCargado = gremioCargado.getMiembros().get(0);
        System.out.println("   Cargada: victorias=" + cargada.getPerfil().getVictorias() + ", logros="
                + cargada.getPerfil().getLogros() + ", derrotados=" + cargada.getDerrotados());
        System.out.println("   Gremio con " + gremioCargado.getMiembros().size() + " miembros; el gremio de "
                + thorCargado.getNombre() + " es el mismo objeto del gremio cargado: "
                + (thorCargado.getGremio() == gremioCargado));
        System.out.println("   Arma: " + thorCargado.getArma().getNombre() + ", pociones: "
                + thorCargado.getInventario().tamanio() + ", habilidades: " + thorCargado.getHabilidades());
        System.out.println("   transient: efectos tras cargar = " + thorCargado.getEfectos().size()
                + ", estado = " + thorCargado.getEstado() + " (no se guardan los hilos de efectos)");
    }

    private static byte[] serializarEnMemoria(Object objeto) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(objeto);
        }
        return bytes.toByteArray();
    }

    private static Object deserializarEnMemoria(byte[] datos) throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(datos))) {
            return entrada.readObject();
        }
    }


    private static void errores(File carpeta) throws IOException, PersistenciaException {
        System.out.println("\n== Archivos inexistentes, corruptos o imposibles de escribir ==");
        GestorPartida gestor = new GestorPartida();

        probarCarga(gestor, new File(carpeta, "no-existe.dat"), "inexistente");

        File corrupto = new File(carpeta, "corrupto.dat");
        Files.writeString(corrupto.toPath(), "esto no es una partida");
        probarCarga(gestor, corrupto, "corrupto");

        File valido = new File(carpeta, "partida.dat");
        byte[] bytes = Files.readAllBytes(valido.toPath());
        File truncado = new File(carpeta, "truncado.dat");
        Files.write(truncado.toPath(), Arrays.copyOf(bytes, bytes.length / 2));
        probarCarga(gestor, truncado, "incompleto");

        File ajeno = new File(carpeta, "ajeno.dat");
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ajeno))) {
            salida.writeObject(new File("cualquier-cosa"));
        }
        probarCarga(gestor, ajeno, "con clases no permitidas (filtro)");

        File unArchivo = new File(carpeta, "soy-un-archivo");
        Files.writeString(unArchivo.toPath(), "x");
        try {
            gestor.guardar(new PartidaGuardada(new PerfilJugador(), new Gremio("G"), Map.of()),
                    new File(unArchivo, "partida.dat"));
        } catch (PersistenciaException e) {
            System.out.println("   Escritura imposible -> " + e.getMessage());
        }
    }

    private static void probarCarga(GestorPartida gestor, File archivo, String tipo) {
        try {
            gestor.cargar(archivo);
            System.out.println("   (" + tipo + ") se cargo sin error?");
        } catch (PersistenciaException e) {
            System.out.println("   Archivo " + tipo + " -> " + e.getMessage());
        }
    }


    private static void integracionConConcurrencia(File carpeta) throws PersistenciaException, InterruptedException {
        System.out.println("\n== Auto-guardado daemon + lectura simultanea del mismo archivo ==");
        GestorPartida gestor = new GestorPartida();
        File archivo = new File(carpeta, "concurrente.dat");
        Gremio gremio = new Gremio("Concurrentes");
        new Guerrero("Thor").unirseAGremio(gremio);
        PartidaGuardada partida = new PartidaGuardada(new PerfilJugador(), gremio, Map.of());
        gestor.guardar(partida, archivo);

        int[] guardados = {0};
        Thread daemon = new AutoGuardadoDaemon(5, () -> {
            try {
                gestor.guardar(partida, archivo);
                guardados[0]++;
            } catch (PersistenciaException e) {
                System.out.println("   guardado fallido: " + e.getMessage());
            }
        }).iniciar();

        int lecturasBuenas = 0;
        int lecturasMalas = 0;
        long fin = System.currentTimeMillis() + 1500;
        while (System.currentTimeMillis() < fin) {
            try {
                gestor.cargar(archivo);
                lecturasBuenas++;
            } catch (PersistenciaException e) {
                lecturasMalas++;
            }
        }
        System.out.println("   El daemon guardo ~" + guardados[0] + " veces mientras main leia: " + lecturasBuenas
                + " lecturas correctas, " + lecturasMalas + " fallidas");
        daemon.interrupt();
        daemon.join();
        String[] restos = carpeta.list((dir, nombre) -> nombre.startsWith("concurrente.dat"));
        System.out.println("   Archivos de esa partida en disco: " + Arrays.toString(restos)
                + " (sin .tmp: nunca queda un archivo a medio escribir)");
    }
}
