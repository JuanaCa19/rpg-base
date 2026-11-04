package com.rpg;

import com.rpg.core.Consola;
import com.rpg.core.MotorJuego;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Consola consola = new Consola(scanner);
            MotorJuego motor = new MotorJuego(consola);
            motor.iniciarAutoGuardado();
            if (motor.hayPartidaGuardada()) {
                System.out.println("Hay una partida guardada: usa la opcion 6 para cargarla "
                        + "(se sobrescribe cuando termines el proximo combate).");
            }
            ejecutarMenu(consola, motor);
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada finalizada. Hasta luego.");
        }
    }

    private static void ejecutarMenu(Consola consola, MotorJuego motor) {
        int opcion;
        do {
            mostrarMenu();
            opcion = consola.leerEntero("Opcion: ", 0, 8);
            switch (opcion) {
                case 1 -> motor.combateRapido();
                case 2 -> motor.jugarTorneo();
                case 3 -> motor.mostrarBestiario();
                case 4 -> motor.verHistorial();
                case 5 -> motor.guardarProgreso();
                case 6 -> motor.cargarPartida();
                case 7 -> motor.mostrarArchivos();
                case 8 -> motor.borrarPartida();
                default -> System.out.println("Hasta luego.");
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n=== SIMULADOR DE BATALLA RPG ===");
        System.out.println("1. Combate rapido");
        System.out.println("2. Torneo");
        System.out.println("3. Ver bestiario");
        System.out.println("4. Ver historial de combates");
        System.out.println("5. Guardar partida");
        System.out.println("6. Cargar partida");
        System.out.println("7. Ver archivos de la partida");
        System.out.println("8. Borrar partida guardada");
        System.out.println("0. Salir");
    }
}
