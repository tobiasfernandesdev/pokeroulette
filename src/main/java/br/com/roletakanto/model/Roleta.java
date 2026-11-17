package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Sorteia segmentos conforme seus pesos. */
public final class Roleta implements Serializable {
  private final List<SegmentoRoleta> segmentos;
  private final Random random;

  public Roleta(List<SegmentoRoleta> segmentos, Random random) {
    this.segmentos = List.copyOf(Objects.requireNonNull(segmentos, "Segmentos obrigatorios."));
    this.random = Objects.requireNonNull(random, "Gerador aleatorio obrigatorio.");
  }

  public ResultadoRoleta sortear() {
    if (segmentos.isEmpty()) {
      throw new IllegalStateException("Roleta sem segmentos.");
    }

    if (segmentos.stream().anyMatch(segmento -> segmento.peso() <= 0)) {
      throw new IllegalStateException("Todos os segmentos devem possuir peso positivo.");
    }
    int pesoTotal = segmentos.stream().mapToInt(SegmentoRoleta::peso).sum();

    int numeroSorteado = random.nextInt(pesoTotal) + 1;
    int pesoAcumulado = 0;

    for (SegmentoRoleta segmento : segmentos) {
      pesoAcumulado += segmento.peso();
      if (numeroSorteado <= pesoAcumulado) {
        return new ResultadoRoleta(
            segmento.tipo(), segmento.idConteudo(), "Resultado sorteado");
      }
    }

    throw new IllegalStateException("Nao foi possivel determinar o resultado da roleta.");
  }

  public List<SegmentoRoleta> getSegmentos() {
    return segmentos;
  }
}
