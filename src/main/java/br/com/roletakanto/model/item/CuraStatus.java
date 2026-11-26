package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;
import br.com.roletakanto.model.StatusPokemon;

/** Remove paralisia ou envenenamento de um Pokemon apto. */
public final class CuraStatus extends Item {
  public CuraStatus(String id, String nome) {
    super(id, nome);
  }

  @Override
  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto == null || contexto.partida() == null) {
      return new ResultadoUsoItem(false, "Partida invalida para usar a Cura de Status.");
    }
    if (!podeUsarEm(contexto.partida().getFase())) {
      return new ResultadoUsoItem(false, "A Cura de Status nao pode ser usada nesta fase.");
    }
    if (contexto.alvo() == null || !contexto.alvo().estaApto()) {
      return new ResultadoUsoItem(false, "Selecione um Pokemon apto para usar a Cura de Status.");
    }
    if (contexto.alvo().getStatus() == StatusPokemon.NENHUM) {
      return new ResultadoUsoItem(false, "Esse Pokemon nao possui uma condicao para curar.");
    }

    contexto.alvo().curarStatus();
    return new ResultadoUsoItem(true, contexto.alvo().getEspecie().getNome() + " foi curado do status.");
  }
}
