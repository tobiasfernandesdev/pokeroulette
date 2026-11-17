package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Cura 50 HP de um Pokemon apto. */
public final class SuperPocao extends Item {
  public SuperPocao(String id, String nome) {
    super(id, nome);
  }

  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto.alvo() == null) return new ResultadoUsoItem(false, "Selecione um Pokemon para usar a super poção");
    contexto.alvo().curar(50);
    return new ResultadoUsoItem(true, "Super Poçao aplicada");
  }
}
