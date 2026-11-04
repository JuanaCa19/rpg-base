package com.rpg.persistencia;

import com.rpg.exceptions.PersistenciaException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GestorPartida {

    public static final File DIRECTORIO = new File("partidas");
    public static final File PARTIDA = new File(DIRECTORIO, "partida.dat");
    public static final File AUTOGUARDADO = new File(DIRECTORIO, "autoguardado.dat");

    private static final String FILTRO = "maxdepth=100;maxrefs=100000;maxbytes=5000000;"
            + "com.rpg.**;java.lang.*;java.util.*;!*";

    private final Object candado = new Object();

    public void guardar(PartidaGuardada partida, File destino) throws PersistenciaException {
        synchronized (candado) {
            File temporal = new File(destino.getPath() + ".tmp");
            try {
                Archivos.asegurarCarpeta(destino.getAbsoluteFile().getParentFile());
                try (ObjectOutputStream salida = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(temporal)))) {
                    salida.writeObject(partida);
                }
                reemplazar(temporal, destino);
            } catch (NotSerializableException e) {
                throw new PersistenciaException("Hay un objeto que no es Serializable: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new PersistenciaException("No se pudo guardar en " + destino.getPath() + ": " + e.getMessage(), e);
            } finally {
                if (temporal.exists() && !temporal.delete()) {
                    System.out.println("Aviso: no se pudo borrar el temporal " + temporal.getPath());
                }
            }
        }
    }

    public PartidaGuardada cargar(File origen) throws PersistenciaException {
        synchronized (candado) {
            try (ObjectInputStream entrada = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(origen)))) {
                entrada.setObjectInputFilter(ObjectInputFilter.Config.createFilter(FILTRO));
                return (PartidaGuardada) entrada.readObject();
            } catch (FileNotFoundException e) {
                String motivo = origen.exists() ? "no se puede abrir (" + e.getMessage() + ")" : "no existe";
                throw new PersistenciaException("No se pudo cargar " + origen.getPath() + ": " + motivo, e);
            } catch (EOFException e) {
                throw new PersistenciaException("El archivo " + origen.getName() + " esta incompleto", e);
            } catch (InvalidClassException | StreamCorruptedException e) {
                throw new PersistenciaException("El archivo " + origen.getName()
                        + " esta corrupto, es de otra version del juego o contiene clases no permitidas", e);
            } catch (IOException e) {
                throw new PersistenciaException("No se pudo leer " + origen.getPath() + ": " + e.getMessage(), e);
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new PersistenciaException("El archivo " + origen.getName()
                        + " no contiene una partida valida", e);
            }
        }
    }

    public boolean borrar(File archivo) throws PersistenciaException {
        synchronized (candado) {
            try {
                return Files.deleteIfExists(archivo.toPath());
            } catch (IOException e) {
                throw new PersistenciaException("No se pudo borrar " + archivo.getPath() + ": " + e, e);
            }
        }
    }

    private void reemplazar(File temporal, File destino) throws IOException {
        try {
            Files.move(temporal.toPath(), destino.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
