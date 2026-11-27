package br.com.roletakanto.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.Random;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.service.CatalogoJogo;

final class TesteFluxoCampanhaSwing {
  @Test
  void percorreQuatroGinasiosComResultadosEReiniciaPeloMenu() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      NavegadorTelas navegador = new NavegadorTelas();
      navegador.registrar("menu", new JPanel());
      ControladorPartida controlador = new ControladorPartida(new CatalogoJogo(), new Random(159L));
      FluxoCampanha fluxo = new FluxoCampanha(navegador, controlador);
      fluxo.iniciar("charmander");
      var partida = controlador.getPartida();

      clicar(navegador, "EQUIPE");
      assertEquals("equipe", navegador.getTelaAtual());
      assertEquals(FasePartida.ROTA, partida.getFase());
      clicar(navegador, "VOLTAR À ROTA");

      String[] lideres = {"brock", "misty", "lt_surge", "erika"};
      for (int i = 0; i < lideres.length; i++) {
        assertEquals("rota", navegador.getTelaAtual());
        assertEquals(lideres[i], partida.getLiderAtual().getId());
        esgotarGiros(navegador, controlador);

        clicar(navegador, "AVANÇAR");
        assertEquals("equipe", navegador.getTelaAtual());
        assertEquals(FasePartida.ORGANIZACAO, partida.getFase());
        clicar(navegador, "DESAFIAR LÍDER");
        assertEquals("batalha", navegador.getTelaAtual());
        assertEquals(FasePartida.BATALHA, partida.getFase());

        // Encurta os combates do teste. O golpe e a vitória ainda passam pelo controlador.
        for (int turno = 0; turno < 12 && partida.getFase() == FasePartida.BATALHA; turno++) {
          var combate = partida.getBatalhaAtual();
          var oponente = combate.getAtivoLider();
          oponente.receberDano(oponente.getHpAtual() - 1);
          var ativo = combate.obterAtivoJogador();
          ativo.restaurarParaCentro();
          clicar(navegador, texto -> texto.startsWith("Investida ·"));
        }

        assertEquals("resultado", navegador.getTelaAtual());
        assertEquals(i + 1, partida.getInsignias().size());
        if (i < lideres.length - 1) {
          assertEquals(FasePartida.ROTA, partida.getFase());
          clicar(navegador, texto -> texto.startsWith("CONTINUAR ·"));
        }
      }

      assertEquals(FasePartida.VITORIA_FINAL, partida.getFase());
      clicar(navegador, "VOLTAR AO MENU");
      assertEquals("menu", navegador.getTelaAtual());

      fluxo.iniciar("charmander");
      var nova = controlador.getPartida();
      // Mantém só o inicial para preparar uma derrota real no primeiro combate.
      esgotarGiros(navegador, controlador);
      for (var pokemon : nova.getJogador().getEquipe().listar().stream().skip(1).toList()) {
        controlador.moverParaReserva(pokemon.getId());
      }
      clicar(navegador, "AVANÇAR");
      clicar(navegador, "DESAFIAR LÍDER");
      var ativo = nova.getBatalhaAtual().obterAtivoJogador();
      ativo.receberDano(ativo.getHpAtual() - 1);
      clicar(navegador, texto -> texto.startsWith("Investida ·"));
      assertEquals(FasePartida.DERROTA, nova.getFase());
      assertEquals("resultado", navegador.getTelaAtual());
      clicar(navegador, "VOLTAR AO MENU");
      assertEquals("menu", navegador.getTelaAtual());
    });
  }

  @Test
  void impedeSegundoGiroDuranteAnimacaoEExibeRecompensaAoParar() throws Exception {
    NavegadorTelas navegador = new NavegadorTelas();
    ControladorPartida controlador = new ControladorPartida(new CatalogoJogo(), new Random(159L));
    PainelRota[] painel = new PainelRota[1];
    SwingUtilities.invokeAndWait(() -> {
      new FluxoCampanha(navegador, controlador).iniciar("charmander");
      painel[0] = (PainelRota) navegador.getComponent(0);
      clicar(navegador, "GIRAR");
      JButton girar = procurar(navegador, "GIRAR"::equals);
      assertFalse(girar.isEnabled());
      girar.doClick(0);
      assertEquals(1, controlador.getPartida().getRotaAtual().getGirosRestantes());
      assertTrue(painel[0].estaGirando());
    });

    // A animação roda na thread Swing. Esperamos sua conclusão fora dela.
    JButton[] girar = new JButton[1];
    long limite = System.nanoTime() + 8_000_000_000L;
    while (girar[0] == null && System.nanoTime() < limite) {
      Thread.sleep(35);
      SwingUtilities.invokeAndWait(() -> {
        if (!painel[0].estaGirando()) {
          girar[0] = procurar(navegador, "GIRAR"::equals);
        }
      });
    }
    assertTrue(girar[0] != null && girar[0].isEnabled());
    SwingUtilities.invokeAndWait(() -> {
      assertEquals(1, controlador.getPartida().getRotaAtual().getGirosRestantes());
      assertTrue(contemTexto(navegador, "✦ SUPER POÇÃO ✦"));
    });
  }

  private static void esgotarGiros(NavegadorTelas navegador, ControladorPartida controlador) {
    var partida = controlador.getPartida();
    while (partida.getRotaAtual().temGiroDisponivel()) {
      assertTrue(controlador.girarRoletaComResultado().sucesso());
    }
    // Atualiza a rota pelo mesmo caminho que o jogador usa ao fechar a equipe.
    clicar(navegador, "EQUIPE");
    clicar(navegador, "VOLTAR À ROTA");
  }

  private static void clicar(Container raiz, String texto) {
    clicar(raiz, texto::equals);
  }

  private static void clicar(Container raiz, Predicate<String> texto) {
    JButton botao = procurar(raiz, texto);
    assertTrue(botao != null && botao.isEnabled(), "O botão precisa estar visível e habilitado");
    botao.doClick(0);
  }

  private static JButton procurar(Container raiz, Predicate<String> texto) {
    for (Component filho : raiz.getComponents()) {
      if (!filho.isVisible()) {
        continue;
      }
      if (filho instanceof JButton botao && texto.test(botao.getText())) {
        return botao;
      }
      if (filho instanceof Container container) {
        JButton resultado = procurar(container, texto);
        if (resultado != null) {
          return resultado;
        }
      }
    }
    return null;
  }

  private static boolean contemTexto(Container raiz, String texto) {
    for (Component filho : raiz.getComponents()) {
      if (filho instanceof javax.swing.JLabel label && texto.equals(label.getText())) {
        return true;
      }
      if (filho instanceof Container container && contemTexto(container, texto)) {
        return true;
      }
    }
    return false;
  }
}
