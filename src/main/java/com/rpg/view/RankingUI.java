package com.rpg.view;

import com.rpg.controller.RankingController;
import com.rpg.model.Victoria;
import java.util.List;
import java.util.Scanner;

public class RankingUI {

    private RankingController rankingController;
    private Scanner scanner;

    public RankingUI(RankingController rankingController, Scanner scanner) {
        this.rankingController = rankingController;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n=== RANKING Y VICTORIAS ===");
            System.out.println("1. Ver ranking general");
            System.out.println("2. Ver victorias de un jugador");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opcion no valida");
                continue;
            }

            switch (opcion) {
                case 1:
                    List<String[]> ranking = rankingController.obtenerRanking();
                    if (ranking.isEmpty()) {
                        System.out.println("No hay datos en el ranking");
                    } else {
                        System.out.println("Posicion | Jugador | Victorias");
                        System.out.println("---------|---------|----------");
                        int posicion = 1;
                        for (String[] entrada : ranking) {
                            System.out.println(posicion + "        | " + entrada[0] + "    | " + entrada[1]);
                            posicion++;
                        }
                    }
                    break;

                case 2:
                    System.out.print("Ingrese nombre del jugador: ");
                    String usuario = scanner.nextLine();
                    List<Victoria> victorias = rankingController.obtenerVictoriasDeJugador(usuario);
                    if (victorias == null || victorias.isEmpty()) {
                        System.out.println("No se encontraron victorias para ese jugador");
                    } else {
                        System.out.println("Victorias de " + usuario + ":");
                        for (Victoria v : victorias) {
                            System.out.println("- Monstruo: " + v.getNombreMonstruo() + " | Fecha: " + v.getFecha());
                        }
                    }
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Opcion no valida");
                    break;
            }
        }
    }
}
