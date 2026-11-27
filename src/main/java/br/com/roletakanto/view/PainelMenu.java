package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/** Menu sobre o vídeo. Confirmação e controles de áudio ficam na mesma área. */
public final class PainelMenu extends JPanel {
  private static final long serialVersionUID = 1L;

  private final FundoVideo fundo = new FundoVideo("menuBgOficial.mp4", true);
  private final CardLayout abas = new CardLayout();
  private final JPanel controles = new JPanel(abas) {
    @Override
    public Dimension getPreferredSize() {
      // O menu comum ocupa só o espaço dos botões. Áudio e confirmação têm
      // tamanhos próprios e não empurram esse cartão para cima do título do vídeo.
      for (var cartao : getComponents()) {
        if (cartao.isVisible()) {
          return cartao.getPreferredSize();
        }
      }
      return super.getPreferredSize();
    }
  };
  private final JLabel jogador = EstiloJogo.faixa("ROLETA KANTO", 14);
  private final JLabel mensagem = EstiloJogo.faixa(" ", 12);
  private final JButton continuar = botao("CONTINUAR", false);
  private final AudioJogo audio;
  private final Runnable aoIniciar;
  private Runnable aoContinuar = () -> {};
  private Runnable aoTrocarNome = () -> {};
  private Runnable aoConfirmarNova = () -> {};

  public PainelMenu() {
    this(() -> {}, AudioJogo.SILENCIO);
  }

  PainelMenu(Runnable aoIniciar) {
    this(aoIniciar, AudioJogo.SILENCIO);
  }

  PainelMenu(Runnable aoIniciar, AudioJogo audio) {
    super(new BorderLayout());
    this.aoIniciar = aoIniciar;
    this.audio = audio;
    setPreferredSize(new Dimension(1152, 648));
    getAccessibleContext().setAccessibleName("Menu principal de Roleta Kanto");
    add(fundo, BorderLayout.CENTER);
    construirComponentes();
  }

  public void construirComponentes() {
    fundo.removeAll();
    controles.removeAll();
    controles.setOpaque(false);
    JPanel menu = EstiloJogo.painel();
    menu.add(jogador, BorderLayout.NORTH);
    JPanel linha = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    linha.setOpaque(false);
    JButton nova = botao("NOVA PARTIDA", true);
    JButton trocar = botao("TROCAR NOME", false);
    JButton audioBotao = botao("ÁUDIO", false);
    JButton sair = botao("SAIR", false);
    for (JButton botao : java.util.List.of(nova, continuar, trocar, audioBotao, sair)) {
      linha.add(botao);
    }
    menu.add(linha, BorderLayout.CENTER);
    menu.add(mensagem, BorderLayout.SOUTH);
    nova.addActionListener(evento -> acao(aoIniciar));
    if (continuar.getActionListeners().length == 0) {
      continuar.addActionListener(evento -> acao(aoContinuar));
    }
    trocar.addActionListener(evento -> acao(aoTrocarNome));
    audioBotao.addActionListener(evento -> acao(() -> abas.show(controles, "audio")));
    sair.addActionListener(evento -> {
      Window janela = SwingUtilities.getWindowAncestor(this);
      if (janela != null) {
        janela.dispatchEvent(new WindowEvent(janela, WindowEvent.WINDOW_CLOSING));
      }
    });
    controles.add(menu, "menu");
    controles.add(criarConfirmacao(), "confirmar");
    controles.add(criarAudio(), "audio");
    atualizarDisponibilidade(false);
    fundo.posicionarControles(controles, 0.75);
    revalidate();
    repaint();
  }

  void definirAcoes(Runnable continuar, Runnable trocarNome) {
    aoContinuar = continuar;
    aoTrocarNome = trocarNome;
  }

  void apresentar(String nome, boolean salvo, String aviso) {
    jogador.setText("JOGADOR · " + nome);
    mensagem.setText(aviso);
    mensagem.setVisible(!aviso.isBlank());
    atualizarDisponibilidade(salvo);
    abas.show(controles, "menu");
  }

  void confirmarNova(Runnable acao) {
    aoConfirmarNova = acao;
    abas.show(controles, "confirmar");
  }

  void fecharOpcao() {
    abas.show(controles, "menu");
  }

  boolean temOpcaoAberta() {
    return !controles.getComponent(0).isVisible();
  }

  public void atualizarDisponibilidade(boolean salvamento) {
    continuar.setEnabled(salvamento);
  }

  public void limparMensagens() {
    mensagem.setText(" ");
    mensagem.setVisible(false);
  }

  private JPanel criarConfirmacao() {
    JPanel painel = EstiloJogo.painel();
    painel.add(EstiloJogo.faixa("JÁ EXISTE UM PROGRESSO", 16), BorderLayout.NORTH);
    painel.add(EstiloJogo.faixa("Começar outra jornada arquiva o progresso anterior.", 12),
        BorderLayout.CENTER);
    JPanel botoes = new JPanel(new FlowLayout());
    botoes.setOpaque(false);
    JButton cancelar = botao("CANCELAR", false);
    JButton iniciar = botao("CONFIRMAR", true);
    cancelar.addActionListener(evento -> acao(this::fecharOpcao));
    iniciar.addActionListener(evento -> acao(aoConfirmarNova));
    botoes.add(cancelar);
    botoes.add(iniciar);
    painel.add(botoes, BorderLayout.SOUTH);
    return painel;
  }

  private JPanel criarAudio() {
    JPanel painel = EstiloJogo.painel();
    painel.add(EstiloJogo.faixa("ÁUDIO", 16), BorderLayout.NORTH);
    JPanel linhas = new JPanel(new GridLayout(2, 2, 12, 6));
    linhas.setOpaque(false);
    JCheckBox musica = new JCheckBox("Música", true);
    JCheckBox efeitos = new JCheckBox("Efeitos", true);
    JSlider volumeMusica = new JSlider(0, 100, 35);
    JSlider volumeEfeitos = new JSlider(0, 100, 45);
    volumeMusica.getAccessibleContext().setAccessibleName("Volume da música");
    volumeEfeitos.getAccessibleContext().setAccessibleName("Volume dos efeitos");
    for (JCheckBox caixa : java.util.List.of(musica, efeitos)) {
      caixa.setOpaque(false);
      caixa.setFont(EstiloJogo.fonte(java.awt.Font.BOLD, 14));
      caixa.setForeground(EstiloJogo.ROXO_ESCURO);
    }
    volumeMusica.setOpaque(false);
    volumeEfeitos.setOpaque(false);
    linhas.add(musica);
    linhas.add(volumeMusica);
    linhas.add(efeitos);
    linhas.add(volumeEfeitos);
    Runnable configurar = () -> audio.configurar(musica.isSelected(), efeitos.isSelected(),
        volumeMusica.getValue(), volumeEfeitos.getValue());
    musica.addActionListener(evento -> configurar.run());
    efeitos.addActionListener(evento -> configurar.run());
    volumeMusica.addChangeListener(evento -> configurar.run());
    volumeEfeitos.addChangeListener(evento -> configurar.run());
    painel.add(linhas, BorderLayout.CENTER);
    JButton voltar = botao("VOLTAR", false);
    voltar.addActionListener(evento -> acao(this::fecharOpcao));
    JPanel acoes = new JPanel(new FlowLayout());
    acoes.setOpaque(false);
    acoes.add(voltar);
    painel.add(acoes, BorderLayout.SOUTH);
    return painel;
  }

  private static JButton botao(String texto, boolean principal) {
    JButton botao = new JButton(texto);
    EstiloJogo.personalizarEntrada(botao, principal, 146, 48);
    return botao;
  }

  private void acao(Runnable acao) {
    audio.efeito(AudioJogo.Efeito.CLIQUE);
    acao.run();
  }
}
