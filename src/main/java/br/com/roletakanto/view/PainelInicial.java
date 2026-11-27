package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import br.com.roletakanto.model.EspeciePokemon;

/** Exibe as opções de inicial sobre o vídeo e devolve o ID escolhido. */
public final class PainelInicial extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JPanel botoesIniciais = new JPanel(new GridLayout(1, 0, 16, 0));
    private final JLabel detalhe = EstiloJogo.faixa("Selecione seu inicial.", 12);
    private final JLabel candidato = EstiloJogo.faixa("ESCOLHA SEU INICIAL", 15);
    private final JButton confirmar = new JButton("ESCOLHER");
    private final JButton voltar = new JButton("VOLTAR");
    private final VisualPokemon visuais = new VisualPokemon();
    private AudioJogo audio = AudioJogo.SILENCIO;
    private Runnable aoVoltar = () -> {};
    private ButtonGroup selecao = new ButtonGroup();
    private transient Consumer<String> aoConfirmar = id -> {};

    public PainelInicial() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(1152, 648));
        getAccessibleContext().setAccessibleName("Escolha seu Pokémon inicial");

        FundoVideo fundo = new FundoVideo("inicialBgOficial.mp4", false);
        add(fundo, BorderLayout.CENTER);

        JPanel controles = new JPanel(new BorderLayout(0, 16));
        controles.setOpaque(false);
        botoesIniciais.setOpaque(false);
        controles.add(candidato, BorderLayout.NORTH);
        controles.add(botoesIniciais, BorderLayout.CENTER);
        JPanel rodape = new JPanel(new BorderLayout(12, 0));
        rodape.setOpaque(false);
        rodape.add(detalhe, BorderLayout.CENTER);
        EstiloJogo.personalizarEntrada(confirmar, true, 150, 48);
        EstiloJogo.personalizarEntrada(voltar, false, 120, 48);
        voltar.addActionListener(evento -> {
            audio.efeito(AudioJogo.Efeito.VOLTAR);
            aoVoltar.run();
        });
        rodape.add(voltar, BorderLayout.WEST);
        confirmar.setEnabled(false);
        confirmar.addActionListener(evento -> {
            String selecionado = obterIdSelecionado();
            if (selecionado != null) {
                audio.efeito(AudioJogo.Efeito.CONFIRMA);
                aoConfirmar.accept(selecionado);
            }
        });
        rodape.add(confirmar, BorderLayout.EAST);
        controles.add(rodape, BorderLayout.SOUTH);

        // O título faz parte do MP4. As escolhas ficam no espaço livre abaixo dele.
        fundo.posicionarControles(controles, 0.69);
    }

    public void exibirOpcoes(List<EspeciePokemon> opcoes) {
        botoesIniciais.removeAll();
        selecao = new ButtonGroup();
        detalhe.setText("Selecione seu inicial.");
        confirmar.setEnabled(false);

        for (EspeciePokemon especie : opcoes) {
            String tipo = especie.getTipo().name().equals("AGUA") ? "ÁGUA" : especie.getTipo().name();
            JToggleButton botao = new JToggleButton("<html><center>" + especie.getNome()
                + "<br><small>" + tipo + "</small></center></html>");
            EstiloJogo.personalizarEntrada(botao, false, 210, 154);
            botao.setIcon(visuais.carregarIcone(especie.getId(), 76, 76));
            botao.setHorizontalTextPosition(SwingConstants.CENTER);
            botao.setVerticalTextPosition(SwingConstants.BOTTOM);
            botao.getAccessibleContext().setAccessibleName(especie.getNome() + " · " + tipo);
            botao.setActionCommand(especie.getId());
            botao.addActionListener(evento -> {
                audio.efeito(AudioJogo.Efeito.SELECAO);
                mostrarDetalhe(especie);
                confirmar.setEnabled(true);
            });

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

    public void definirAcaoConfirmar(Consumer<String> acao) {
        aoConfirmar = acao == null ? id -> {} : acao;
    }

    void configurarNavegacao(AudioJogo audio, Runnable voltar) {
        this.audio = audio;
        this.aoVoltar = voltar;
    }

    public void apresentarCandidato(String nome) {
        candidato.setText("CANDIDATO: " + nome);
    }

    public void mostrarDetalhe(EspeciePokemon especie) {
        detalhe.setText("%s · HP %d · Ataque %d · Defesa %d".formatted(
            especie.getNome(), especie.getHpMaximo(), especie.getAtaque(), especie.getDefesa()
        ));
    }
}
