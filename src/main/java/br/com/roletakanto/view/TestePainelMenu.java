
package br.com.roletakanto.view;

import java.awt.GraphicsDevice;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.service.CatalogoJogo;

/** Prévia independente do menu e da escolha do Pokémon inicial. */
public final class TestePainelMenu {

    private TestePainelMenu() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestePainelMenu::abrirPrevia);
    }

    private static void abrirPrevia() {
        NavegadorTelas navegador = new NavegadorTelas();
        PainelMenu menu = new PainelMenu(() -> navegador.mostrar("inicial"));
        PainelInicial inicial = new PainelInicial();
        PainelRota rota = new PainelRota();
        ControladorPartida controlador = new ControladorPartida();
        CatalogoJogo catalogo = new CatalogoJogo();
        var especies = catalogo.carregarEspecies();
        inicial.exibirOpcoes(java.util.List.of(
            especies.get("bulbasaur"),
            especies.get("charmander"),
            especies.get("squirtle")));
        inicial.definirAcaoConfirmar(idInicial -> {
            controlador.novaPartida(idInicial);
            rota.conectar(controlador);
            navegador.mostrar("rota");
        });
        navegador.registrar("menu", menu);
        navegador.registrar("inicial", inicial);
        navegador.registrar("rota", rota);

        JFrame janela = new JFrame("Teste - Roleta Kanto");
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        janela.setUndecorated(true);
        janela.setContentPane(navegador);
        janela.getRootPane().registerKeyboardAction(evento -> {
            if ("rota".equals(navegador.getTelaAtual())) {
                navegador.mostrar("inicial");
            } else if ("inicial".equals(navegador.getTelaAtual())) {
                navegador.mostrar("menu");
            } else {
                janela.dispatchEvent(new WindowEvent(janela, WindowEvent.WINDOW_CLOSING));
            }
        }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // A tela cheia usa o tamanho real do monitor, inclusive em 4K.
        janela.pack();
        janela.setLocationRelativeTo(null);
        GraphicsDevice monitor = janela.getGraphicsConfiguration().getDevice();
        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                if (monitor.getFullScreenWindow() == janela) {
                    monitor.setFullScreenWindow(null);
                }
            }

            @Override
            public void windowClosed(WindowEvent evento) {
                FundoVideo.encerrarJavaFxDaPrevia();
            }
        });
        monitor.setFullScreenWindow(janela);
    }
}
