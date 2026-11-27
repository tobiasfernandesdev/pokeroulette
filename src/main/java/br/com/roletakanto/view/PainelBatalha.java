package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.service.CatalogoJogo;

/** Apresenta o estado de qualquer ginásio. Cada botão envia uma ação ao controlador. */
public final class PainelBatalha extends JPanel {
  private static final long serialVersionUID = 1L;
  private final VisualPokemon visuais = new VisualPokemon();
  private final JLabel titulo = EstiloJogo.faixa("GINÁSIO", 22);
  private final CartaoPokemon jogador = new CartaoPokemon(visuais, 56);
  private final CartaoPokemon adversario = new CartaoPokemon(visuais, 56);
  private final Arena arena = new Arena();
  private final JPanel golpes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
  private final JButton mochila = EstiloJogo.criarBotao("MOCHILA", false);
  private final JButton trocar = EstiloJogo.criarBotao("TROCAR POKÉMON", false);
  private final JTextArea mensagens = new JTextArea(4, 30);

  private Batalha batalha;
  private Partida partida;
  private ControladorBatalha controlador;
  private Consumer<ResultadoTurno> aoTerminar = turno -> {};
  private boolean bloqueado;
  private AudioJogo audio = AudioJogo.SILENCIO;
  private Runnable aoAbrirMochila = () -> {};
  private Runnable aoAbrirTroca = () -> {};
  private Runnable aoAtualizar = () -> {};

  public PainelBatalha() {
    super(new BorderLayout(12, 12));
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(titulo, BorderLayout.NORTH);

    JPanel palco = EstiloJogo.painel();
    JPanel duplas = new JPanel(new GridLayout(1, 2, 10, 0));
    duplas.setOpaque(false);
    duplas.add(criarLado("SEU POKÉMON", jogador));
    duplas.add(criarLado("POKÉMON DO LÍDER", adversario));
    palco.add(duplas, BorderLayout.NORTH);
    palco.add(arena, BorderLayout.CENTER);
    add(palco, BorderLayout.CENTER);

    JPanel turno = EstiloJogo.painel();
    mensagens.setEditable(false);
    mensagens.setLineWrap(true);
    mensagens.setWrapStyleWord(true);
    mensagens.setFont(EstiloJogo.fonte(Font.PLAIN, 13));
    mensagens.setForeground(EstiloJogo.ROXO_ESCURO);
    mensagens.setBackground(EstiloJogo.CREME);
    mensagens.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    mensagens.getAccessibleContext().setAccessibleName("Mensagens do combate");
    turno.add(new JScrollPane(mensagens), BorderLayout.CENTER);

    JPanel acoes = new JPanel(new BorderLayout(8, 6));
    acoes.setOpaque(false);
    golpes.setOpaque(false);
    acoes.add(golpes, BorderLayout.CENTER);
    JPanel auxiliares = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    auxiliares.setOpaque(false);
    auxiliares.add(mochila);
    auxiliares.add(trocar);
    acoes.add(auxiliares, BorderLayout.EAST);
    turno.add(acoes, BorderLayout.SOUTH);
    add(turno, BorderLayout.SOUTH);

    mochila.addActionListener(evento -> aoAbrirMochila.run());
    trocar.addActionListener(evento -> aoAbrirTroca.run());
    atualizarStatus();
  }

  void configurarInteracoes(AudioJogo audio, Runnable abrirMochila, Runnable abrirTroca, Runnable atualizar) {
    this.audio = audio;
    aoAbrirMochila = abrirMochila;
    aoAbrirTroca = abrirTroca;
    aoAtualizar = atualizar;
  }

  private static JPanel criarLado(String nome, CartaoPokemon cartao) {
    JPanel lado = new JPanel(new BorderLayout(0, 4));
    lado.setOpaque(false);
    lado.add(EstiloJogo.faixa(nome, 12), BorderLayout.NORTH);
    lado.add(cartao, BorderLayout.CENTER);
    return lado;
  }

  void conectar(Partida partida, ControladorBatalha controlador, Consumer<ResultadoTurno> aoTerminar) {
    this.partida = partida;
    this.controlador = controlador;
    this.aoTerminar = aoTerminar;
    bloqueado = false;
    apresentar(partida.getBatalhaAtual());
  }

  public void apresentar(Batalha batalha) {
    if (this.batalha != batalha && batalha != null) {
      var lider = batalha.getLider();
      String tipo = lider.getEquipe().isEmpty() ? "" : "  ·  " + lider.getEquipe().getFirst().getTipo();
      String nomeJogador = partida == null ? "JOGADOR" : partida.getJogador().getNome();
      titulo.setText(nomeJogador + "  /  GINÁSIO  /  " + lider.getNome() + tipo);
      titulo.setIcon(visuais.carregarLider(lider.getId(), 40, 40));
      arena.cenario = CenarioPixel.doLider(lider.getId());
      mensagens.setText("");
      adicionarMensagem(lider.getNome() + " aceitou o desafio!");
    }
    this.batalha = batalha;
    atualizarStatus();
  }

  public void atualizarStatus() {
    Pokemon ativo = batalha == null ? null : batalha.obterAtivoJogador();
    Pokemon oponente = batalha == null ? null : batalha.getAtivoLider();
    jogador.apresentar(ativo);
    adversario.apresentar(oponente);
    arena.mostrar(ativo, oponente);

    boolean podeAgir = podeAgir();
    golpes.removeAll();
    if (ativo != null) {
      for (Golpe golpe : ativo.getEspecie().getGolpes()) {
        int usos = batalha.getUsosRestantes(ativo, golpe);
        String contador = golpe.ehIlimitado() ? "∞"
            : usos + "/" + batalha.getUsosMaximos(ativo, golpe);
        JButton botao = EstiloJogo.criarBotao(golpe.getNome() + " · " + contador, !golpe.ehIlimitado());
        botao.setEnabled(podeAgir && ativo.estaApto() && (golpe.ehIlimitado() || usos > 0));
        botao.setToolTipText(golpe.ehIlimitado() ? "Golpe básico · usos ilimitados" : "Golpe especial · usos restantes");
        botao.addActionListener(evento -> {
          audio.efeito(AudioJogo.Efeito.CONFIRMA);
          executar(() -> controlador.executarGolpe(golpe.getId()));
        });
        golpes.add(botao);
      }
    }
    golpes.revalidate();
    golpes.repaint();
    mochila.setEnabled(podeAgir && !itensDisponiveis().isEmpty());
    trocar.setEnabled(podeAgir && !trocasDisponiveis().isEmpty());
  }

  public void adicionarMensagem(String mensagem) {
    if (mensagem != null && !mensagem.isBlank()) {
      mensagens.append(mensagem + "\n");
      mensagens.setCaretPosition(mensagens.getDocument().getLength());
    }
  }

  public void bloquearAcoes(boolean bloquear) {
    bloqueado = bloquear;
    atualizarStatus();
  }

  private boolean podeAgir() {
    return !bloqueado && controlador != null && controlador.batalhaAtiva();
  }

  private List<PilhaItem> itensDisponiveis() {
    return partida.getJogador().getMochila().listar().stream()
        .filter(pilha -> pilha.getItem().podeUsarEm(partida.getFase()))
        .toList();
  }

  List<Pokemon> trocasDisponiveis() {
    return partida.getJogador().getEquipe().listar().stream().filter(batalha::podeTrocar).toList();
  }

  String usarItem(String id) {
    var inventario = partida.getJogador().getMochila();
    PilhaItem antes = inventario.obterPilhaPorId(id);
    int quantidade = antes == null ? 0 : antes.getQuantidade();
    var turno = executar(() -> controlador.usarItem(id));
    PilhaItem depois = inventario.obterPilhaPorId(id);
    if (quantidade > (depois == null ? 0 : depois.getQuantidade())) {
      audio.efeito(CatalogoJogo.ID_REVIVER.equals(id) ? AudioJogo.Efeito.REVIVER : AudioJogo.Efeito.CURA);
    }
    return turno.mensagens().isEmpty() ? " " : turno.mensagens().getFirst();
  }

  void trocarPor(Pokemon escolhido) {
    executar(() -> controlador.trocarPokemon(escolhido.getId()));
  }

  private ResultadoTurno executar(Supplier<ResultadoTurno> acao) {
    if (!podeAgir()) {
      return new ResultadoTurno(List.of("Aguarde o fim do turno."), ResultadoBatalha.EM_ANDAMENTO, null);
    }
    bloquearAcoes(true);
    try {
      Pokemon aliado = batalha.obterAtivoJogador();
      Pokemon rival = batalha.getAtivoLider();
      int hpAliado = aliado.getHpAtual();
      int hpRival = rival.getHpAtual();
      ResultadoTurno turno = acao.get();
      turno.mensagens().forEach(this::adicionarMensagem);
      if (rival.getHpAtual() < hpRival) {
        audio.efeito(AudioJogo.Efeito.ATAQUE);
      }
      if (aliado.getHpAtual() < hpAliado) {
        audio.efeito(AudioJogo.Efeito.DANO);
      }
      if ((hpAliado > 0 && !aliado.estaApto()) || (hpRival > 0 && !rival.estaApto())) {
        audio.efeito(AudioJogo.Efeito.DESMAIO);
      }
      aoAtualizar.run();
      if (turno.resultado() != ResultadoBatalha.EM_ANDAMENTO) {
        aoTerminar.accept(turno);
      }
      return turno;
    } catch (IllegalStateException erro) {
      adicionarMensagem(erro.getMessage());
      return new ResultadoTurno(List.of(erro.getMessage()), ResultadoBatalha.EM_ANDAMENTO, null);
    } finally {
      bloquearAcoes(false);
    }
  }

  private final class Arena extends JPanel {
    private static final long serialVersionUID = 1L;
    private CenarioPixel cenario = CenarioPixel.ROCHA;
    private Pokemon aliado;
    private Pokemon oponente;

    Arena() {
      setPreferredSize(new Dimension(540, 285));
      setBorder(EstiloJogo.bordaPixel(EstiloJogo.ROXO_ESCURO, EstiloJogo.LAVANDA));
    }

    void mostrar(Pokemon aliado, Pokemon oponente) {
      this.aliado = aliado;
      this.oponente = oponente;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics grafico) {
      super.paintComponent(grafico);
      cenario.pintar(grafico, getWidth(), getHeight());
      int tamanho = Math.max(1, Math.min(230, Math.min(getWidth() / 4, (int) (getHeight() * 0.43))));
      pintarPokemon(grafico, aliado, getWidth() * 28 / 100, getHeight() * 85 / 100, tamanho);
      pintarPokemon(grafico, oponente, getWidth() * 73 / 100, getHeight() * 63 / 100, tamanho);
    }

    private void pintarPokemon(Graphics grafico, Pokemon pokemon, int x, int y, int tamanho) {
      if (pokemon == null) {
        return;
      }
      int plataforma = tamanho * 3 / 2;
      grafico.setColor(new Color(0x3D, 0x16, 0x57, 135));
      grafico.fillOval(x - plataforma / 2, y - tamanho / 10, plataforma, tamanho / 5);
      grafico.setColor(EstiloJogo.LILAS_CLARO);
      grafico.fillOval(x - plataforma / 2, y - tamanho / 10, plataforma, tamanho / 7);
      // Agrupar em múltiplos de 8 limita o número de tamanhos guardados durante o redimensionamento.
      int lado = Math.max(8, tamanho / 8 * 8);
      ImageIcon imagem = visuais.carregarIcone(pokemon.getEspecie().getId(), lado, lado);
      if (imagem != null) {
        imagem.paintIcon(this, grafico, x - imagem.getIconWidth() / 2, y - imagem.getIconHeight());
      }
    }
  }
}
