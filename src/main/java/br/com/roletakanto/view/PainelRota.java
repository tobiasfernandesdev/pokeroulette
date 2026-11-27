package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.controller.ResultadoGiro;
import br.com.roletakanto.model.EscolhaEventoRota;
import br.com.roletakanto.model.Jogador;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoRoleta;
import br.com.roletakanto.model.Rota;
import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.service.CatalogoJogo;

/** Tela da rota com equipe, roleta, líder, mochila e navegação da campanha. */
public final class PainelRota extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final int DURACAO_GIRO_MS = 2800;

  private final VisualPokemon visuais = new VisualPokemon();
  private final DesenhadorRoleta desenhador = new DesenhadorRoleta();
  private final JLabel nomeRota = criarTexto("ROTA ATUAL", 17, EstiloJogo.ROXO_ESCURO);
  private final JLabel nomeJogador = criarTexto("CANDIDATO: JOGADOR", 11, EstiloJogo.ROXO);
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
  private final JLabel spriteLider = new RetratoLider();
  private final JLabel destaqueResultado = criarTexto("PRÊMIOS DA ROTA", 15, EstiloJogo.ROXO);
  private final JTextArea mensagens = new JTextArea(
      "Escolha GIRAR para descobrir o próximo encontro.");
  private final JButton botaoGirar = EstiloJogo.criarBotao("GIRAR", true);
  private final JButton botaoEquipe = EstiloJogo.criarBotao("EQUIPE", false);
  private final JButton botaoMochila = EstiloJogo.criarBotao("MOCHILA", false);
  private final JButton botaoEvento = EstiloJogo.criarBotao("EVENTO", false);
  private final JButton botaoAvancar = EstiloJogo.criarBotao("AVANÇAR", false);

  private ControladorPartida controlador;
  private Partida partida;
  private Rota rota;
  private CenarioPixel cenario = CenarioPixel.CAMPO;
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
    if (this.rota != rota) {
      cenario = CenarioPixel.daRota(rota.getId());
      desenhador.definirAngulo(0);
      desenhador.definirResultado(null);
      destaqueResultado.setText("PRÊMIOS DA ROTA");
      destaqueResultado.setIcon(null);
      mostrarMensagem("Escolha GIRAR para descobrir o próximo encontro.", false);
      repaint();
    }
    this.rota = rota;
    this.jogador = jogador;
    nomeJogador.setText("CANDIDATO: " + jogador.getNome().toUpperCase(java.util.Locale.ROOT));
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
    botaoEvento.setEnabled(false);
    botaoAvancar.setEnabled(false);
    destaqueResultado.setText("A RODA MÁGICA ESTÁ GIRANDO...");
    destaqueResultado.setIcon(null);
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
    destaqueResultado.setIcon(resultado == null ? null
        : visuais.carregarPremio(resultado.tipo(), resultado.idConteudo(), 32));
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
    centro.setLayout(new GridBagLayout());
    GridBagConstraints faixa = new GridBagConstraints();
    faixa.gridx = 0;
    faixa.gridy = 0;
    faixa.weightx = 1;
    faixa.insets = new Insets(0, 0, 4, 0);
    centro.add(nomeRota, faixa);
    faixa.gridy++;
    centro.add(nomeJogador, faixa);
    faixa.gridy++;
    centro.add(progresso, faixa);
    barraCampanha.setForeground(EstiloJogo.MAGENTA);
    barraCampanha.setBackground(EstiloJogo.AZUL_LAVANDA);
    barraCampanha.setBorder(EstiloJogo.bordaPixel(EstiloJogo.ROXO_ESCURO, EstiloJogo.LILAS_CLARO));
    barraCampanha.setPreferredSize(new Dimension(260, 14));
    faixa.gridy++;
    faixa.insets = new Insets(0, 0, 0, 0);
    centro.add(barraCampanha, faixa);
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
    centro.setOpaque(false);
    centro.setLayout(new BorderLayout(0, 4));
    destaqueResultado.setHorizontalAlignment(SwingConstants.CENTER);
    destaqueResultado.setOpaque(true);
    destaqueResultado.setBackground(EstiloJogo.LILAS_CLARO);
    destaqueResultado.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
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
    nomeLider.setHorizontalAlignment(SwingConstants.CENTER);
    tipoLider.setHorizontalAlignment(SwingConstants.CENTER);
    nomeLider.setMaximumSize(new Dimension(Integer.MAX_VALUE, nomeLider.getPreferredSize().height));
    tipoLider.setMaximumSize(new Dimension(Integer.MAX_VALUE, tipoLider.getPreferredSize().height));
    spriteLider.setAlignmentX(Component.CENTER_ALIGNMENT);
    spriteLider.setPreferredSize(new Dimension(150, 144));
    spriteLider.setMaximumSize(new Dimension(180, 145));
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
    JScrollPane rolagem = new JScrollPane(listaMochila);
    rolagem.setBorder(BorderFactory.createEmptyBorder());
    rolagem.setOpaque(false);
    rolagem.getViewport().setOpaque(false);
    rolagem.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    rolagem.getVerticalScrollBar().setUnitIncrement(24);
    mochila.add(rolagem, BorderLayout.CENTER);
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
    navegacao.setLayout(new GridLayout(1, 4, 8, 0));
    navegacao.add(botaoEquipe);
    navegacao.add(botaoMochila);
    navegacao.add(botaoEvento);
    navegacao.add(botaoAvancar);
    rodape.add(navegacao, BorderLayout.EAST);
    return rodape;
  }

  private void configurarEventos() {
    botaoGirar.addActionListener(evento -> executarGiro());
    botaoEquipe.addActionListener(evento -> {
      aoAbrirEquipe.run();
    });
    botaoMochila.addActionListener(evento -> abrirMochila());
    botaoEvento.addActionListener(evento -> visitarEventoOpcional());
    botaoAvancar.addActionListener(evento -> avancarCampanha());
  }

  private void abrirMochila() {
    if (controlador == null || partida == null || partida.getFase() != FasePartida.ROTA) {
      return;
    }
    aoAbrirMochila.run();
    var mochila = jogador.getMochila();
    PilhaItem pilha = MochilaVisual.escolher(this, mochila.listar().stream()
        .filter(item -> item.getItem().podeUsarEm(partida.getFase())).toList(), visuais);
    if (pilha == null) {
      return;
    }
    String id = pilha.getItem().getId();
    if (CatalogoJogo.ID_POKEBOLA_DOURADA.equals(id)) {
      mostrarMensagem(controlador.usarGiroBonus(), false);
    } else {
      Pokemon alvo = MochilaVisual.escolherPokemon(this, pilha.getItem().getNome(),
          jogador.getEquipe().listar());
      if (alvo == null) {
        return;
      }
      var uso = mochila.usar(id, new ContextoUsoItem(partida, null, alvo));
      mostrarMensagem(uso.mensagem(), !uso.sucesso());
    }
    apresentar(partida);
  }

  private void visitarEventoOpcional() {
    if (controlador == null || rota == null || !rota.eventoOpcionalDisponivel()) {
      mostrarMensagem("O evento opcional desta rota não está disponível.", true);
      return;
    }

    Object[] opcoes = {"Descansar", "Pegar Super Poção", "Cancelar"};
    int escolha = JOptionPane.showOptionDialog(
        this,
        "Escolha uma recompensa para esta rota.",
        "Evento opcional",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        opcoes,
        opcoes[0]);
    if (escolha == 0) {
      mostrarMensagem(controlador.usarEventoOpcional(EscolhaEventoRota.DESCANSO), false);
    } else if (escolha == 1) {
      mostrarMensagem(controlador.usarEventoOpcional(EscolhaEventoRota.SUPRIMENTOS), false);
    } else {
      return;
    }
    apresentar(controlador.getPartida());
  }

  private void executarGiro() {
    if (animacao != null) {
      return;
    }
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
      controlador.navegarParaFase(FasePartida.ORGANIZACAO);
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
    CartaoPokemon cartao = new CartaoPokemon(visuais, 58);
    cartao.apresentar(pokemon);
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
        // A imagem acompanha o nome: mesmo sem um arquivo, o item continua legível.
        ImageIcon icone = visuais.carregarItem(pilha.getItem().getId(), 24, 24);
        if (icone != null) {
          JLabel imagem = new JLabel(icone);
          imagem.setToolTipText(pilha.getItem().getNome());
          linha.add(imagem, BorderLayout.WEST);
        }
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
    ImageIcon icone = visuais.carregarLider(lider.getId(), 136, 134);
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
    botaoEquipe.setEnabled(possuiPartida);
    botaoMochila.setEnabled(controlador != null && possuiPartida);
    boolean podeGirar = controlador != null && possuiPartida && rota.temGiroDisponivel()
        && (partida == null || partida.getFase() == br.com.roletakanto.model.FasePartida.ROTA);
    botaoGirar.setEnabled(podeGirar);
    botaoEvento.setEnabled(controlador != null
        && possuiPartida
        && partida != null
        && partida.getFase() == br.com.roletakanto.model.FasePartida.ROTA
        && rota.eventoOpcionalDisponivel());
    botaoAvancar.setEnabled(controlador != null && possuiPartida && !rota.temGiroDisponivel()
        && partida != null
        && partida.getFase() == br.com.roletakanto.model.FasePartida.ROTA);
  }

  private void mostrarMensagem(String texto, boolean erro) {
    mensagens.setText(texto == null ? "" : texto);
    mensagens.setForeground(erro ? new Color(0xA5, 0x16, 0x4D) : EstiloJogo.ROXO_ESCURO);
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
      case "cura_status" -> "CURA DE STATUS";
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

  boolean estaGirando() {
    return animacao != null;
  }

  @Override
  protected void paintComponent(Graphics grafico) {
    super.paintComponent(grafico);
    cenario.pintar(grafico, getWidth(), getHeight());
  }
}
