package br.com.roletakanto.model.item;

import br.com.roletakanto.model.*;

/** Cura 25 HP de um Pokémon apto. */
public final class Pocao extends Item {
  public Pocao(String id, String nome, int limitePilha) {
    super(id, nome, limitePilha);
  }

  public ResultadoUsoItem usar(ContextoUsoItem contexto) {
    if (contexto.alvo() == null) return new ResultadoUsoItem(false, "Selecione um Pokémon");
    contexto.alvo().curar(25);
    return new ResultadoUsoItem(true, "Poção aplicada");
  }
}
