package br.com.roletakanto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.junit.Test;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.controller.ResultadoGiro;
import br.com.roletakanto.service.CatalogoJogo;
import br.com.roletakanto.view.PainelRota;
import br.com.roletakanto.view.VisualPokemon;

public final class TesteRoletaTela {

  @Test
  public void giroEstruturadoUsaUmUnicoResultado() {
    ControladorPartida controlador = new ControladorPartida(new CatalogoJogo(), new Random(21L));
    controlador.novaPartida("charmander");

    ResultadoGiro giro = controlador.girarRoletaComResultado();

    assertTrue(giro.sucesso());
    assertNotNull(giro.resultado());
    assertEquals(1, controlador.getPartida().getRotaAtual().getGirosRestantes());
    assertTrue(giro.mensagem().contains("Giros restantes: 1"));
  }

  @Test
  public void painelRenderizaEstadoRealDaPartida() throws Exception {
    ControladorPartida controlador = new ControladorPartida(new CatalogoJogo(), new Random(7L));
    controlador.novaPartida("bulbasaur");

    SwingUtilities.invokeAndWait(() -> {
      PainelRota painel = new PainelRota();
      painel.conectar(controlador);
      painel.setSize(1280, 760);
      organizar(painel);

      BufferedImage imagem = new BufferedImage(1280, 760, BufferedImage.TYPE_INT_ARGB);
      Graphics2D desenho = imagem.createGraphics();
      try {
        painel.paint(desenho);
      } finally {
        desenho.dispose();
      }

      assertNotNull(procurarTexto(painel, "BULBASAUR"));
      assertNotNull(procurarTexto(painel, "BROCK"));
      assertNotNull(procurarTexto(painel, "x2"));
      JButton girar = procurarBotao(painel, "GIRAR");
      assertNotNull(girar);
      assertTrue(girar.isEnabled());
    });

    VisualPokemon visuais = new VisualPokemon();
    assertTrue(visuais.recursoExiste("bulbasaur"));
    assertTrue(visuais.recursoExiste("charmander"));
    assertTrue(visuais.recursoExiste("squirtle"));
  }

  private static void organizar(Container container) {
    container.doLayout();
    for (Component componente : container.getComponents()) {
      if (componente instanceof Container filhos) {
        organizar(filhos);
      }
    }
  }

  private static JLabel procurarTexto(Container raiz, String texto) {
    for (Component componente : raiz.getComponents()) {
      if (componente instanceof JLabel label && texto.equals(label.getText())) {
        return label;
      }
      if (componente instanceof Container filhos) {
        JLabel encontrado = procurarTexto(filhos, texto);
        if (encontrado != null) {
          return encontrado;
        }
      }
    }
    return null;
  }

  private static JButton procurarBotao(Container raiz, String texto) {
    for (Component componente : raiz.getComponents()) {
      if (componente instanceof JButton botao && texto.equals(botao.getText())) {
        return botao;
      }
      if (componente instanceof Container filhos) {
        JButton encontrado = procurarBotao(filhos, texto);
        if (encontrado != null) {
          return encontrado;
        }
      }
    }
    return null;
  }
}
