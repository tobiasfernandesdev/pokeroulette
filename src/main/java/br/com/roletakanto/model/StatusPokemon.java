package br.com.roletakanto.model;

public enum StatusPokemon {
  NENHUM("sem condição"),
  ENVENENADO("envenenado"),
  PARALISADO("paralisado");

  private final String descricao;

  StatusPokemon(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
