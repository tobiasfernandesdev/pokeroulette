
package br.com.roletakanto.view;

import java.awt.GraphicsDevice;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import br.com.roletakanto.model.EspeciePokemon;
import br.com.roletakanto.model.TipoPokemon;

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
        // Dados da prévia para testar a interface sem iniciar a lógica da partida.
        inicial.exibirOpcoes(List.of(
            new EspeciePokemon("bulbasaur", "Bulbasaur", TipoPokemon.PLANTA, 70, 17, 13, 12, List.of()),
            new EspeciePokemon("charmander", "Charmander", TipoPokemon.FOGO, 62, 20, 10, 17, List.of()),
            new EspeciePokemon("squirtle", "Squirtle", TipoPokemon.AGUA, 72, 15, 17, 10, List.of())
        ));
        navegador.registrar("menu", menu);
        navegador.registrar("inicial", inicial);

        JFrame janela = new JFrame("Teste - Roleta Kanto");
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        janela.setUndecorated(true);
        janela.setContentPane(navegador);
        janela.getRootPane().registerKeyboardAction(evento -> {
            if ("inicial".equals(navegador.getTelaAtual())) {
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
