package br.com.roletakanto.view;

import java.awt.GraphicsDevice;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.persistence.SalvamentosJogador;

/** Abre a campanha completa em Swing, com os vídeos nos cartões de entrada. */
public final class TestePainelMenu {
  private TestePainelMenu() {}

  public static void main(String[] args) {
    SwingUtilities.invokeLater(TestePainelMenu::abrirPrevia);
  }

  private static void abrirPrevia() {
    NavegadorTelas navegador = new NavegadorTelas();
    AudioJogo audio = new AudioJogo();
    FluxoEntrada entrada = new FluxoEntrada(navegador, new ControladorPartida(),
        new SalvamentosJogador(Path.of("saves")), audio);

    JFrame janela = new JFrame("Roleta Kanto");
    janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    janela.setUndecorated(true);
    janela.setContentPane(navegador);
    janela.getRootPane().registerKeyboardAction(evento -> {
      if ("login".equals(navegador.getTelaAtual())) {
        janela.dispatchEvent(new WindowEvent(janela, WindowEvent.WINDOW_CLOSING));
      } else {
        entrada.voltar();
      }
    }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

    janela.pack();
    janela.setLocationRelativeTo(null);
    GraphicsDevice monitor = janela.getGraphicsConfiguration().getDevice();
    janela.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evento) {
        if (!entrada.campanha().salvarSeJogando()) {
          return;
        }
        if (monitor.getFullScreenWindow() == janela) {
          monitor.setFullScreenWindow(null);
        }
        janela.dispose();
      }

      @Override
      public void windowClosed(WindowEvent evento) {
        audio.close();
        FundoVideo.encerrarJavaFxDaPrevia();
      }
    });
    monitor.setFullScreenWindow(janela);
  }

  static FluxoCampanha registrarTelas(NavegadorTelas navegador, ControladorPartida controlador) {
    return new FluxoEntrada(navegador, controlador, new SalvamentosJogador(Path.of("saves")),
        AudioJogo.SILENCIO).campanha();
  }
}
