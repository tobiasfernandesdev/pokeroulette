package br.com.roletakanto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.service.CatalogoJogo;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TesteFluxoBatalha {

  private CatalogoJogo catalogo;
  private String idInicial;
  private ControladorPartida controladorPartida;
  private ControladorBatalha controladorBatalha;

  @BeforeEach
  void preparar() {
    catalogo = new CatalogoJogo();
    idInicial = catalogo.carregarEspecies().keySet().iterator().next();
    controladorPartida = new ControladorPartida(catalogo, new Random(42L));
    controladorBatalha = new ControladorBatalha();
  }

  @Test
  void deveBloquearExecucaoDeGolpeSemBatalhaAtiva() {
    assertThrows(
        IllegalStateException.class,
        () -> controladorBatalha.executarGolpe("golpe-1"),
        "Deveria falhar sem partida ou batalha ativa.");
  }

  @Test
  void deveIniciarPartidaComDoisPocoesEConcluirBatalha() {
    controladorPartida.novaPartida(idInicial);
    controladorBatalha.definirPartida(controladorPartida.getPartida());

    assertEquals(
        FasePartida.ROTA,
        controladorPartida.getPartida().getFase(),
        "Fase inicial deveria ser ROTA.");

    PilhaItem pilhaPocoes =
        controladorPartida.getPartida().getJogador().getMochila().obterPilhaPorId(CatalogoJogo.ID_POCAO);
    int qtdPocoes = pilhaPocoes == null ? 0 : pilhaPocoes.getQuantidade();
    assertEquals(
        2,
        qtdPocoes,
        "Mochila deveria iniciar com 2 poções, mas encontrou: " + qtdPocoes);

    controladorPartida.confirmarBatalha();
    assertTrue(
        controladorBatalha.batalhaAtiva(),
        "Batalha deveria estar ativa após confirmarBatalha().");

    ResultadoTurno ultimoResultado = null;
    int turnos = 0;
    int limiteSeguranca = 100;

    while ((ultimoResultado == null || ultimoResultado.resultado() == ResultadoBatalha.EM_ANDAMENTO)
        && turnos < limiteSeguranca) {
      Pokemon ativoJogador = controladorPartida.getPartida().getBatalhaAtual().obterAtivoJogador();
      assertNotNull(ativoJogador, "Deveria haver um Pokémon ativo apto durante a batalha.");
      assertFalse(
          ativoJogador.getEspecie().getGolpes().isEmpty(),
          "Pokémon ativo do jogador sem golpes disponíveis.");

      String idGolpe = ativoJogador.getEspecie().getGolpes().get(0).getId();
      ultimoResultado = controladorBatalha.executarGolpe(idGolpe);
      assertNotNull(ultimoResultado);
      turnos++;
    }

    assertNotNull(ultimoResultado);
    assertTrue(
        ultimoResultado.resultado() == ResultadoBatalha.VITORIA
            || ultimoResultado.resultado() == ResultadoBatalha.DERROTA,
        "A batalha deveria encerrar com VITÓRIA ou DERROTA.");
    assertTrue(
        controladorPartida.getPartida().getFase() == FasePartida.ROTA
            || controladorPartida.getPartida().getFase() == FasePartida.DERROTA);
  }
}