package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.*;

/** Sorteia segmentos conforme seus pesos. */
public final class Roleta implements Serializable {
  private final List<SegmentoRoleta> segmentos;
  private final Random random;

  public Roleta(List<SegmentoRoleta> segmentos, Random random) {
    this.segmentos = List.copyOf(segmentos);
    this.random = random;
  }

  public ResultadoRoleta sortear() {
    if (segmentos.isEmpty()) throw new IllegalStateException("Roleta sem segmentos");
    SegmentoRoleta s = segmentos.get(random.nextInt(segmentos.size()));
    return new ResultadoRoleta(s.tipo(), s.idConteudo(), "Resultado sorteado");
  }

  public List<SegmentoRoleta> getSegmentos() {
    return segmentos;
  }
}
