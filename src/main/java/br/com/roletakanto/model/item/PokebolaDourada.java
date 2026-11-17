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
    return new ResultadoUsoItem(true, "TODO: giro bônus concedido");
  }
}
