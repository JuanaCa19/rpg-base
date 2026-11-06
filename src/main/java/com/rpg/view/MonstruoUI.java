package com.rpg.view;

import com.rpg.controller.MonstruoController;
import com.rpg.controller.LoginController;
import com.rpg.model.Monstruo;
import java.util.List;
import java.util.Scanner;

public class MonstruoUI {

    private MonstruoController monstruoController;
    private LoginController loginController;
    private Scanner scanner;

    public MonstruoUI(MonstruoController monstruoController, LoginController loginController, Scanner scanner) {
        this.monstruoController = monstruoController;
        this.loginController = loginController;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n=== GESTION DE MONSTRUOS ===");
            System.out.println("1. Listar monstruos");
            System.out.println("2. Buscar por nombre");
            System.out.println("3. Listar por vida minima");
            System.out.println("4. Crear monstruo (admin)");
            System.out.println("5. Editar monstruo (admin)");
            System.out.println("6. Eliminar monstruo (admin)");
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
                    List<Monstruo> monstruos = monstruoController.listarTodos();
                    if (monstruos.isEmpty()) {
                        System.out.println("No hay monstruos registrados");
                    } else {
                        for (Monstruo m : monstruos) {
                            System.out.println("ID: " + m.getId() + " | Nombre: " + m.getNombre()
                                    + " | Vida: " + m.getVida() + " | Ataque: " + m.getAtaque());
                        }
                    }
                    break;

                case 2:
                    System.out.print("Ingrese nombre del monstruo: ");
                    String nombre = scanner.nextLine();
                    Monstruo encontrado = monstruoController.buscarPorNombre(nombre);
                    if (encontrado != null) {
                        System.out.println("ID: " + encontrado.getId() + " | Nombre: " + encontrado.getNombre()
                                + " | Vida: " + encontrado.getVida() + " | Ataque: " + encontrado.getAtaque());
                    } else {
                        System.out.println("No se encontro el monstruo");
                    }
                    break;

                case 3:
                    System.out.print("Ingrese vida minima: ");
                    int vidaMinima = Integer.parseInt(scanner.nextLine());
                    List<Monstruo> filtrados = monstruoController.listarPorVidaMinima(vidaMinima);
                    if (filtrados.isEmpty()) {
                        System.out.println("No se encontraron monstruos con esa vida minima");
                    } else {
                        for (Monstruo m : filtrados) {
                            System.out.println("ID: " + m.getId() + " | Nombre: " + m.getNombre()
                                    + " | Vida: " + m.getVida() + " | Ataque: " + m.getAtaque());
                        }
                    }
                    break;

                case 4:
                    if (!loginController.esAdmin()) {
                        System.out.println("Solo un admin puede realizar esta accion");
                        break;
                    }
                    System.out.print("Ingrese nombre: ");
                    String nombreNuevo = scanner.nextLine();
                    System.out.print("Ingrese vida: ");
                    int vida = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese ataque: ");
                    int ataque = Integer.parseInt(scanner.nextLine());
                    String resultadoCrear = monstruoController.crear(nombreNuevo, vida, ataque);
                    System.out.println(resultadoCrear);
                    break;

                case 5:
                    if (!loginController.esAdmin()) {
                        System.out.println("Solo un admin puede realizar esta accion");
                        break;
                    }
                    System.out.print("Ingrese ID del monstruo: ");
                    int idEditar = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese nuevo nombre: ");
                    String nombreEditar = scanner.nextLine();
                    System.out.print("Ingrese nueva vida: ");
                    int vidaEditar = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese nuevo ataque: ");
                    int ataqueEditar = Integer.parseInt(scanner.nextLine());
                    String resultadoEditar = monstruoController.actualizar(idEditar, nombreEditar, vidaEditar, ataqueEditar);
                    System.out.println(resultadoEditar);
                    break;

                case 6:
                    if (!loginController.esAdmin()) {
                        System.out.println("Solo un admin puede realizar esta accion");
                        break;
                    }
                    System.out.print("Ingrese ID del monstruo a eliminar: ");
                    int idEliminar = Integer.parseInt(scanner.nextLine());
                    String resultadoEliminar = monstruoController.eliminar(idEliminar);
                    System.out.println(resultadoEliminar);
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
