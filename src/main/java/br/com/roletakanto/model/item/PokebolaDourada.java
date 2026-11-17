package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Concede giro bônus na rota atual. */
public final class PokebolaDourada extends Item {
  public PokebolaDourada(String id, String nome) {
    super(id, nome);
  }

  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto == null || contexto.partida() == null) {
      return new ResultadoUsoItem(false, "Partida invalida para conceder giro bonus.");
    }

    if (!contexto.partida().getRotaAtual().concederGiroBonus()) {
      return new ResultadoUsoItem(false, "A rota ja possui o maximo de tres giros.");
    }

    return new ResultadoUsoItem(true, "Giro bonus concedido.");
  }
}
