package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.Equipe;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.Reserva;

/** Permite reorganizar os Pokémon da rota e preparar a formação antes do ginásio. */
public final class PainelEquipe extends JPanel {
  private static final long serialVersionUID = 1L;
  private final JLabel titulo = EstiloJogo.faixa("EQUIPE E RESERVA", 19);
  private final JLabel mensagem = EstiloJogo.faixa("Selecione um Pokémon para organizar a formação.", 13);
  private final JList<Pokemon> ativos = new JList<>();
  private final JList<Pokemon> reservados = new JList<>();
  private final JButton mover = EstiloJogo.criarBotao("MOVER", false);
  private final JButton primeiro = EstiloJogo.criarBotao("COMEÇAR COM ESTE", false);
  private final JButton voltar = EstiloJogo.criarBotao("VOLTAR À ROTA", false);
  private final JButton desafiar = EstiloJogo.criarBotao("DESAFIAR LÍDER", true);
  private Equipe equipe;
  private Reserva reserva;
  private ControladorPartida controlador;
  private boolean atualizando;

  public PainelEquipe() {
    super(new BorderLayout(12, 12));
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(titulo, BorderLayout.NORTH);

    JPanel colunas = new JPanel(new GridLayout(1, 2, 12, 0));
    colunas.setOpaque(false);
    VisualPokemon visuais = new VisualPokemon();
    colunas.add(criarLista("EQUIPE ATIVA", ativos, reservados, visuais));
    colunas.add(criarLista("RESERVA", reservados, ativos, visuais));
    add(colunas, BorderLayout.CENTER);

    JPanel controles = EstiloJogo.painel();
    JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    botoes.setOpaque(false);
    for (JButton botao : List.of(voltar, mover, primeiro, desafiar)) {
      botoes.add(botao);
    }
    controles.add(mensagem, BorderLayout.NORTH);
    controles.add(botoes, BorderLayout.SOUTH);
    add(controles, BorderLayout.SOUTH);
    mover.addActionListener(evento -> moverSelecionado());
    primeiro.addActionListener(evento -> definirPrimeiro());
    atualizarBotoes();
  }

  void conectar(ControladorPartida controlador, Runnable aoVoltar, Runnable aoDesafiar) {
    this.controlador = controlador;
    voltar.addActionListener(evento -> executar(aoVoltar));
    desafiar.addActionListener(evento -> executar(aoDesafiar));
  }

  void apresentarPartida() {
    var partida = controlador.getPartida();
    boolean preparando = partida.getFase() == FasePartida.ORGANIZACAO;
    titulo.setText(preparando ? "PREPARAÇÃO · " + partida.getLiderAtual().getNome() : "EQUIPE E RESERVA");
    voltar.setVisible(!preparando || partida.getRotaAtual().temGiroDisponivel());
    desafiar.setVisible(preparando);
    mensagem.setText("O primeiro Pokémon apto da equipe começa o combate.");
    apresentar(partida.getJogador().getEquipe(), partida.getJogador().getReserva());
  }

  public void apresentar(Equipe equipe, Reserva reserva) {
    this.equipe = equipe;
    this.reserva = reserva;
    atualizarFormacao();
  }

  public void selecionarPokemon(String id) {
    for (JList<Pokemon> lista : List.of(ativos, reservados)) {
      for (int i = 0; i < lista.getModel().getSize(); i++) {
        if (lista.getModel().getElementAt(i).getId().equals(id)) {
          lista.setSelectedIndex(i);
          lista.ensureIndexIsVisible(i);
          return;
        }
      }
    }
  }

  public void atualizarFormacao() {
    if (equipe == null || reserva == null) {
      return;
    }
    atualizando = true;
    ativos.setListData(equipe.listar().toArray(Pokemon[]::new));
    reservados.setListData(reserva.listar().toArray(Pokemon[]::new));
    atualizando = false;
    atualizarBotoes();
  }

  private JPanel criarLista(String nome, JList<Pokemon> lista, JList<Pokemon> outra, VisualPokemon visuais) {
    JPanel painel = EstiloJogo.painel();
    painel.add(EstiloJogo.faixa(nome, 15), BorderLayout.NORTH);
    lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lista.setFixedCellHeight(82);
    lista.setBackground(EstiloJogo.LILAS_CLARO);
    CartaoPokemon cartao = new CartaoPokemon(visuais, 58);
    lista.setCellRenderer((origem, pokemon, indice, selecionado, temFoco) -> {
      cartao.apresentar(pokemon);
      cartao.setBackground(selecionado ? EstiloJogo.AZUL_LAVANDA : EstiloJogo.CREME);
      cartao.setBorder(EstiloJogo.bordaComMargem(
          selecionado ? EstiloJogo.MAGENTA : EstiloJogo.ROXO, EstiloJogo.LAVANDA, 5));
      // Um renderizador de lista pinta o cartão diretamente, sem montá-lo na janela.
      cartao.setSize(origem.getWidth(), origem.getFixedCellHeight());
      cartao.doLayout();
      return cartao;
    });
    lista.getAccessibleContext().setAccessibleName(nome);
    lista.addListSelectionListener(evento -> {
      if (!atualizando && !evento.getValueIsAdjusting()) {
        if (!lista.isSelectionEmpty()) {
          outra.clearSelection();
        }
        atualizarBotoes();
      }
    });
    JScrollPane rolagem = new JScrollPane(lista);
    rolagem.setPreferredSize(new Dimension(300, 270));
    painel.add(rolagem, BorderLayout.CENTER);
    return painel;
  }

  private void moverSelecionado() {
    Pokemon ativo = ativos.getSelectedValue();
    Pokemon guardado = reservados.getSelectedValue();
    if (controlador == null || (ativo == null && guardado == null)) {
      return;
    }
    executar(() -> {
      if (ativo != null) {
        controlador.moverParaReserva(ativo.getId());
        mensagem.setText(ativo.getEspecie().getNome() + " foi para a reserva.");
      } else {
        controlador.moverParaEquipe(guardado.getId());
        mensagem.setText(guardado.getEspecie().getNome() + " entrou na equipe.");
      }
      atualizarFormacao();
      selecionarPokemon(ativo != null ? ativo.getId() : guardado.getId());
    });
  }

  private void definirPrimeiro() {
    Pokemon escolhido = ativos.getSelectedValue();
    if (escolhido != null && escolhido.estaApto()) {
      equipe.trocarPosicao(0, ativos.getSelectedIndex());
      atualizarFormacao();
      selecionarPokemon(escolhido.getId());
      mensagem.setText(escolhido.getEspecie().getNome() + " fica em primeiro na equipe.");
    }
  }

  private void atualizarBotoes() {
    Pokemon selecionado = ativos.getSelectedValue();
    mover.setEnabled(controlador != null && (selecionado != null || !reservados.isSelectionEmpty()));
    mover.setText(selecionado == null ? "MOVER PARA EQUIPE" : "MOVER PARA RESERVA");
    primeiro.setEnabled(selecionado != null && selecionado.estaApto() && ativos.getSelectedIndex() > 0);
    desafiar.setEnabled(equipe != null && equipe.podeIniciarBatalha());
  }

  private void executar(Runnable acao) {
    try {
      acao.run();
    } catch (IllegalStateException erro) {
      mensagem.setText(erro.getMessage());
    }
  }
}
