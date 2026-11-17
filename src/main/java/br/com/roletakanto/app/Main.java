package br.com.roletakanto.app;

import javax.swing.SwingUtilities;

import br.com.roletakanto.view.JanelaInicial;

/** Ponto de entrada portátil do aplicativo Roleta Kanto. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JanelaInicial().mostrar());
        System.out.println("Ola Brasil");
    }
}
