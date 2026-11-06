package com.rpg.view;

import com.rpg.controller.LoginController;
import java.util.Scanner;

public class LoginUI {

    private LoginController loginController;
    private Scanner scanner;

    public LoginUI(LoginController loginController, Scanner scanner) {
        this.loginController = loginController;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n=== LOGIN ===");
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar sesion");
            System.out.println("3. Cerrar sesion");
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
                    System.out.print("Ingrese usuario: ");
                    String nuevoUsuario = scanner.nextLine();
                    System.out.print("Ingrese clave: ");
                    String nuevaClave = scanner.nextLine();
                    String resultadoRegistro = loginController.registrar(nuevoUsuario, nuevaClave);
                    System.out.println(resultadoRegistro);
                    break;

                case 2:
                    System.out.print("Ingrese usuario: ");
                    String usuario = scanner.nextLine();
                    System.out.print("Ingrese clave: ");
                    String clave = scanner.nextLine();
                    String resultadoLogin = loginController.login(usuario, clave);
                    System.out.println(resultadoLogin);
                    break;

                case 3:
                    String resultadoCierre = loginController.cerrarSesion();
                    System.out.println(resultadoCierre);
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
