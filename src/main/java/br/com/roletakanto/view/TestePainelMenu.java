
package br.com.roletakanto.view;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Abre uma janela para testar o vídeo, o layout e os botões do menu. */
public final class TestePainelMenu {

    private TestePainelMenu() {
    }

    public static void main(String[] args) {

        // Os componentes Swing são criados na thread de eventos da interface.
        SwingUtilities.invokeLater(() -> {

            JFrame janela = new JFrame("Teste - Roleta Kanto");

            janela.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
            );

            janela.setContentPane(new PainelMenu());

            // pack() monta a janela com o tamanho preferido do menu.
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
