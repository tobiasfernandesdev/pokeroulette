package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.List;

import br.com.roletakanto.service.CalculadoraDano;
import br.com.roletakanto.service.SeletorAcaoIA;

/** Motor de combate; as regras detalhadas serã o implementadas neste ponto. */
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
