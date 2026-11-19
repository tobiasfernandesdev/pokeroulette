package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Cura ate 50 HP de um Pokemon apto. */
public final class SuperPocao extends Item {
  private static final int CURA = 50;

  public SuperPocao(String id, String nome) {
    super(id, nome);
  }

  @Override
  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto == null || contexto.partida() == null) {
      return new ResultadoUsoItem(false, "Partida invalida para usar a Super Pocao.");
    }

    if (!podeUsarEm(contexto.partida().getFase())) {
      return new ResultadoUsoItem(false, "A Super Pocao nao pode ser usada nesta fase.");
    }

    if (contexto.alvo() == null) {
      return new ResultadoUsoItem(false, "Selecione um Pokemon para usar a Super Pocao.");
    }

    if (!contexto.alvo().estaApto()) {
      return new ResultadoUsoItem(false, "Pokemon nocauteado nao pode ser curado.");
    }

    int recuperado = contexto.alvo().curar(CURA);
    if (recuperado == 0) {
      return new ResultadoUsoItem(false, "O Pokemon ja esta com HP maximo.");
    }

    return new ResultadoUsoItem(true, "Super Pocao aplicada: " + recuperado + " HP recuperado.");
  }
}
