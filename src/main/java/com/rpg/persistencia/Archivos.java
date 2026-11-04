package com.rpg.persistencia;

import java.io.File;
import java.io.IOException;

public final class Archivos {

    private Archivos() {
    }

    public static void asegurarCarpeta(File carpeta) throws IOException {
        if (carpeta != null && !carpeta.isDirectory() && !carpeta.mkdirs() && !carpeta.isDirectory()) {
            throw new IOException("No se pudo crear la carpeta " + carpeta.getPath());
        }
    }

    public static String describir(File archivo) {
        StringBuilder texto = new StringBuilder();
        texto.append(String.format("%s%n", archivo.getName()));
        texto.append(String.format("   ruta relativa: %s%n", archivo.getPath()));
        texto.append(String.format("   ruta absoluta: %s%n", archivo.getAbsolutePath()));
        if (archivo.exists()) {
            texto.append(String.format("   existe: si | tamano: %d bytes%n", archivo.length()));
        } else {
            texto.append(String.format("   existe: no%n"));
        }
        return texto.toString();
    }
}
