package br.com.roletakanto;

import org.junit.Test;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.service.CatalogoJogo;

public final class TesteFluxoBatalha {

  // O Maven chama o mesmo cenário que pode ser executado pela classe diretamente.
  @Test
  public void verificarFluxoBatalha() {
    main(new String[0]);
  }

  public static void main(String[] args) {
    CatalogoJogo catalogo = new CatalogoJogo();
    String idInicial = catalogo.carregarEspecies().keySet().iterator().next();

    ControladorPartida controladorPartida = new ControladorPartida(catalogo, new java.util.Random(42L));
    ControladorBatalha controladorBatalha = new ControladorBatalha();

    // 1. Verifica bloqueio antes de iniciar batalha
    try {
      controladorBatalha.executarGolpe("golpe-1");
      throw new AssertionError("Deveria falhar sem partida ou batalha ativa.");
    } catch (IllegalStateException expected) {
      System.out.println("[OK] Bloqueio antes da batalha: " + expected.getMessage());
    }

    // 2. Inicia partida e confere estado inicial + 2 pocoes na mochila
    controladorPartida.novaPartida(idInicial);
    controladorBatalha.definirPartida(controladorPartida.getPartida());

    if (controladorPartida.getPartida().getFase() != FasePartida.ROTA) {
      throw new AssertionError("Fase inicial deveria ser ROTA.");
    }

    PilhaItem pilhaPocoes = controladorPartida.getPartida()
        .getJogador()
        .getMochila()
        .obterPilhaPorId(CatalogoJogo.ID_POCAO);
    int qtdPocoes = pilhaPocoes == null ? 0 : pilhaPocoes.getQuantidade();
    if (qtdPocoes != 2) {
      throw new AssertionError("Mochila deveria iniciar com 2 pocoes, mas encontrou: " + qtdPocoes);
    }
    System.out.println("[OK] Partida iniciada em ROTA com " + qtdPocoes + " pocoes na mochila.");

    // 3. Confirma batalha e executa turnos ate o encerramento
    controladorPartida.confirmarBatalha();
    if (!controladorBatalha.batalhaAtiva()) {
      throw new AssertionError("Batalha deveria estar ativa apos confirmarBatalha().");
    }
    System.out.println("[OK] Batalha confirmada e ativa contra "
        + controladorPartida.getPartida().getBatalhaAtual().getLider().getNome() + ".");

    ResultadoTurno ultimoResultado = null;
    int turno = 1;

    while (ultimoResultado == null || ultimoResultado.resultado() == ResultadoBatalha.EM_ANDAMENTO) {
      Pokemon ativoJogador = controladorPartida.getPartida().getBatalhaAtual().obterAtivoJogador();
      if (ativoJogador == null || ativoJogador.getEspecie().getGolpes().isEmpty()) {
        throw new AssertionError("Pokemon ativo do jogador sem golpes disponiveis.");
      }
      String idGolpe = ativoJogador.getEspecie().getGolpes().get(0).getId();

      System.out.println("--- Turno " + turno + " ---");
      ultimoResultado = controladorBatalha.executarGolpe(idGolpe);
      for (String mensagem : ultimoResultado.mensagens()) {
        System.out.println("  " + mensagem);
      }
      turno++;
    }

    System.out.println("[FIM] Resultado da batalha: " + ultimoResultado.resultado());
    System.out.println("[FIM] Fase atual da partida: " + controladorPartida.getPartida().getFase());
  }
}
