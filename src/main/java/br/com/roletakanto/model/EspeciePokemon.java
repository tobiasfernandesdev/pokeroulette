package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.List;

/** Dados compartilhados por todos os Pokémon da mesma espécie. */
public final class EspeciePokemon implements Serializable {
  private final String id, nome;
  private final TipoPokemon tipo;
  private final int hpMaximo, ataque, defesa, velocidade;
  private final List<Golpe> golpes;

  public EspeciePokemon(
      String id,
      String nome,
      TipoPokemon tipo,
      int hpMaximo,
      int ataque,
      int defesa,
      int velocidade,
      List<Golpe> golpes) {
    this.id = id;
    this.nome = nome;
    this.tipo = tipo;
    this.hpMaximo = hpMaximo;
    this.ataque = ataque;
    this.defesa = defesa;
    this.velocidade = velocidade;
    this.golpes = List.copyOf(golpes);
  }

  public Pokemon criarInstancia() {
    return new Pokemon(java.util.UUID.randomUUID().toString(), this);
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

  public int getHpMaximo() {
    return hpMaximo;
  }

  public int getAtaque() {
    return ataque;
  }

  public int getDefesa() {
    return defesa;
  }

  public int getVelocidade() {
    return velocidade;
  }

  public List<Golpe> getGolpes() {
    return golpes;
  }
}
