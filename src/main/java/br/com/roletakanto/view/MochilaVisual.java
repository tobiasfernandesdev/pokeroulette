package br.com.roletakanto.view;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.util.FormatadorTexto;

/** Diálogo de seleção compartilhado. Quem abriu o diálogo decide como executar a ação. */
final class MochilaVisual {
  private MochilaVisual() {}

  static PilhaItem escolher(Component origem, List<PilhaItem> itens, VisualPokemon visuais) {
    if (itens.isEmpty()) {
      JOptionPane.showMessageDialog(origem, "Nenhum item disponível nesta fase.", "Mochila",
          JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    JPanel lista = EstiloJogo.painel();
    lista.setLayout(new GridLayout(0, 1, 0, 6));
    JOptionPane seletor = new JOptionPane(lista, JOptionPane.PLAIN_MESSAGE,
        JOptionPane.DEFAULT_OPTION, null, new Object[] {"Cancelar"});
    for (PilhaItem pilha : itens) {
      JButton botao = EstiloJogo.criarBotao(pilha.getItem().getNome() + "  ×" + pilha.getQuantidade(), false);
      botao.setIcon(visuais.carregarItem(pilha.getItem().getId(), 24, 24));
      botao.addActionListener(evento -> seletor.setValue(pilha));
      lista.add(botao);
    }
    var dialogo = seletor.createDialog(origem, "Mochila");
    try {
      dialogo.setVisible(true);
      return seletor.getValue() instanceof PilhaItem escolha ? escolha : null;
    } finally {
      dialogo.dispose();
    }
  }

  static Pokemon escolherPokemon(Component origem, String titulo, List<Pokemon> opcoes) {
    if (opcoes.isEmpty()) {
      JOptionPane.showMessageDialog(origem, "Nenhum Pokémon disponível.", titulo,
          JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    String[] nomes = new String[opcoes.size()];
    for (int i = 0; i < nomes.length; i++) {
      Pokemon pokemon = opcoes.get(i);
      nomes[i] = "%d. %s · HP %s".formatted(i + 1, pokemon.getEspecie().getNome(),
          FormatadorTexto.formatarHp(pokemon));
    }
    int indice = JOptionPane.showOptionDialog(origem, "Escolha um Pokémon da equipe.", titulo,
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, nomes, nomes[0]);
    return indice < 0 ? null : opcoes.get(indice);
  }
}
