package br.com.roletakanto.model;

import java.io.Serializable;

/** Instância capturada que possui HP e estado próprios. */
public final class Pokemon implements Serializable {
  private final String id;
  private final EspeciePokemon especie;
  private int hpAtual;
  private StatusPokemon status = StatusPokemon.NENHUM;

  public Pokemon(String id, EspeciePokemon especie) {
    this.id = id;
    this.especie = especie;
    this.hpAtual = especie.getHpMaximo();
  }

  public int receberDano(int dano) {
    hpAtual = Math.max(0, hpAtual - Math.max(0, dano));
    return hpAtual;
  }

  public int curar(int quantidade) {
    int antes = hpAtual;
    hpAtual = Math.min(getHpMaximo(), hpAtual + Math.max(0, quantidade));
    return hpAtual - antes;
  }

  public boolean reviver(int hp) {
    if (estaApto()) return false;
    hpAtual = Math.max(1, Math.min(getHpMaximo(), hp));
    return true;
  }

  public boolean estaApto() {
    return hpAtual > 0;
  }

  public EstadoPokemon getEstado() {
    return estaApto() ? EstadoPokemon.ATIVO : EstadoPokemon.DESMAIADO;
  }

  public StatusPokemon getStatus() {
    return status == null ? StatusPokemon.NENHUM : status;
  }

  public boolean aplicarStatus(StatusPokemon novoStatus) {
    if (!estaApto()
        || novoStatus == null
        || novoStatus == StatusPokemon.NENHUM
        || getStatus() != StatusPokemon.NENHUM) {
      return false;
    }
    status = novoStatus;
    return true;
  }

  public boolean curarStatus() {
    if (getStatus() == StatusPokemon.NENHUM) {
      return false;
    }
    status = StatusPokemon.NENHUM;
    return true;
  }

  public int getHpAtual() {
    return hpAtual;
  }

  public int getHpMaximo() {
    return especie.getHpMaximo();
  }

  public String getId() {
    return id;
  }

  public EspeciePokemon getEspecie() {
    return especie;
  }

  public void restaurarParaCentro() {
    hpAtual = getHpMaximo();
    status = StatusPokemon.NENHUM;
  }
}
