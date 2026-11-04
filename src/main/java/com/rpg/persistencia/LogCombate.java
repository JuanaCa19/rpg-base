package com.rpg.persistencia;

import com.rpg.exceptions.PersistenciaException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogCombate {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final File archivo;

    public LogCombate(File archivo) {
        this.archivo = archivo;
    }

    public File getArchivo() {
        return archivo;
    }

    public synchronized void guardar(String texto, boolean agregar) throws PersistenciaException {
        try {
            prepararArchivo();
            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8, agregar))) {
                escritor.write("=== Registro guardado el " + LocalDateTime.now().format(FECHA) + " ===");
                escritor.newLine();
                for (String linea : texto.split("\\R")) {
                    escritor.write(linea);
                    escritor.newLine();
                }
                escritor.flush();
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo escribir el historial " + archivo.getPath()
                    + ": " + e.getMessage(), e);
        }
    }

    public synchronized List<String> leer() throws PersistenciaException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
            return lineas;
        } catch (FileNotFoundException e) {
            throw new PersistenciaException("Aun no hay historial de combates: no existe " + archivo.getPath(), e);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el historial " + archivo.getPath()
                    + ": " + e.getMessage(), e);
        }
    }

    public synchronized boolean borrar() {
        return archivo.delete();
    }

    private void prepararArchivo() throws IOException {
        Archivos.asegurarCarpeta(archivo.getAbsoluteFile().getParentFile());
        archivo.createNewFile();
    }
}
