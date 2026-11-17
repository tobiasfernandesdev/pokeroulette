package br.com.roletakanto.model;

import java.io.Serializable;

/** Definição imutável de um golpe. */
public final class Golpe implements Serializable {
  private final String id;
  private final String nome;
  private final TipoPokemon tipo;
  private final int potencia;
  private final int usosMaximos;

  public Golpe(String id, String nome, TipoPokemon tipo, int potencia, int usosMaximos) {
    this.id = id;
    this.nome = nome;
    this.tipo = tipo;
    this.potencia = potencia;
    this.usosMaximos = usosMaximos;
  }

  public String getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public TipoPokemon getTipo() {
    return tipo;
  }

  public int getPotencia() {
    return potencia;
  }

  public int getUsosMaximos() {
    return usosMaximos;
  }

  public boolean ehIlimitado() {
    return usosMaximos == 0;
  }
}
