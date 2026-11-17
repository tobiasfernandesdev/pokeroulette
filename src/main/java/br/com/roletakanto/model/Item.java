package br.com.roletakanto.model;

import java.io.Serializable;

/** Contrato polimórfico para itens do jogo. */
public abstract class Item implements Serializable {
  private final String id, nome;
  private final int limitePilha;
  private static final int LIMIT = 5;
  protected Item(String id, String nome) {
    this.id = id;
    this.nome = nome;
    this.limitePilha = LIMIT;
  }

  public abstract ResultadoUsoItem usar(ContextoUsoItem contexto);

  public boolean podeUsarEm(FasePartida fase) {
    return fase == FasePartida.ROTA || fase == FasePartida.BATALHA;
  }

  public String getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public int getLimitePilha() {
    return limitePilha;
  }
}
