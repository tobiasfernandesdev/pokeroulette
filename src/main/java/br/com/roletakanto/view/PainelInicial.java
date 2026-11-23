package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import br.com.roletakanto.model.EspeciePokemon;

/** Exibe as opções de inicial sobre o vídeo e devolve o ID escolhido. */
public final class PainelInicial extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JPanel botoesIniciais = new JPanel(new GridLayout(1, 0, 16, 0));
    private final JLabel detalhe = new JLabel(" ", SwingConstants.CENTER);
    private ButtonGroup selecao = new ButtonGroup();

    public PainelInicial() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(1152, 648));
        getAccessibleContext().setAccessibleName("Escolha seu Pokémon inicial");

        FundoVideo fundo = new FundoVideo("inicialBgOficial.mp4", false);
        add(fundo, BorderLayout.CENTER);

        JPanel controles = new JPanel(new BorderLayout(0, 16));
        controles.setOpaque(false);
        botoesIniciais.setOpaque(false);
        detalhe.setForeground(new Color(49, 23, 76));
        detalhe.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        controles.add(botoesIniciais, BorderLayout.CENTER);
        controles.add(detalhe, BorderLayout.SOUTH);

        // O título faz parte do MP4. As escolhas ficam no espaço livre abaixo dele.
        fundo.posicionarControles(controles, 0.55);
    }

    public void exibirOpcoes(List<EspeciePokemon> opcoes) {
        botoesIniciais.removeAll();
        selecao = new ButtonGroup();
        detalhe.setText(" ");

        for (EspeciePokemon especie : opcoes) {
            JToggleButton botao = new JToggleButton(especie.getNome());
            botao.setPreferredSize(new Dimension(164, 64));
            botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            botao.setActionCommand(especie.getId());
            botao.addActionListener(evento -> mostrarDetalhe(especie));

            // O grupo mantém um único Pokémon selecionado por vez.
            selecao.add(botao);
            botoesIniciais.add(botao);
        }

        botoesIniciais.revalidate();
        botoesIniciais.repaint();
    }

    public String obterIdSelecionado() {
        return selecao.getSelection() == null ? null : selecao.getSelection().getActionCommand();
    }

    public void mostrarDetalhe(EspeciePokemon especie) {
        detalhe.setText("%s · HP %d · Ataque %d · Defesa %d".formatted(
            especie.getNome(), especie.getHpMaximo(), especie.getAtaque(), especie.getDefesa()
        ));
    }
}
