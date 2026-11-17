package br.com.roletakanto.model;

import java.io.Serializable;

/** Uma pilha de itens do mesmo tipo. */
public final class PilhaItem implements Serializable {
  private final Item item;
  private int quantidade;

  public PilhaItem(Item item, int quantidade) {
    this.item = item;
    this.quantidade = quantidade;
  }

  public Item getItem() {
    return item;
  }

  public int getQuantidade() {
    return quantidade;
  }

  public int adicionar(int valor) {
    int livre = item.getLimitePilha() - quantidade;
    int adicionado = Math.max(0, Math.min(livre, valor));
    quantidade += adicionado;
    return adicionado;
  }

  public boolean remover(int valor) {
    if (valor <= 0 || valor > quantidade) return false;
    quantidade -= valor;
    return true;
  }
}
