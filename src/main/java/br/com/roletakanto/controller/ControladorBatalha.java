package br.com.roletakanto.controller;

import br.com.roletakanto.model.AcaoBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.FasePartida;
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
    if (!batalhaAtiva()) {
      throw new IllegalStateException("Inicie uma partida antes de executar um golpe.");
    }
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.golpe(idGolpe));
    verificarFimDeBatalha(resultado);
    return resultado;
  }

  public ResultadoTurno usarItem(String idItem) {
    if (!batalhaAtiva()) {
      throw new IllegalStateException("Inicie uma partida antes de usar um item.");
    }
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.item(idItem));
    verificarFimDeBatalha(resultado);
    return resultado;
  }

  public ResultadoTurno trocarPokemon(String idPokemon) {
    if (!batalhaAtiva()) {
      throw new IllegalStateException("Inicie uma partida antes de trocar de Pokemon.");
    }
    Batalha batalha = partida.getBatalhaAtual();
    ResultadoTurno resultado = batalha.executarAcao(AcaoBatalha.troca(idPokemon));
    verificarFimDeBatalha(resultado);
    return resultado;
  }

  public void desistir() {
    if (!batalhaAtiva()) {
      throw new IllegalStateException("Inicie uma partida antes de desistir da batalha.");
    }
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

  public boolean batalhaAtiva() {
    return partida != null
        && partida.getBatalhaAtual() != null
        && partida.getFase() == FasePartida.BATALHA;
  }
}