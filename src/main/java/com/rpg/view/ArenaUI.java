package com.rpg.view;

import com.rpg.controller.LoginController;
import com.rpg.controller.MonstruoController;
import com.rpg.controller.RankingController;
import java.util.Scanner;

public class ArenaUI {

    private LoginController loginController;
    private MonstruoController monstruoController;
    private RankingController rankingController;
    private LoginUI loginUI;
    private MonstruoUI monstruoUI;
    private RankingUI rankingUI;
    private Scanner scanner;

    public ArenaUI(Scanner scanner) {
        this.scanner = scanner;

        this.loginController = new LoginController();
        this.monstruoController = new MonstruoController();
        this.rankingController = new RankingController();

        this.loginUI = new LoginUI(loginController, scanner);
        this.monstruoUI = new MonstruoUI(monstruoController, loginController, scanner);
        this.rankingUI = new RankingUI(rankingController, scanner);
    }

    public void iniciar() {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n=== ARENA SEGURA - RPG ===");
            System.out.println("1. Login / Registro");
            System.out.println("2. Gestion de monstruos");
            System.out.println("3. Ranking y victorias");
            System.out.println("4. Demostracion inyeccion SQL");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opcion no valida");
                continue;
            }

            switch (opcion) {
                case 1:
                    loginUI.mostrarMenu();
                    break;

                case 2:
                    monstruoUI.mostrarMenu();
                    break;

                case 3:
                    rankingUI.mostrarMenu();
                    break;

                case 4:
                    com.rpg.demo.LoginInseguroDemo.main(new String[]{});
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
