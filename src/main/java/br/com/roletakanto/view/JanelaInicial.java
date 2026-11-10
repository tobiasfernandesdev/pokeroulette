package br.com.roletakanto.view;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Janela temporária para confirmar que o projeto abre em qualquer IDE Java. */
public final class JanelaInicial {
    private final JFrame janela;

    public JanelaInicial() {
        janela = new JFrame("Roleta Kanto");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(640, 420);
        janela.setLocationRelativeTo(null);
        janela.setContentPane(criarPainel());
    }

    private JPanel criarPainel() {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBackground(new Color(24, 52, 74));
        painel.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        JLabel titulo = new JLabel("Roleta Kanto", SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));

        JLabel mensagem = new JLabel("Estrutura inicial pronta para receber o jogo.", SwingConstants.CENTER);
        mensagem.setForeground(new Color(225, 235, 242));
        mensagem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        painel.add(titulo, BorderLayout.CENTER);
        painel.add(mensagem, BorderLayout.SOUTH);
        return painel;
    }

    public void mostrar() {
        janela.setVisible(true);
    }
}
