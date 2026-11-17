
package br.com.roletakanto.view;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestePainelMenu {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame janela = new JFrame("Teste - Roleta Kanto");

            janela.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
            );

            janela.setContentPane(new PainelMenu());

            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
