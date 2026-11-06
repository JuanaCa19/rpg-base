package com.rpg.demo;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import com.rpg.dao.ConexionDB;

public class LoginInseguroDemo {

    public void autenticar(String usuario, String clave) {
        String sql = "SELECT * FROM jugadores WHERE usuario = '" + usuario + "' AND clave = '" + clave + "'";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("LOGIN EXITOSO (inseguro) - Usuario: " + rs.getString("usuario"));
            } else {
                System.out.println("Login fallido");
            }

        } catch (Exception e) {
            System.out.println("Error en la autenticacion insegura: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LoginInseguroDemo demo = new LoginInseguroDemo();
        com.rpg.dao.JugadorDAO daoSeguro = new com.rpg.dao.JugadorDAO();

        String payload = "admin' OR 1=1 -- ";
        String claveCualquiera = "cualquiera";

        System.out.println("==================================================");
        System.out.println("DEMOSTRACION: INYECCION SQL VS PREPAREDSTATEMENT");
        System.out.println("==================================================");
        System.out.println("Entrada maliciosa para usuario: " + payload);
        System.out.println("Clave ingresada: " + claveCualquiera);
        System.out.println();

        System.out.println("--- 1. Prueba con Statement (INSEGURO / CONCATENACION) ---");
        demo.autenticar(payload, claveCualquiera);

        System.out.println();
        System.out.println("--- 2. Prueba con PreparedStatement (SEGURO / DAO) ---");
        com.rpg.model.Jugador resultado = daoSeguro.autenticar(payload, claveCualquiera);
        if (resultado != null) {
            System.out.println("LOGIN EXITOSO (inesperado) - Usuario: " + resultado.getUsuario());
        } else {
            System.out.println("LOGIN FALLIDO: La inyeccion fue bloqueada exitosamente por PreparedStatement.");
        }
        System.out.println("==================================================");
    }
}
