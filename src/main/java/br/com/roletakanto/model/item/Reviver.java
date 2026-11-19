package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Revive um Pokemon nocauteado com metade do HP maximo. */
public final class Reviver extends Item {
  public Reviver(String id, String nome) {
    super(id, nome);
  }

  @Override
  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto == null || contexto.partida() == null) {
      return new ResultadoUsoItem(false, "Partida invalida para usar o Reviver.");
    }

    if (!podeUsarEm(contexto.partida().getFase())) {
      return new ResultadoUsoItem(false, "O Reviver nao pode ser usado nesta fase.");
    }

    if (contexto.alvo() == null) {
      return new ResultadoUsoItem(false, "Selecione um Pokemon para usar o Reviver.");
    }

    if (contexto.alvo().estaApto()) {
      return new ResultadoUsoItem(false, "O Pokemon ja esta ativo.");
    }

    boolean aplicado = contexto.alvo().reviver(contexto.alvo().getHpMaximo() / 2);
    return new ResultadoUsoItem(
        aplicado,
        aplicado ? "Reviver aplicado." : "Nao foi possivel reviver o Pokemon.");
  }
}
