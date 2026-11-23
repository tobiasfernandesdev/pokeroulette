package br.com.roletakanto.view;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.controller.ResultadoGiro;
import br.com.roletakanto.model.Jogador;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoRoleta;
import br.com.roletakanto.model.Rota;

/** Tela da rota com equipe, roleta, líder, mochila e navegação da campanha. */
public final class PainelRota extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final int DURACAO_GIRO_MS = 2800;

  private final VisualPokemon visuais = new VisualPokemon();
  private final DesenhadorRoleta desenhador = new DesenhadorRoleta();
  private final JLabel nomeRota = criarTexto("ROTA ATUAL", 17, EstiloJogo.ROXO_ESCURO);
  private final JLabel giros = criarTexto("GIROS 0", 15, EstiloJogo.ROXO_ESCURO);
  private final JLabel progresso = criarTexto("ROTA 1 / 1", 12, EstiloJogo.ROXO_ESCURO);
  private final JProgressBar barraCampanha = new JProgressBar();
  private final JPanel listaEquipe = painelTransparente();
  private final JPanel listaMochila = painelTransparente();
  private final JPanel painelEquipe = criarPainelPixel();
  private final JPanel painelInformacoes = criarPainelPixel();
  private final JLabel reserva = criarTexto("RESERVA 0 / 6", 10, EstiloJogo.ROXO);
  private final JLabel nomeLider = criarTexto("PRÓXIMO LÍDER", 15, EstiloJogo.ROXO_ESCURO);
  private final JLabel tipoLider = criarTexto("TIPO —", 11, EstiloJogo.ROXO);
  private final JLabel spriteLider = new JLabel("?", SwingConstants.CENTER);
  private final JLabel destaqueResultado = criarTexto("PRÊMIOS DA ROTA", 15, EstiloJogo.ROXO);
  private final JTextArea mensagens = new JTextArea(
      "Escolha GIRAR para descobrir o próximo encontro.");
  private final JButton botaoGirar = EstiloJogo.criarBotao("GIRAR", true);
  private final JButton botaoEquipe = EstiloJogo.criarBotao("EQUIPE", false);
  private final JButton botaoMochila = EstiloJogo.criarBotao("MOCHILA", false);
  private final JButton botaoAvancar = EstiloJogo.criarBotao("AVANÇAR", false);

  private ControladorPartida controlador;
  private Partida partida;
  private Rota rota;
  private Jogador jogador;
  private Timer animacao;
  private Runnable aoAbrirEquipe = () -> {};
  private Runnable aoAbrirMochila = () -> {};
  private Runnable aoAvancar = () -> {};

  public PainelRota() {
    super(new BorderLayout(12, 12));
    setOpaque(true);
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
    setPreferredSize(new Dimension(1280, 760));
    setMinimumSize(new Dimension(980, 620));
    EstiloJogo.aplicarAcessibilidade(
        this,
        "Rota e roleta de prêmios",
        "Tela principal da rota com equipe, roleta, líder e mochila");

    add(criarCabecalho(), BorderLayout.NORTH);
    add(criarCorpo(), BorderLayout.CENTER);
    add(criarRodape(), BorderLayout.SOUTH);
    configurarEventos();
  }

  /** Conecta o painel ao estado real da partida sem copiar regras para o Swing. */
  public void conectar(ControladorPartida controlador) {
    this.controlador = controlador;
    if (controlador == null || controlador.getPartida() == null) {
      mostrarMensagem("Inicie uma partida antes de abrir a rota.", true);
      botaoGirar.setEnabled(false);
      return;
    }
    apresentar(controlador.getPartida());
  }

  /** Ações opcionais para quando as telas vizinhas forem ligadas ao navegador. */
  public void definirAcoes(Runnable abrirEquipe, Runnable abrirMochila, Runnable avancar) {
    aoAbrirEquipe = abrirEquipe == null ? () -> {} : abrirEquipe;
    aoAbrirMochila = abrirMochila == null ? () -> {} : abrirMochila;
    aoAvancar = avancar == null ? () -> {} : avancar;
  }

  public void apresentar(Partida partida) {
    this.partida = partida;
    if (partida == null) {
      return;
    }
    apresentar(partida.getRotaAtual(), partida.getJogador());
    atualizarLider(partida.getLiderAtual());
    atualizarProgresso(partida.getNumeroRotaAtual(), partida.getTotalRotas());
  }

  public void apresentar(Rota rota, Jogador jogador) {
    if (rota == null || jogador == null) {
      return;
    }
    this.rota = rota;
    this.jogador = jogador;
    nomeRota.setText(rota.getNome().toUpperCase());
    desenhador.definirSegmentos(rota.getRoleta().getSegmentos());
    atualizarGiros(rota.getGirosRestantes());
    atualizarEquipe();
    atualizarMochila();
  }

  public void iniciarAnimacao() {
    desenhador.definirGirando(true);
    botaoGirar.setEnabled(false);
    botaoEquipe.setEnabled(false);
    botaoMochila.setEnabled(false);
    botaoAvancar.setEnabled(false);
    destaqueResultado.setText("A RODA MÁGICA ESTÁ GIRANDO...");
    destaqueResultado.setForeground(EstiloJogo.MAGENTA);
  }

  public void pararAnimacao() {
    if (animacao != null) {
      animacao.stop();
      animacao = null;
    }
    desenhador.definirGirando(false);
    botaoEquipe.setEnabled(true);
    botaoMochila.setEnabled(true);
    atualizarBotoes();
  }

  public void mostrarResultado(ResultadoRoleta resultado) {
    desenhador.definirResultado(resultado);
    destaqueResultado.setText("✦ " + nomeResultado(resultado) + " ✦");
    destaqueResultado.setForeground(EstiloJogo.ROXO);
  }

  public void atualizarGiros(int quantidade) {
    giros.setText("GIROS " + quantidade);
    giros.setForeground(quantidade > 0 ? EstiloJogo.ROXO_ESCURO : EstiloJogo.MAGENTA);
    atualizarBotoes();
  }

  private JPanel criarCabecalho() {
    JPanel cabecalho = criarPainelPixel();
    cabecalho.setLayout(new BorderLayout(18, 0));
    cabecalho.setBorder(EstiloJogo.bordaComMargem(EstiloJogo.ROXO_ESCURO, EstiloJogo.ROSA, 10));

    JLabel titulo = criarTexto("ROLETA KANTO", 24, EstiloJogo.ROXO_ESCURO);
    titulo.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 12));
    cabecalho.add(titulo, BorderLayout.WEST);

    JPanel centro = painelTransparente();
    centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
    nomeRota.setAlignmentX(Component.CENTER_ALIGNMENT);
    progresso.setAlignmentX(Component.CENTER_ALIGNMENT);
    centro.add(nomeRota);
    centro.add(Box.createVerticalStrut(4));
    centro.add(progresso);
    centro.add(Box.createVerticalStrut(4));
    barraCampanha.setForeground(EstiloJogo.MAGENTA);
    barraCampanha.setBackground(EstiloJogo.AZUL_LAVANDA);
    barraCampanha.setBorder(EstiloJogo.bordaPixel(EstiloJogo.ROXO_ESCURO, EstiloJogo.LILAS_CLARO));
    barraCampanha.setPreferredSize(new Dimension(260, 14));
    barraCampanha.setMaximumSize(new Dimension(320, 14));
    barraCampanha.setAlignmentX(Component.CENTER_ALIGNMENT);
    centro.add(barraCampanha);
    cabecalho.add(centro, BorderLayout.CENTER);

    giros.setHorizontalAlignment(SwingConstants.CENTER);
    giros.setPreferredSize(new Dimension(145, 48));
    giros.setOpaque(true);
    giros.setBackground(EstiloJogo.AMARELO);
    giros.setBorder(EstiloJogo.bordaPixel(EstiloJogo.ROXO_ESCURO, EstiloJogo.ROSA));
    cabecalho.add(giros, BorderLayout.EAST);
    return cabecalho;
  }

  private JPanel criarCorpo() {
    JPanel corpo = painelTransparente();
    corpo.setLayout(new GridBagLayout());
    GridBagConstraints posicao = new GridBagConstraints();
    posicao.gridy = 0;
    posicao.fill = GridBagConstraints.BOTH;
    posicao.weighty = 1;

    posicao.gridx = 0;
    posicao.weightx = 0.25;
    posicao.insets = new Insets(0, 0, 0, 10);
    corpo.add(criarLateralEquipe(), posicao);

    posicao.gridx = 1;
    posicao.weightx = 0.50;
    posicao.insets = new Insets(0, 0, 0, 10);
    corpo.add(criarCentroRoleta(), posicao);

    posicao.gridx = 2;
    posicao.weightx = 0.25;
    posicao.insets = new Insets(0, 0, 0, 0);
    corpo.add(criarLateralInformacoes(), posicao);
    return corpo;
  }

  private JPanel criarLateralEquipe() {
    painelEquipe.setLayout(new BorderLayout(0, 8));
    painelEquipe.add(criarTituloSecao("SUA EQUIPE"), BorderLayout.NORTH);
    listaEquipe.setLayout(new GridLayout(3, 1, 0, 8));
    painelEquipe.add(listaEquipe, BorderLayout.CENTER);
    reserva.setHorizontalAlignment(SwingConstants.CENTER);
    painelEquipe.add(reserva, BorderLayout.SOUTH);
    return painelEquipe;
  }

  private JPanel criarCentroRoleta() {
    JPanel centro = criarPainelPixel();
    centro.setLayout(new BorderLayout(0, 4));
    destaqueResultado.setHorizontalAlignment(SwingConstants.CENTER);
    destaqueResultado.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
    centro.add(destaqueResultado, BorderLayout.NORTH);
    centro.add(desenhador, BorderLayout.CENTER);

    JPanel areaBotao = painelTransparente();
    areaBotao.setLayout(new GridBagLayout());
    botaoGirar.setPreferredSize(new Dimension(210, 56));
    areaBotao.add(botaoGirar);
    centro.add(areaBotao, BorderLayout.SOUTH);
    return centro;
  }

  private JPanel criarLateralInformacoes() {
    painelInformacoes.setLayout(new BorderLayout(0, 8));
    JPanel lider = painelTransparente();
    lider.setLayout(new BoxLayout(lider, BoxLayout.Y_AXIS));
    JLabel titulo = criarTituloSecao("DESAFIO À FRENTE");
    titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
    nomeLider.setAlignmentX(Component.CENTER_ALIGNMENT);
    tipoLider.setAlignmentX(Component.CENTER_ALIGNMENT);
    spriteLider.setAlignmentX(Component.CENTER_ALIGNMENT);
    spriteLider.setPreferredSize(new Dimension(150, 180));
    spriteLider.setMaximumSize(new Dimension(180, 190));
    spriteLider.setFont(EstiloJogo.fonte(Font.BOLD, 42));
    spriteLider.setForeground(EstiloJogo.ROXO);
    lider.add(titulo);
    lider.add(Box.createVerticalStrut(5));
    lider.add(spriteLider);
    lider.add(Box.createVerticalStrut(3));
    lider.add(nomeLider);
    lider.add(tipoLider);
    painelInformacoes.add(lider, BorderLayout.NORTH);

    JPanel mochila = painelTransparente();
    mochila.setLayout(new BorderLayout(0, 7));
    mochila.add(criarTituloSecao("MOCHILA"), BorderLayout.NORTH);
    listaMochila.setLayout(new BoxLayout(listaMochila, BoxLayout.Y_AXIS));
    mochila.add(listaMochila, BorderLayout.CENTER);
    painelInformacoes.add(mochila, BorderLayout.CENTER);
    return painelInformacoes;
  }

  private JPanel criarRodape() {
    JPanel rodape = criarPainelPixel();
    rodape.setLayout(new BorderLayout(12, 0));
    mensagens.setEditable(false);
    mensagens.setFocusable(false);
    mensagens.setOpaque(false);
    mensagens.setLineWrap(true);
    mensagens.setWrapStyleWord(true);
    mensagens.setRows(2);
    mensagens.setFont(EstiloJogo.fonte(Font.BOLD, 12));
    mensagens.setForeground(EstiloJogo.ROXO_ESCURO);
    mensagens.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    rodape.add(mensagens, BorderLayout.CENTER);

    JPanel navegacao = painelTransparente();
    navegacao.setLayout(new GridLayout(1, 3, 8, 0));
    navegacao.add(botaoEquipe);
    navegacao.add(botaoMochila);
    navegacao.add(botaoAvancar);
    rodape.add(navegacao, BorderLayout.EAST);
    return rodape;
  }

  private void configurarEventos() {
    botaoGirar.addActionListener(evento -> executarGiro());
    botaoEquipe.addActionListener(evento -> {
      destacarTemporariamente(painelEquipe);
      mostrarMensagem("Sua equipe e a reserva foram destacadas à esquerda.", false);
      aoAbrirEquipe.run();
    });
    botaoMochila.addActionListener(evento -> {
      destacarTemporariamente(painelInformacoes);
      mostrarMensagem("O resumo atualizado da mochila está à direita.", false);
      aoAbrirMochila.run();
    });
    botaoAvancar.addActionListener(evento -> avancarCampanha());
  }

  private void executarGiro() {
    if (controlador == null) {
      mostrarMensagem("A roleta ainda não foi conectada à partida.", true);
      return;
    }
    ResultadoGiro giro = controlador.girarRoletaComResultado();
    if (!giro.sucesso()) {
      mostrarMensagem(giro.mensagem(), true);
      atualizarBotoes();
      return;
    }

    iniciarAnimacao();
    desenhador.definirResultado(null);
    double inicio = desenhador.getAngulo();
    double destino = desenhador.calcularAnguloDestino(giro.resultado(), 5);
    long comeco = System.nanoTime();
    animacao = new Timer(16, evento -> {
      double decorrido = (System.nanoTime() - comeco) / 1_000_000.0;
      double progressoGiro = Math.min(1.0, decorrido / DURACAO_GIRO_MS);
      double suavizado = 1.0 - Math.pow(1.0 - progressoGiro, 3.0);
      desenhador.definirAngulo(inicio + (destino - inicio) * suavizado);
      if (progressoGiro >= 1.0) {
        pararAnimacao();
        mostrarResultado(giro.resultado());
        apresentar(controlador.getPartida());
        mostrarMensagem(giro.mensagem(), false);
      }
    });
    animacao.start();
  }

  private void avancarCampanha() {
    if (partida == null || rota == null) {
      mostrarMensagem("A partida ainda não foi carregada.", true);
      return;
    }
    if (rota.temGiroDisponivel()) {
      mostrarMensagem("Use os giros restantes antes de avançar para o ginásio.", true);
      return;
    }
    try {
      partida.iniciarOrganizacao();
      mostrarMensagem("Rota concluída. Organize sua equipe para enfrentar o líder!", false);
      atualizarBotoes();
      aoAvancar.run();
    } catch (IllegalStateException erro) {
      mostrarMensagem(erro.getMessage(), true);
    }
  }

  private void atualizarEquipe() {
    listaEquipe.removeAll();
    List<Pokemon> membros = jogador.getEquipe().listar();
    for (int indice = 0; indice < 3; indice++) {
      listaEquipe.add(indice < membros.size() ? criarCartaoPokemon(membros.get(indice)) : criarVagaVazia());
    }
    reserva.setText("RESERVA " + jogador.getReserva().listar().size() + " / 6");
    listaEquipe.revalidate();
    listaEquipe.repaint();
  }

  private JPanel criarCartaoPokemon(Pokemon pokemon) {
    JPanel cartao = new JPanel(new BorderLayout(7, 3));
    cartao.setOpaque(true);
    cartao.setBackground(EstiloJogo.CREME);
    cartao.setBorder(EstiloJogo.bordaComMargem(EstiloJogo.ROXO, EstiloJogo.LAVANDA, 5));

    JLabel sprite = new JLabel("◆", SwingConstants.CENTER);
    sprite.setForeground(EstiloJogo.ROXO);
    sprite.setFont(EstiloJogo.fonte(Font.BOLD, 24));
    sprite.setPreferredSize(new Dimension(72, 66));
    ImageIcon icone = visuais.carregarIcone(pokemon.getEspecie().getId(), 68, 62);
    if (icone != null) {
      sprite.setText("");
      sprite.setIcon(icone);
    }
    cartao.add(sprite, BorderLayout.WEST);

    JPanel dados = painelTransparente();
    dados.setLayout(new BoxLayout(dados, BoxLayout.Y_AXIS));
    JLabel nome = criarTexto(pokemon.getEspecie().getNome().toUpperCase(), 12, EstiloJogo.ROXO_ESCURO);
    JLabel tipo = criarTexto(pokemon.getEspecie().getTipo().name(), 9, EstiloJogo.ROXO);
    nome.setAlignmentX(Component.LEFT_ALIGNMENT);
    tipo.setAlignmentX(Component.LEFT_ALIGNMENT);
    JProgressBar hp = new JProgressBar(0, pokemon.getHpMaximo());
    hp.setValue(pokemon.getHpAtual());
    hp.setStringPainted(true);
    hp.setString("HP " + pokemon.getHpAtual() + " / " + pokemon.getHpMaximo());
    hp.setFont(EstiloJogo.fonte(Font.BOLD, 9));
    hp.setForeground(pokemon.getHpAtual() * 4 > pokemon.getHpMaximo()
        ? EstiloJogo.VERDE_HP : EstiloJogo.VERMELHO_HP);
    hp.setBackground(EstiloJogo.AZUL_LAVANDA);
    hp.setBorder(BorderFactory.createLineBorder(EstiloJogo.ROXO_ESCURO, 2));
    hp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
    hp.setAlignmentX(Component.LEFT_ALIGNMENT);
    dados.add(nome);
    dados.add(tipo);
    dados.add(Box.createVerticalStrut(4));
    dados.add(hp);
    cartao.add(dados, BorderLayout.CENTER);
    return cartao;
  }

  private JPanel criarVagaVazia() {
    JPanel vaga = new JPanel(new GridBagLayout());
    vaga.setOpaque(true);
    vaga.setBackground(new Color(0xE9, 0xE3, 0xF5));
    vaga.setBorder(EstiloJogo.bordaComMargem(EstiloJogo.LAVANDA, EstiloJogo.AZUL_LAVANDA, 5));
    JLabel texto = criarTexto("◇  VAGA LIVRE", 11, new Color(0x7C, 0x68, 0x89));
    vaga.add(texto);
    return vaga;
  }

  private void atualizarMochila() {
    listaMochila.removeAll();
    List<PilhaItem> itens = jogador.getMochila().listar();
    if (itens.isEmpty()) {
      JLabel vazia = criarTexto("Mochila vazia", 11, EstiloJogo.ROXO);
      vazia.setHorizontalAlignment(SwingConstants.CENTER);
      listaMochila.add(vazia);
    } else {
      for (PilhaItem pilha : itens) {
        JPanel linha = new JPanel(new BorderLayout(6, 0));
        linha.setOpaque(true);
        linha.setBackground(EstiloJogo.CREME);
        linha.setBorder(EstiloJogo.bordaComMargem(EstiloJogo.ROXO, EstiloJogo.LAVANDA, 5));
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.add(criarTexto(pilha.getItem().getNome(), 10, EstiloJogo.ROXO_ESCURO), BorderLayout.CENTER);
        JLabel quantidade = criarTexto("x" + pilha.getQuantidade(), 12, EstiloJogo.MAGENTA);
        linha.add(quantidade, BorderLayout.EAST);
        listaMochila.add(linha);
        listaMochila.add(Box.createVerticalStrut(5));
      }
    }
    listaMochila.revalidate();
    listaMochila.repaint();
  }

  private void atualizarLider(LiderGinasio lider) {
    if (lider == null) {
      return;
    }
    nomeLider.setText(lider.getNome().toUpperCase());
    String tipo = lider.getEquipe().isEmpty() ? "—" : lider.getEquipe().get(0).getTipo().name();
    tipoLider.setText("TIPO " + tipo);
    ImageIcon icone = visuais.carregarLider(lider.getId(), 145, 170);
    spriteLider.setIcon(icone);
    spriteLider.setText(icone == null ? lider.getNome().substring(0, 1) : "");
  }

  private void atualizarProgresso(int atual, int total) {
    progresso.setText("ROTA " + atual + " / " + total);
    barraCampanha.setMinimum(0);
    barraCampanha.setMaximum(Math.max(1, total));
    barraCampanha.setValue(Math.max(0, atual));
    barraCampanha.setToolTipText("Progresso da campanha: rota " + atual + " de " + total);
  }

  private void atualizarBotoes() {
    if (animacao != null && animacao.isRunning()) {
      return;
    }
    boolean possuiPartida = rota != null && jogador != null;
    boolean podeGirar = controlador != null && possuiPartida && rota.temGiroDisponivel()
        && (partida == null || partida.getFase() == br.com.roletakanto.model.FasePartida.ROTA);
    botaoGirar.setEnabled(podeGirar);
    botaoAvancar.setEnabled(possuiPartida && !rota.temGiroDisponivel()
        && partida != null
        && partida.getFase() == br.com.roletakanto.model.FasePartida.ROTA);
  }

  private void mostrarMensagem(String texto, boolean erro) {
    mensagens.setText(texto == null ? "" : texto);
    mensagens.setForeground(erro ? new Color(0xA5, 0x16, 0x4D) : EstiloJogo.ROXO_ESCURO);
  }

  private void destacarTemporariamente(JPanel painel) {
    painel.setBorder(EstiloJogo.bordaComMargem(EstiloJogo.MAGENTA, EstiloJogo.AMARELO, 9));
    Timer retirar = new Timer(650, evento ->
        painel.setBorder(EstiloJogo.bordaComMargem(
            EstiloJogo.ROXO_ESCURO, EstiloJogo.LILAS_CLARO, 9)));
    retirar.setRepeats(false);
    retirar.start();
  }

  private static String nomeResultado(ResultadoRoleta resultado) {
    if (resultado == null) {
      return "PRÊMIO";
    }
    return switch (resultado.idConteudo()) {
      case "pocao" -> "POÇÃO";
      case "super_pocao" -> "SUPER POÇÃO";
      case "reviver" -> "REVIVER";
      case "pokebola_dourada" -> "POKÉBOLA DOURADA";
      case "evento" -> "EVENTO DA ROTA";
      default -> resultado.idConteudo().replace('_', ' ').toUpperCase();
    };
  }

  private static JPanel criarPainelPixel() {
    JPanel painel = new JPanel();
    painel.setOpaque(true);
    painel.setBackground(new Color(0xF4, 0xEF, 0xFF));
    painel.setBorder(EstiloJogo.bordaComMargem(
        EstiloJogo.ROXO_ESCURO, EstiloJogo.LILAS_CLARO, 9));
    return painel;
  }

  private static JPanel painelTransparente() {
    JPanel painel = new JPanel();
    painel.setOpaque(false);
    return painel;
  }

  private static JLabel criarTituloSecao(String texto) {
    JLabel titulo = criarTexto(texto, 14, EstiloJogo.ROXO_ESCURO);
    titulo.setHorizontalAlignment(SwingConstants.CENTER);
    titulo.setOpaque(true);
    titulo.setBackground(EstiloJogo.AZUL_LAVANDA);
    titulo.setBorder(EstiloJogo.bordaPixel(EstiloJogo.ROXO, EstiloJogo.LAVANDA));
    return titulo;
  }

  private static JLabel criarTexto(String texto, int tamanho, Color cor) {
    JLabel label = new JLabel(texto);
    label.setFont(EstiloJogo.fonte(Font.BOLD, tamanho));
    label.setForeground(cor);
    return label;
  }

  @Override
  protected void paintComponent(Graphics grafico) {
    super.paintComponent(grafico);
    Graphics2D desenho = (Graphics2D) grafico.create();
    try {
      desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      desenho.setComposite(AlphaComposite.SrcOver.derive(0.13f));
      desenho.setColor(EstiloJogo.LILAS_CLARO);
      int tamanho = 28;
      for (int y = 0; y < getHeight(); y += tamanho) {
        for (int x = (y / tamanho % 2) * tamanho; x < getWidth(); x += tamanho * 2) {
          desenho.fillRect(x, y, tamanho, tamanho);
        }
      }
    } finally {
      desenho.dispose();
    }
  }
}
