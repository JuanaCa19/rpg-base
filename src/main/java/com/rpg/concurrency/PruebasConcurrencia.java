package com.rpg.concurrency;

import com.rpg.entities.EstadoEfecto;
import com.rpg.entities.Guerrero;
import com.rpg.entities.Mago;
import com.rpg.entities.Personaje;
import com.rpg.entities.enums.TipoEfecto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public final class PruebasConcurrencia {

    private static final int HILOS = 4;
    private static final int GOLPES_POR_HILO = 100_000;

    private PruebasConcurrencia() {
    }

    public static void main(String[] args) throws InterruptedException {
        condicionDeCarrera();
        efectosInterrumpidos();
        reentrantLock();
        hiloDaemon();
    }


    private static final class VidaSinProteccion {
        private int vida;

        VidaSinProteccion(int vida) {
            this.vida = vida;
        }

        void recibirDanio(int cantidad) {
            int nueva = vida - cantidad;
            Thread.yield();
            vida = nueva;
        }
    }

    private static void condicionDeCarrera() throws InterruptedException {
        int total = HILOS * GOLPES_POR_HILO;
        int vidaInicial = 2 * total;
        System.out.printf("== 1. Condicion de carrera: %d hilos x %d golpes de 1 de dano (esperado: %d) ==%n",
                HILOS, GOLPES_POR_HILO, vidaInicial - total);

        VidaSinProteccion sinProteccion = new VidaSinProteccion(vidaInicial);
        correrEnParalelo(() -> {
            for (int i = 0; i < GOLPES_POR_HILO; i++) {
                sinProteccion.recibirDanio(1);
            }
        });
        System.out.printf("   Sin proteccion:        vida final %d (se perdieron %d golpes)%n",
                sinProteccion.vida, sinProteccion.vida - (vidaInicial - total));

        Personaje protegido = new Guerrero("Protegido");
        protegido.setVidaMaxima(vidaInicial);
        protegido.setVida(vidaInicial);
        correrEnParalelo(() -> {
            for (int i = 0; i < GOLPES_POR_HILO; i++) {
                protegido.recibirDanio(1);
            }
        });
        System.out.printf("   Personaje.recibirDanio: vida final %d (esperado %d)%n",
                protegido.getVida(), vidaInicial - total);
    }

    private static void correrEnParalelo(Runnable tarea) throws InterruptedException {
        CountDownLatch salida = new CountDownLatch(1);
        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            Thread hilo = new Thread(() -> {
                try {
                    salida.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                tarea.run();
            }, "carrera-" + i);
            hilos.add(hilo);
            hilo.start();
        }
        salida.countDown();
        for (Thread hilo : hilos) {
            hilo.join();
        }
    }


    private static void efectosInterrumpidos() throws InterruptedException {
        System.out.println("\n== 2. Efectos DoT en hilos propios ==");

        Personaje heroe = new Guerrero("Heroe");
        heroe.setNotificador(mensaje -> System.out.println("   [efecto] " + mensaje));
        heroe.aplicarEfecto(new EstadoEfecto("Veneno", TipoEfecto.DANIO_CONTINUO, 4, 10));
        Thread.sleep(2500);
        System.out.printf("   Tras ~2 ticks: vida %d, estado %s, hilos de efecto vivos: %d%n",
                heroe.getVida(), heroe.getEstado(), hilosDeEfectoVivos());

        heroe.quitarEfectosDanio();
        Thread.sleep(200);
        int vidaTrasAntidoto = heroe.getVida();
        Thread.sleep(1500);
        System.out.printf("   Tras el antidoto: estado %s, hilos vivos: %d, vida %d -> %d (ya no baja)%n",
                heroe.getEstado(), hilosDeEfectoVivos(), vidaTrasAntidoto, heroe.getVida());

        Personaje mago = new Mago("Fragil");
        mago.setNotificador(mensaje -> System.out.println("   [efecto] " + mensaje));
        mago.setVida(5);
        mago.aplicarEfecto(new EstadoEfecto("Quemadura", TipoEfecto.DANIO_CONTINUO, 4, 10));
        Thread.sleep(2500);
        System.out.printf("   Muerto por el efecto: estado %s, hilos vivos: %d%n", mago.getEstado(), hilosDeEfectoVivos());
    }

    private static long hilosDeEfectoVivos() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(hilo -> hilo.getName().startsWith("efecto-") && hilo.isAlive())
                .count();
    }


    private static final class VidaConLock {
        private final ReentrantLock candado = new ReentrantLock();
        private int vida;

        VidaConLock(int vida) {
            this.vida = vida;
        }

        void recibirDanio(int cantidad) {
            candado.lock();
            try {
                vida = Math.max(0, vida - cantidad);
            } finally {
                candado.unlock();
            }
        }
    }

    private static void reentrantLock() throws InterruptedException {
        System.out.println("\n== 3. ReentrantLock ==");
        int total = HILOS * GOLPES_POR_HILO;
        VidaConLock vida = new VidaConLock(2 * total);
        correrEnParalelo(() -> {
            for (int i = 0; i < GOLPES_POR_HILO; i++) {
                vida.recibirDanio(1);
            }
        });
        System.out.printf("   Con ReentrantLock: vida final %d (esperado %d)%n", vida.vida, total);
    }


    private static void hiloDaemon() {
        System.out.println("\n== 4. Hilo daemon ==");
        Thread daemon = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "daemon-de-prueba");
        daemon.setDaemon(true);
        daemon.start();
        try {
            daemon.setDaemon(false);
        } catch (IllegalThreadStateException e) {
            System.out.println("   setDaemon() despues de start() lanza " + e.getClass().getSimpleName());
        }
        System.out.println("   main termina con el daemon vivo (isAlive=" + daemon.isAlive()
                + "): la JVM cierra igual, no espera a los daemon.");
    }
}
