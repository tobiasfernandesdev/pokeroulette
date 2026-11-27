package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.BiFunction;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;

import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.StatusPokemon;
import br.com.roletakanto.service.CatalogoJogo;
import br.com.roletakanto.util.FormatadorTexto;

/** Um cartão interno para consultar a mochila e aplicar um efeito por clique. */
final class MochilaVisual extends JPanel {
  private static final long serialVersionUID = 1L;
  private final VisualPokemon visuais = new VisualPokemon();
  private final JList<PilhaItem> itens = new JList<>();
  private final JLabel nome = EstiloJogo.faixa("MOCHILA VAZIA", 17);
  private final JTextArea descricao = new JTextArea();
  private final JComboBox<Pokemon> alvos = new JComboBox<>();
  private final JLabel mensagem = EstiloJogo.faixa(" ", 12);
  private final JButton usar = EstiloJogo.criarBotao("USAR", true);
  private Partida partida;
  private FasePartida fase;
  private Runnable aoVoltar = () -> {};
  private BiFunction<String, Pokemon, String> aoUsar;
  private boolean atualizando;
  private boolean emUso;
  private final Timer intervaloUso = new Timer(300, evento -> {
    emUso = false;
    atualizarDetalhes();
  });

  MochilaVisual() {
    super(new BorderLayout(12, 12));
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(EstiloJogo.faixa("MOCHILA", 22), BorderLayout.NORTH);

    JPanel centro = new JPanel(new GridLayout(1, 2, 12, 0));
    centro.setOpaque(false);
    JPanel inventario = EstiloJogo.painel();
    inventario.add(EstiloJogo.faixa("ITENS", 14), BorderLayout.NORTH);
    itens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    itens.setFixedCellHeight(53);
    itens.setBackground(EstiloJogo.CREME);
    itens.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
          boolean selecionado, boolean foco) {
        super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
        if (valor instanceof PilhaItem pilha) {
          setText(pilha.getItem().getNome() + "  ×" + pilha.getQuantidade());
          setIcon(visuais.carregarItem(pilha.getItem().getId(), 32, 32));
          setFont(EstiloJogo.fonte(Font.BOLD, 13));
          setBackground(selecionado ? EstiloJogo.AZUL_LAVANDA : EstiloJogo.CREME);
          setForeground(EstiloJogo.ROXO_ESCURO);
          setBorder(EstiloJogo.bordaComMargem(EstiloJogo.ROXO, EstiloJogo.LAVANDA, 4));
        }
        return this;
      }
    });
    inventario.add(new JScrollPane(itens), BorderLayout.CENTER);
    centro.add(inventario);

    JPanel detalhes = EstiloJogo.painel();
    detalhes.add(nome, BorderLayout.NORTH);
    descricao.setEditable(false);
    descricao.setLineWrap(true);
    descricao.setWrapStyleWord(true);
    descricao.setBackground(EstiloJogo.CREME);
    descricao.setForeground(EstiloJogo.ROXO_ESCURO);
    descricao.setFont(EstiloJogo.fonte(Font.PLAIN, 14));
    descricao.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    detalhes.add(new JScrollPane(descricao), BorderLayout.CENTER);

    JPanel escolher = new JPanel(new BorderLayout(8, 6));
    escolher.setOpaque(false);
    JLabel rotulo = EstiloJogo.texto("Alvo", 13, EstiloJogo.ROXO_ESCURO);
    rotulo.setLabelFor(alvos);
    escolher.add(rotulo, BorderLayout.WEST);
    alvos.setFont(EstiloJogo.fonte(Font.PLAIN, 13));
    alvos.setPreferredSize(new Dimension(200, 40));
    alvos.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
          boolean selecionado, boolean foco) {
        super.getListCellRendererComponent(lista, valor, indice, selecionado, foco);
        if (valor instanceof Pokemon pokemon) {
          setText(pokemon.getEspecie().getNome() + " · HP " + FormatadorTexto.formatarHp(pokemon));
          setIcon(visuais.carregarIcone(pokemon.getEspecie().getId(), 26, 26));
        }
        return this;
      }
    });
    escolher.add(alvos, BorderLayout.CENTER);
    detalhes.add(escolher, BorderLayout.SOUTH);
    centro.add(detalhes);
    add(centro, BorderLayout.CENTER);

    JPanel rodape = EstiloJogo.painel();
    rodape.add(mensagem, BorderLayout.NORTH);
    JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    botoes.setOpaque(false);
    JButton voltar = EstiloJogo.criarBotao("VOLTAR", false);
    voltar.addActionListener(evento -> voltar());
    usar.addActionListener(evento -> usarSelecionado());
    botoes.add(voltar);
    botoes.add(usar);
    rodape.add(botoes, BorderLayout.SOUTH);
    add(rodape, BorderLayout.SOUTH);
    itens.addListSelectionListener(evento -> {
      if (!evento.getValueIsAdjusting() && !atualizando) {
        mensagem.setText(" ");
        atualizarDetalhes();
      }
    });
    usar.setEnabled(false);
    intervaloUso.setRepeats(false);
  }

  void abrir(Partida partida, BiFunction<String, Pokemon, String> usarItem, Runnable voltar) {
    intervaloUso.stop();
    emUso = false;
    this.partida = partida;
    fase = partida.getFase();
    aoUsar = usarItem;
    aoVoltar = voltar;
    mensagem.setText(" ");
    carregarItens(null);
  }

  void voltar() {
    aoVoltar.run();
  }

  private void carregarItens(String id) {
    atualizando = true;
    List<PilhaItem> pilhas = partida.getJogador().getMochila().listar();
    itens.setListData(pilhas.toArray(PilhaItem[]::new));
    for (int i = 0; i < pilhas.size(); i++) {
      if (pilhas.get(i).getItem().getId().equals(id)) {
        itens.setSelectedIndex(i);
        break;
      }
    }
    if (itens.isSelectionEmpty() && !pilhas.isEmpty()) {
      itens.setSelectedIndex(0);
    }
    atualizando = false;
    atualizarDetalhes();
  }

  private void atualizarDetalhes() {
    PilhaItem selecionado = itens.getSelectedValue();
    alvos.removeAllItems();
    alvos.setEnabled(false);
    usar.setEnabled(false);
    if (selecionado == null) {
      nome.setText("MOCHILA VAZIA");
      nome.setIcon(null);
      descricao.setText("Os itens recebidos na roleta aparecem aqui.");
      return;
    }

    var item = selecionado.getItem();
    String id = item.getId();
    nome.setText(item.getNome() + "  ×" + selecionado.getQuantidade());
    nome.setIcon(visuais.carregarItem(id, 38, 38));
    boolean permitido = fase == partida.getFase() && item.podeUsarEm(fase);
    boolean alvoRota = CatalogoJogo.ID_POKEBOLA_DOURADA.equals(id);
    if (permitido && !alvoRota) {
      alvosPossiveis(id).forEach(alvos::addItem);
      alvos.setEnabled(fase == FasePartida.ROTA && alvos.getItemCount() > 0);
    }
    String contexto = alvoRota ? "Alvo: roleta da área atual."
        : fase == FasePartida.BATALHA ? "Em batalha, o alvo é definido pelo combate."
        : "Na rota, escolha um Pokémon da equipe.";
    if (!permitido) {
      contexto += "\nUso indisponível nesta fase.";
    } else if (!alvoRota && alvos.getItemCount() == 0) {
      contexto += "\nNenhum alvo pode receber este item agora.";
    }
    descricao.setText(efeito(id) + "\n\n" + contexto);
    descricao.setCaretPosition(0);
    usar.setEnabled(!emUso && permitido && (alvoRota || alvos.getItemCount() > 0));
  }

  private List<Pokemon> alvosPossiveis(String id) {
    if (fase == FasePartida.BATALHA) {
      Pokemon alvo = partida.getBatalhaAtual().alvoDoItem(id);
      return alvo != null && servePara(id, alvo) ? List.of(alvo) : List.of();
    }
    return partida.getJogador().getEquipe().listar().stream()
        .filter(pokemon -> servePara(id, pokemon)).toList();
  }

  private static boolean servePara(String id, Pokemon pokemon) {
    return switch (id) {
      case CatalogoJogo.ID_REVIVER -> !pokemon.estaApto();
      case CatalogoJogo.ID_CURA_STATUS -> pokemon.estaApto() && pokemon.getStatus() != StatusPokemon.NENHUM;
      case CatalogoJogo.ID_POCAO, CatalogoJogo.ID_SUPER_POCAO ->
          pokemon.estaApto() && pokemon.getHpAtual() < pokemon.getHpMaximo();
      default -> pokemon.estaApto();
    };
  }

  private static String efeito(String id) {
    return switch (id) {
      case CatalogoJogo.ID_POCAO -> "Recupera até 25 HP de um Pokémon apto com HP abaixo do máximo.";
      case CatalogoJogo.ID_SUPER_POCAO -> "Recupera até 50 HP de um Pokémon apto com HP abaixo do máximo.";
      case CatalogoJogo.ID_REVIVER -> "Revive um Pokémon desmaiado com metade do HP máximo.";
      case CatalogoJogo.ID_CURA_STATUS -> "Remove envenenamento ou paralisia de um Pokémon apto.";
      case CatalogoJogo.ID_POKEBOLA_DOURADA -> "Concede um giro extra. Pode ser usada uma vez por rota, durante a exploração.";
      default -> "Consulte as regras do item antes de usar.";
    };
  }

  private void usarSelecionado() {
    PilhaItem pilha = itens.getSelectedValue();
    if (pilha == null || !usar.isEnabled() || partida.getFase() != fase) {
      return;
    }
    usar.setEnabled(false);
    emUso = true;
    String id = pilha.getItem().getId();
    try {
      mensagem.setText(aoUsar.apply(id, (Pokemon) alvos.getSelectedItem()));
      carregarItens(id);
    } finally {
      intervaloUso.restart();
    }
  }
}
