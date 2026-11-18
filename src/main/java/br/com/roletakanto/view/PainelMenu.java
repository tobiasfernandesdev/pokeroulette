
package br.com.roletakanto.view;

// IMPORTACOES AWT
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class PainelMenu extends JPanel {

    // COMPONENTES DO MENU
    private JLabel logo;

    private JButton botaoNovaPartida;
    private JButton botaoContinuar;
    private JButton botaoSair;

    // CONSTRUTOR
    public PainelMenu() {
        construirComponentes();
    }

    // CONSTRUCAO DA INTERFACE
    public void construirComponentes() {

        removeAll();

        // Configuracoes gerais do menu
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(960, 540));

        // ==================================
        // LOGO DO JOGO
        // ==================================

        // Painel que vai guardar a logo
        JPanel painelLogo = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 0, 0)
        );

        // Deixa o fundo do painel transparente
        painelLogo.setOpaque(false);

        // Espacamento superior
        painelLogo.setBorder(
            BorderFactory.createEmptyBorder(45, 0, 0, 0)
        );

        // Logo provisoria em texto
        logo = new JLabel(
            "ROLETA KANTO",
            SwingConstants.CENTER
        );

        // Tamanho da logo
        logo.setPreferredSize(new Dimension(540, 130));

        // Fonte
        logo.setFont(
            new Font("Monospaced", Font.BOLD, 46)
        );

        // Cores da logo provisoria
        logo.setForeground(new Color(83, 46, 32));
        logo.setBackground(new Color(229, 172, 98));
        logo.setOpaque(true);

        // Borda da logo
        logo.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(100, 57, 36), 6
                ),
                BorderFactory.createEmptyBorder(
                    12, 20, 12, 20
                )
            )
        );

        // Adiciona a logo ao painel
        painelLogo.add(logo);

        // Posiciona no topo do menu
        add(painelLogo, BorderLayout.NORTH);

        // ==================================
        // BOTOES DO MENU
        // ==================================

        // Painel que organiza os botoes
        JPanel painelBotoes = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 18, 0)
        );

        painelBotoes.setOpaque(false);

        // Espacamento inferior e lateral
        painelBotoes.setBorder(
            BorderFactory.createEmptyBorder(
                0, 15, 55, 15
            )
        );

        // Cria os tres botoes
        botaoNovaPartida = criarBotao("NOVA PARTIDA");
        botaoContinuar = criarBotao("CONTINUAR");
        botaoSair = criarBotao("SAIR");

        // Adiciona os botoes ao painel
        painelBotoes.add(botaoNovaPartida);
        painelBotoes.add(botaoContinuar);
        painelBotoes.add(botaoSair);

        // Posiciona os botoes na parte inferior
        add(painelBotoes, BorderLayout.SOUTH);

        // ==================================
        // CONFIGURACOES DOS BOTOES
        // ==================================

        // Continuar fica desabilitado inicialmente
        atualizarDisponibilidade(false);

        // Botao Sair fecha o programa
        botaoSair.addActionListener(
            e -> System.exit(0)
        );

        revalidate();
        repaint();
    }

    // ==================================
    // APARENCIA DOS BOTOES
    // ==================================

    private JButton criarBotao(String texto) {

        JButton botao = new JButton(texto);

        // Tamanho dos botoes
        botao.setPreferredSize(
            new Dimension(164, 76)
        );

        // Fonte dos botoes
        botao.setFont(
            new Font("Monospaced", Font.BOLD, 14)
        );

        // Cores
        botao.setForeground(new Color(94, 51, 31));
        botao.setBackground(new Color(231, 183, 126));

        // Configuracoes visuais
        botao.setFocusPainted(false);
        botao.setOpaque(true);

        // Cursor de mao ao passar sobre o botao
        botao.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        // Bordas dos botoes
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

    // ==================================
    // BACKGROUND PROVISORIO
    // ==================================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Gradiente azul de cima para baixo
        GradientPaint fundo = new GradientPaint(
            0, 0, new Color(18, 62, 108),
            0, getHeight(), new Color(54, 141, 169)
        );

        g2.setPaint(fundo);

        // Preenche todo o fundo do menu
        g2.fillRect(
            0, 0, getWidth(), getHeight()
        );

        g2.dispose();
    }

    // ==================================
    // DISPONIBILIDADE DE SALVAMENTO
    // ==================================

    public void atualizarDisponibilidade(boolean salvamento) {

        if (botaoContinuar != null) {
            botaoContinuar.setEnabled(salvamento);
        }
    }

    // ==================================
    // LIMPAR MENSAGENS
    // ==================================

    public void limparMensagens() {

        // Sera implementado futuramente
    }
}
