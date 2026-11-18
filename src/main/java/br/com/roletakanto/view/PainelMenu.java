
package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Organiza o fundo em vídeo e os botões Swing do menu principal. */
public final class PainelMenu extends JPanel {
    private static final long serialVersionUID = 1L;

    private final FundoVideoMenu fundo = new FundoVideoMenu();

    private JButton botaoContinuar;

    public PainelMenu() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1152, 648));
        getAccessibleContext().setAccessibleName("Menu principal de Roleta Kanto");
        // O centro do BorderLayout dá ao vídeo toda a área do menu.
        add(fundo, BorderLayout.CENTER);
        construirComponentes();
    }

    public void construirComponentes() {
        fundo.removeAll();

        // FlowLayout centraliza os botões em uma fileira, como flex com gap no CSS.
        JPanel painelBotoes = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 18, 0)
        );
        painelBotoes.setOpaque(false);

        // A margem inferior deixa uma faixa do vídeo visível abaixo dos botões.
        painelBotoes.setBorder(
            BorderFactory.createEmptyBorder(
                0, 15, 88, 15
            )
        );

        JButton botaoNovaPartida = criarBotao("NOVA PARTIDA");
        botaoContinuar = criarBotao("CONTINUAR");
        JButton botaoSair = criarBotao("SAIR");

        painelBotoes.add(botaoNovaPartida);
        painelBotoes.add(botaoContinuar);
        painelBotoes.add(botaoSair);

        // A fileira transparente é desenhada por cima do vídeo, no rodapé.
        fundo.add(painelBotoes, BorderLayout.SOUTH);

        // Continuar é habilitado quando o jogo informa que há um salvamento.
        atualizarDisponibilidade(false);

        botaoSair.addActionListener(
            e -> System.exit(0)
        );

        revalidate();
        repaint();
    }

    /** Centraliza a aparência provisória para facilitar a troca pela arte final. */
    private JButton criarBotao(String texto) {
        JButton botao = new JButton(texto);

        botao.setPreferredSize(
            new Dimension(164, 76)
        );

        botao.setFont(
            new Font("Monospaced", Font.BOLD, 14)
        );

        botao.setForeground(new Color(94, 51, 31));
        botao.setBackground(new Color(231, 183, 126));

        botao.setFocusPainted(false);
        botao.setOpaque(true);

        botao.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        // A borda composta combina o contorno escuro com a linha clara interna.
        botao.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(91, 53, 33), 5
                ),
                BorderFactory.createLineBorder(
                    new Color(255, 216, 154), 3
                )
            )
        );

        return botao;
    }

    public void atualizarDisponibilidade(boolean salvamento) {

        if (botaoContinuar != null) {
            botaoContinuar.setEnabled(salvamento);
        }
    }

    public void limparMensagens() {
        // Reservado para limpar mensagens quando o menu tiver uma área de avisos.
    }
}
