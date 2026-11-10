package br.com.roletakanto.app;

import br.com.roletakanto.view.JanelaInicial;

import javax.swing.SwingUtilities;

/** Ponto de entrada portátil do aplicativo Roleta Kanto. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JanelaInicial().mostrar());
    }
}
