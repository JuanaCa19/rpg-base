package com.rpg.functional;

import com.rpg.core.RegistroCombate;
import com.rpg.entities.Escuadron;
import com.rpg.entities.Personaje;
import com.rpg.entities.enums.TipoPersonaje;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimuladorBatallaGrupal {

    private final RegistroCombate registro;
    private final ModificadorDanio pipelineDanio;

    public SimuladorBatallaGrupal(RegistroCombate registro, ModificadorDanio pipelineDanio) {
        this.registro = registro;
        this.pipelineDanio = pipelineDanio != null ? pipelineDanio : (danio, atacante, defensor) -> danio;
    }

    public static final Predicate<Personaje> TIENE_VIDA_CRITICA =
            personaje -> personaje.estaVivo() && (personaje.getVida() * 100 / personaje.getVidaMaxima()) < 30;

    public static final Predicate<Personaje> ES_COMBATIENTE_FISICO =
            personaje -> personaje.estaVivo() &&
                    (personaje.getTipo() == TipoPersonaje.GUERRERO || personaje.getTipo() == TipoPersonaje.MONSTRUO);

    public void ejecutarRondaGrupal(Escuadron bandoA, Escuadron bandoB) {
        List<Personaje> atacantesVivos = bandoA.obtenerVivos();
        if (atacantesVivos.isEmpty() || !bandoB.haySobrevivientes()) {
            return;
        }

        registro.agregar("%n=== RONDA GRUPAL: %s vs %s ===", bandoA.getNombre(), bandoB.getNombre());

        Consumer<Personaje> accionAtaque = atacante -> {
            if (!bandoB.haySobrevivientes()) {
                return;
            }
            Optional<Personaje> objetivoOpt = bandoB.getMiembros().stream()
                    .filter(Personaje::estaVivo)
                    .min(Comparator.comparingInt(Personaje::getVida));

            objetivoOpt.ifPresent(defensor -> {
                int danioBase = atacante.calcularDanio();
                int danioFinal = pipelineDanio.aplicar(danioBase, atacante, defensor);
                int vidaAntes = defensor.getVida();
                defensor.defender(danioFinal);
                registro.agregar("%s arremete contra %s causando %d de dano (HP restante: %d/%d)",
                        atacante.getNombre(), defensor.getNombre(),
                        vidaAntes - defensor.getVida(), defensor.getVida(), defensor.getVidaMaxima());
            });
        };

        atacantesVivos.forEach(accionAtaque);
    }

    public void curarEnArea(Escuadron escuadron, Predicate<Personaje> filtro, int cantidadCuracion) {
        Consumer<Personaje> curar = miembro -> {
            int curado = miembro.curar(cantidadCuracion);
            registro.agregar("Bendicion sobre %s: recupera +%d de vida (HP actual: %d/%d)",
                    miembro.getNombre(), curado, miembro.getVida(), miembro.getVidaMaxima());
        };

        escuadron.getMiembros().stream()
                .filter(Personaje::estaVivo)
                .filter(filtro)
                .forEach(curar);
    }

    public String generarInformeEscuadron(Escuadron escuadron) {
        StringBuilder informe = new StringBuilder();
        informe.append(String.format("Reporte de Escuadron '%s':%n", escuadron.getNombre()));

        Map<TipoPersonaje, List<Personaje>> agrupadosPorTipo = escuadron.getMiembros().stream()
                .collect(Collectors.groupingBy(Personaje::getTipo));

        agrupadosPorTipo.forEach((tipo, miembros) -> {
            String listaNombres = miembros.stream()
                    .map(m -> String.format("%s (%d HP - %s)", m.getNombre(), m.getVida(), m.getEstado()))
                    .collect(Collectors.joining(", "));
            informe.append(String.format("  [%s x%d]: %s%n", tipo, miembros.size(), listaNombres));
        });

        int vidaTotal = escuadron.calcularVidaTotal();
        double vidaPromedio = escuadron.calcularVidaPromedio();
        long vivos = escuadron.contarVivos();

        informe.append(String.format("  Sobrevivientes: %d/%d | Vida Total: %d | Promedio Vida: %.1f%n",
                vivos, escuadron.getMiembros().size(), vidaTotal, vidaPromedio));

        String habilidadesTotales = escuadron.obtenerTodasLasHabilidades().stream()
                .collect(Collectors.joining(" | "));
        informe.append(String.format("  Habilidades del escuadron: [%s]%n", habilidadesTotales));

        return informe.toString();
    }
}
