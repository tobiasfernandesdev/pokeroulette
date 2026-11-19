package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Concede um giro bonus na rota atual. */
public final class PokebolaDourada extends Item {
  public PokebolaDourada(String id, String nome) {
    super(id, nome);
  }

  @Override
  public boolean podeUsarEm(FasePartida fase) {
    return fase == FasePartida.ROTA;
  }

  @Override
  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto == null || contexto.partida() == null) {
      return new ResultadoUsoItem(false, "Partida invalida para conceder giro bonus.");
    }

    if (!podeUsarEm(contexto.partida().getFase())) {
      return new ResultadoUsoItem(false, "A Pokebola Dourada so pode ser usada na rota.");
    }

    if (!contexto.partida().getRotaAtual().concederGiroBonus()) {
      return new ResultadoUsoItem(false, "Esta rota ja recebeu seu giro bonus.");
    }

    return new ResultadoUsoItem(true, "Giro bonus concedido.");
  }
}
