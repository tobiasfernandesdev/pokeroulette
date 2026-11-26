package br.com.roletakanto.model;

import java.io.Serializable;

/** Definição imutável de um golpe. */
public final class Golpe implements Serializable {
  private final String id;
  private final String nome;
  private final TipoPokemon tipo;
  private final int potencia;
  private final int usosMaximos;
  private final StatusPokemon statusInfligido;
  private final int chanceStatus;

  public Golpe(String id, String nome, TipoPokemon tipo, int potencia, int usosMaximos) {
    this(id, nome, tipo, potencia, usosMaximos, StatusPokemon.NENHUM, 0);
  }

  public Golpe(
      String id,
      String nome,
      TipoPokemon tipo,
      int potencia,
      int usosMaximos,
      StatusPokemon statusInfligido,
      int chanceStatus) {
    if (chanceStatus < 0 || chanceStatus > 100) {
      throw new IllegalArgumentException("A chance de status deve estar entre 0 e 100.");
    }
    this.id = id;
    this.nome = nome;
    this.tipo = tipo;
    this.potencia = potencia;
    this.usosMaximos = usosMaximos;
    this.statusInfligido = statusInfligido == null ? StatusPokemon.NENHUM : statusInfligido;
    this.chanceStatus = chanceStatus;
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

  public StatusPokemon getStatusInfligido() {
    return statusInfligido;
  }

  public int getChanceStatus() {
    return chanceStatus;
  }
}
