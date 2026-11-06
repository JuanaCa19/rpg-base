package com.rpg.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/*
 * Clase encargada de la conexion a la base de datos MySQL.
 *
 * Que es JDBC?
 *   JDBC (Java Database Connectivity) es una API de Java que nos permite
 *   conectarnos a bases de datos relacionales, enviar consultas SQL
 *   y obtener resultados desde un programa Java.
 *
 * Que hace DriverManager?
 *   DriverManager es la clase que administra los drivers (controladores) de
 *   bases de datos. Cuando llamamos a DriverManager.getConnection(), el busca
 *   entre los drivers registrados cual puede manejar la URL que le pasamos
 *   y crea la conexion.
 *
 * Que pasa si falta el driver?
 *   Si no tenemos la dependencia mysql-connector-j en el pom.xml, al intentar
 *   conectar obtenemos el error:
 *   "java.sql.SQLException: No suitable driver found for jdbc:mysql://localhost:3306/rpg_arena_db"
 *   Esto pasa porque DriverManager no encuentra ningun driver capaz de entender
 *   el protocolo jdbc:mysql.
 *
 * URL de conexion: jdbc:mysql://localhost:3306/rpg_arena_db
 *   - jdbc:mysql  -> protocolo (JDBC con MySQL)
 *   - localhost    -> host (servidor donde esta MySQL)
 *   - 3306        -> puerto por defecto de MySQL
 *   - rpg_arena_db -> nombre del esquema/base de datos
 */
public class ConexionDB {

    private static String url;
    private static String usuario;
    private static String clave;
    private static boolean cargado = false;

    private static void cargarConfiguracion() {
        if (cargado) {
            return;
        }

        File archivo = new File("config.txt");

        if (!archivo.exists()) {
            System.out.println("Error: No se encontro el archivo config.txt en la raiz del proyecto.");
            System.out.println("Crea un archivo config.txt con las claves: url, usuario, clave");
            return;
        }

        Map<String, String> propiedades = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] partes = linea.split("=", 2);
                if (partes.length == 2) {
                    propiedades.put(partes[0].trim(), partes[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo config.txt: " + e.getMessage());
            return;
        }

        url = propiedades.get("url");
        usuario = propiedades.get("usuario");
        clave = propiedades.get("clave");

        if (url == null || usuario == null || clave == null) {
            System.out.println("Error: El archivo config.txt debe tener las claves: url, usuario, clave");
            return;
        }

        cargado = true;
    }

    public static Connection getConnection() throws SQLException {
        cargarConfiguracion();
        if (!cargado) {
            throw new SQLException("No se pudo cargar la configuracion de la base de datos");
        }
        return DriverManager.getConnection(url, usuario, clave);
    }
}
