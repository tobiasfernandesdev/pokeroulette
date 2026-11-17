package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Estado central da campanha. */
public final class Partida implements Serializable {
  private final Jogador jogador;
  private final List<Rota> rotas;
  private final List<LiderGinasio> lideres;
  private final List<Insignia> insignias = new ArrayList<>();
  private int indiceRota;
  private FasePartida fase = FasePartida.ROTA;
  private Batalha batalhaAtual;

  private Partida(Jogador jogador, List<Rota> rotas, List<LiderGinasio> lideres) {
    this.jogador = jogador;
    this.rotas = List.copyOf(rotas);
    this.lideres = List.copyOf(lideres);
  }

  public static Partida nova(Jogador jogador, List<Rota> rotas, List<LiderGinasio> lideres) {
    return new Partida(jogador, rotas, lideres);
  }

  public FasePartida getFase() {
    return fase;
  }

  public Rota getRotaAtual() {
    return rotas.get(indiceRota);
  }

  public Jogador getJogador() {
    return jogador;
  }

  public Batalha getBatalhaAtual() {
    return batalhaAtual;
  }

  public List<Insignia> getInsignias() {
    return List.copyOf(insignias);
  }

  public void iniciarOrganizacao() {
    fase = FasePartida.ORGANIZACAO;
  }

  public void iniciarBatalha() {
    fase = FasePartida.BATALHA;
  }

  public void concluirVitoriaDeGinasio() {
    fase = FasePartida.VITORIA_GINASIO;
  }

  public void concluirDerrota() {
    fase = FasePartida.DERROTA;
  }

  public boolean podeContinuar() {
    return fase != FasePartida.DERROTA && fase != FasePartida.VITORIA_FINAL;
  }

  public void validarEstado() {
      
  }
}
