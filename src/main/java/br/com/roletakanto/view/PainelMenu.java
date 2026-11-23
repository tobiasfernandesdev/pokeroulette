
package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Organiza o fundo em vídeo e os botões Swing do menu principal. */
public final class PainelMenu extends JPanel {
    private static final long serialVersionUID = 1L;

    private final FundoVideo fundo = new FundoVideo("menuBgOficial.mp4", true);
    private final transient Runnable aoIniciar;

    private JButton botaoContinuar;

    public PainelMenu() {
        this(() -> {});
    }

    PainelMenu(Runnable aoIniciar) {
        this.aoIniciar = aoIniciar;
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

        JButton botaoNovaPartida = criarBotao("NOVA PARTIDA");
        botaoContinuar = criarBotao("CONTINUAR");
        JButton botaoSair = criarBotao("SAIR");

        painelBotoes.add(botaoNovaPartida);
        painelBotoes.add(botaoContinuar);
        painelBotoes.add(botaoSair);

        // A fileira fica abaixo do título já desenhado no vídeo.
        fundo.posicionarControles(painelBotoes, 0.74);

        // Continuar é habilitado quando o jogo informa que há um salvamento.
        atualizarDisponibilidade(false);
        botaoNovaPartida.addActionListener(evento -> aoIniciar.run());

        botaoSair.addActionListener(evento -> {
            // Envia o mesmo pedido de fechamento que o botão da própria janela.
            Window janela = SwingUtilities.getWindowAncestor(this);
            if (janela != null) {
                janela.dispatchEvent(new WindowEvent(janela, WindowEvent.WINDOW_CLOSING));
            }
        });

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
