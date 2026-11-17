package br.com.roletakanto.model;

import br.com.roletakanto.service.*;
import java.io.Serializable;
import java.util.List;

/** Motor de combate; as regras detalhadas serão implementadas neste ponto. */
public final class Batalha implements Serializable {
  private final Jogador jogador;
  private final LiderGinasio lider;
  private final CalculadoraDano calculadora;
  private final SeletorAcaoIA ia;

  public Batalha(
      Jogador jogador, LiderGinasio lider, CalculadoraDano calculadora, SeletorAcaoIA ia) {
    this.jogador = jogador;
    this.lider = lider;
    this.calculadora = calculadora;
    this.ia = ia;
  }

  public ResultadoTurno executarAcao(AcaoBatalha acao) {
    return new ResultadoTurno(List.of("TODO: resolver ação"), ResultadoBatalha.EM_ANDAMENTO, null);
  }

  public boolean podeTrocar(Pokemon pokemon) {
    return pokemon != null && pokemon.estaApto();
  }

  public void trocarAtivo(Pokemon pokemon) {
    /* TODO */
  }

  public ResultadoBatalha verificarResultado() {
    return ResultadoBatalha.EM_ANDAMENTO;
  }

  public Pokemon obterAtivoJogador() {
    return jogador.getEquipe().listar().stream().filter(Pokemon::estaApto).findFirst().orElse(null);
  }

  public LiderGinasio getLider() {
    return lider;
  }
}
