package br.com.roletakanto.model.item;

import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.ResultadoUsoItem;

/** Reserva um resultado de roleta; a regra detalhada será conectada ao controlador. */
public final class Radar extends Item {
  public Radar(String id, String nome) {
    super(id, nome);
  }

  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    return new ResultadoUsoItem(true, "TODO: resultado reservado pelo Radar");
  }
}
