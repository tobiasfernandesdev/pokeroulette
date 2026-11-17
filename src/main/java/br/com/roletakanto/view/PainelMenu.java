package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class PainelMenu extends JPanel {

    private JButton botaoNovaPartida;
    private JButton botaoContinuar;
    private JButton botaoSair;

    public PainelMenu() {
        construirComponentes();
    }

    public void construirComponentes() {
        removeAll();

        // Configuracao do painel principal
        setLayout(new BorderLayout());
        setBackground(new Color(16, 27, 43));
        setPreferredSize(new Dimension(800, 500));

        // Titulo do jogo
        JLabel titulo = new JLabel("ROLETA KANTO");

        titulo.setFont(new Font("Monospaced", Font.BOLD, 40));
        titulo.setForeground(new Color(245, 201, 76));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        add(titulo, BorderLayout.NORTH);

        // Painel que organiza os botoes
        JPanel painelBotoes = new JPanel();

        painelBotoes.setLayout(new GridLayout(3, 1, 0, 15));
        painelBotoes.setBackground(new Color(16, 27, 43));

        // Criacao dos botoes
        botaoNovaPartida = criarBotao("NOVA PARTIDA");
        botaoContinuar = criarBotao("CONTINUAR");
        botaoSair = criarBotao("SAIR");

        // Adicionando os botoes ao painel
        painelBotoes.add(botaoNovaPartida);
        painelBotoes.add(botaoContinuar);
        painelBotoes.add(botaoSair);

        // Centralizando os botoes
        JPanel centro = new JPanel(new BorderLayout());

        centro.setOpaque(false);
        centro.setBorder(
            BorderFactory.createEmptyBorder(90, 220, 90, 220)
        );

        centro.add(painelBotoes, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);

        // Desabilita continuar quando nao existe salvamento
        atualizarDisponibilidade(false);

        // Funcao provisoria do botao sair
        botaoSair.addActionListener(e -> System.exit(0));
    }

    // Metodo para padronizar a aparencia dos botoes
    private JButton criarBotao(String texto) {

        JButton botao = new JButton(texto);

        botao.setFont(new Font("Monospaced", Font.BOLD, 18));
        botao.setForeground(Color.WHITE);
        botao.setBackground(new Color(38, 59, 83));
        botao.setFocusPainted(false);
        botao.setOpaque(true);

        return botao;
    }

    // Habilita ou desabilita o botao continuar
    public void atualizarDisponibilidade(boolean salvamento) {

        if (botaoContinuar != null) {
            botaoContinuar.setEnabled(salvamento);
        }
    }

    // Reservado para limpar mensagens futuramente
    public void limparMensagens() {

    }
}
