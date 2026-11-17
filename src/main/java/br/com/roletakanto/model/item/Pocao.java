package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Cura 25 HP de um Pokemon apto. */
public final class Pocao extends Item {
  public Pocao(String id, String nome) {
    super(id, nome);
  }

  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto.alvo() == null) return new ResultadoUsoItem(false, "Selecione um Pokemon para usar a poção");
    contexto.alvo().curar(25);
    return new ResultadoUsoItem(true, "Pocao aplicada");
  }
}