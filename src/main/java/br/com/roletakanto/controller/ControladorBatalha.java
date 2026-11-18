package br.com.roletakanto.controller;

import br.com.roletakanto.model.AcaoBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.view.PainelBatalha;

public final class ControladorBatalha {

  private Partida partida;
  private PainelBatalha painelBatalha;

  public ControladorBatalha() {
  }

  public ControladorBatalha(Partida partida) {
    this.partida = partida;
  }

  public void definirPartida(Partida partida) {
    this.partida = partida;
  }

  public void definirPainel(PainelBatalha painelBatalha) {
    this.painelBatalha = painelBatalha;
  }

  public ResultadoTurno executarGolpe(String idGolpe) {
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.golpe(idGolpe));
    verificarFimDeBatalha(resultado);
    return resultado;
  }

  public ResultadoTurno usarItem(String idItem) {
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.item(idItem));
    verificarFimDeBatalha(resultado);
    return resultado;
  }

  public ResultadoTurno trocarPokemon(String idPokemon) {
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.troca(idPokemon));
    verificarFimDeBatalha(resultado);
    return resultado;
  }

  public void desistir() {
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.desistir());
    verificarFimDeBatalha(resultado);
  }

  public void atualizarPainel() {
    if (painelBatalha != null && partida != null && partida.getBatalhaAtual() != null) {
      painelBatalha.apresentar(partida.getBatalhaAtual());
    }
  }

  private void verificarFimDeBatalha(ResultadoTurno resultado) {
    if (resultado.resultado() == ResultadoBatalha.VITORIA) {
      partida.concluirVitoriaDeGinasio();
    } else if (resultado.resultado() == ResultadoBatalha.DERROTA) {
      partida.concluirDerrota();
    }
    atualizarPainel();
  }
}