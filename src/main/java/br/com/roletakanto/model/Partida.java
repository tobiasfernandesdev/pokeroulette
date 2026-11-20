package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    this.indiceRota = 0;
    this.fase = FasePartida.ROTA;
  }

  public static Partida nova(Jogador jogador, List<Rota> rotas, List<LiderGinasio> lideres) {
    if (jogador == null) {
      throw new IllegalArgumentException("O jogador é obrigatório para iniciar uma partida.");
    }
    if (rotas == null || rotas.isEmpty()) {
      throw new IllegalArgumentException("A lista de rotas não pode estar vazia.");
    }
    if (lideres == null || lideres.isEmpty()) {
      throw new IllegalArgumentException("A lista de líderes não pode estar vazia.");
    }
    if (rotas.size() != lideres.size()) {
      throw new IllegalArgumentException(
          "A quantidade de rotas deve corresponder à quantidade de líderes.");
    }

    Partida partida = new Partida(jogador, rotas, lideres);
    partida.validarEstado();
    return partida;
  }

  public FasePartida getFase() {
    return fase;
  }

  public Rota getRotaAtual() {
    validarEstado();
    return rotas.get(indiceRota);
  }

  public LiderGinasio getLiderAtual() {
    validarEstado();
    return lideres.get(indiceRota);
  }

  public Jogador getJogador() {
    return jogador;
  }

  public Batalha getBatalhaAtual() {
    return batalhaAtual;
  }

  public void definirBatalhaAtual(Batalha batalhaAtual) {
    validarEstado();
    this.batalhaAtual = batalhaAtual;
  }

  public List<Insignia> getInsignias() {
    return List.copyOf(insignias);
  }

  public void iniciarOrganizacao() {
    validarEstado();
    if (!podeContinuar()) {
      throw new IllegalStateException("A partida já foi encerrada.");
    }
    if (fase != FasePartida.ROTA) {
      throw new IllegalStateException(
          "Não é possível iniciar a organização a partir da fase " + fase + ".");
    }

    fase = FasePartida.ORGANIZACAO;
  }

  public void voltarParaRota() {
    validarEstado();
    if (!podeContinuar()) {
      throw new IllegalStateException("A partida já foi encerrada.");
    }
    if (fase != FasePartida.ORGANIZACAO) {
      throw new IllegalStateException(
          "Só é possível voltar para a rota a partir da fase de organização.");
    }
    if (!getRotaAtual().temGiroDisponivel()) {
      throw new IllegalStateException("Não há giros disponíveis na rota atual.");
    }

    fase = FasePartida.ROTA;
  }

  public void iniciarBatalha() {
    validarEstado();
    if (!podeContinuar()) {
      throw new IllegalStateException("A partida já foi encerrada.");
    }
    if (fase != FasePartida.ORGANIZACAO && fase != FasePartida.ROTA) {
      throw new IllegalStateException(
          "Não é possível iniciar a batalha a partir da fase " + fase + ".");
    }
    if (!jogador.possuiPokemonApto()) {
      throw new IllegalStateException(
          "Não é possível iniciar a batalha sem ao menos um Pokémon apto na equipe.");
    }

    fase = FasePartida.BATALHA;
  }

  public void concluirVitoriaDeGinasio() {
    validarEstado();
    if (fase != FasePartida.BATALHA) {
      throw new IllegalStateException(
          "Não é possível concluir vitória de ginásio fora da fase de batalha.");
    }

    LiderGinasio liderVencido =
        (batalhaAtual != null && batalhaAtual.getLider() != null)
            ? batalhaAtual.getLider()
            : lideres.get(indiceRota);

    Insignia insignia = liderVencido.getInsignia();
    if (insignia != null && !insignias.contains(insignia)) {
      insignias.add(insignia);
    }

    jogador.restaurarEquipeNoCentro();
    batalhaAtual = null;

    if (indiceRota + 1 < rotas.size()) {
      indiceRota++;
      fase = FasePartida.ROTA;
    } else {
      fase = FasePartida.VITORIA_FINAL;
    }
  }

  public void concluirDerrota() {
    validarEstado();
    if (!podeContinuar()) {
      throw new IllegalStateException("A partida já foi encerrada.");
    }

    batalhaAtual = null;
    fase = FasePartida.DERROTA;
  }

  public boolean podeContinuar() {
    return fase != FasePartida.DERROTA && fase != FasePartida.VITORIA_FINAL;
  }

  public void validarEstado() {
    if (jogador == null) {
      throw new IllegalStateException("O jogador da partida não pode ser nulo.");
    }
    if (rotas == null || rotas.isEmpty()) {
      throw new IllegalStateException("A partida deve possuir ao menos uma rota.");
    }
    if (lideres == null || lideres.size() != rotas.size()) {
      throw new IllegalStateException(
          "A quantidade de líderes deve corresponder à quantidade de rotas.");
    }
    if (indiceRota < 0 || indiceRota >= rotas.size()) {
      throw new IllegalStateException("Índice da rota atual inválido: " + indiceRota);
    }
    if (fase == null) {
      throw new IllegalStateException("A fase da partida não pode ser nula.");
    }
    if (insignias == null) {
      throw new IllegalStateException("A lista de insígnias não pode ser nula.");
    }
  }
}