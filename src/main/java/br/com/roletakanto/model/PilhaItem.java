package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.Objects;

/** Uma pilha de itens do mesmo tipo. */
public final class PilhaItem implements Serializable {
  private final Item item;
  private int quantidade;

  PilhaItem(Item item, int quantidade) {
    this.item = Objects.requireNonNull(item, "O item da pilha e obrigatorio.");
    if (quantidade <= 0 || quantidade > item.getLimitePilha()) {
      throw new IllegalArgumentException("Quantidade inicial da pilha invalida.");
    }
    this.quantidade = quantidade;
  }

  public Item getItem() {
    return item;
  }

  public int getQuantidade() {
    return quantidade;
  }

  int adicionar(int valor) {
    if (valor <= 0) {
      return 0;
    }

    int livre = item.getLimitePilha() - quantidade;
    int adicionado = Math.max(0, Math.min(livre, valor));
    quantidade += adicionado;
    return adicionado;
  }

  boolean remover(int valor) {
    if (valor <= 0 || valor > quantidade) return false;
    quantidade -= valor;
    return true;
  }
}

