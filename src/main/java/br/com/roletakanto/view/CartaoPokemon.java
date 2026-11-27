package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.StatusPokemon;
import br.com.roletakanto.util.FormatadorTexto;

/** O mesmo cartão mostra nome, tipo, condição e HP na rota, equipe e batalha. */
final class CartaoPokemon extends JPanel {
  private static final long serialVersionUID = 1L;
  private final JLabel sprite = new JLabel("◆", SwingConstants.CENTER);
  private final JLabel nome = EstiloJogo.texto("", 13, EstiloJogo.ROXO_ESCURO);
  private final JLabel tipo = EstiloJogo.texto("", 10, EstiloJogo.ROXO);
  private final JProgressBar hp = new JProgressBar();
  private final JPanel dados = new JPanel();
  private final VisualPokemon visuais;
  private final int tamanhoSprite;

  CartaoPokemon(VisualPokemon visuais, int tamanhoSprite) {
    super(new BorderLayout(8, 4));
    this.visuais = visuais;
    this.tamanhoSprite = tamanhoSprite;
    setBackground(EstiloJogo.CREME);
    setBorder(EstiloJogo.bordaComMargem(EstiloJogo.ROXO, EstiloJogo.LAVANDA, 5));
    sprite.setPreferredSize(new Dimension(tamanhoSprite + 6, tamanhoSprite));
    sprite.setForeground(EstiloJogo.ROXO);
    add(sprite, BorderLayout.WEST);

    dados.setOpaque(false);
    dados.setLayout(new BoxLayout(dados, BoxLayout.Y_AXIS));
    hp.setStringPainted(true);
    hp.setFont(EstiloJogo.fonte(Font.BOLD, 10));
    hp.setBackground(EstiloJogo.AZUL_LAVANDA);
    hp.setAlignmentX(LEFT_ALIGNMENT);
    hp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
    nome.setAlignmentX(LEFT_ALIGNMENT);
    tipo.setAlignmentX(LEFT_ALIGNMENT);
    nome.setMaximumSize(new Dimension(Integer.MAX_VALUE, nome.getFontMetrics(nome.getFont()).getHeight()));
    tipo.setMaximumSize(new Dimension(Integer.MAX_VALUE, tipo.getFontMetrics(tipo.getFont()).getHeight()));
    dados.add(nome);
    dados.add(tipo);
    dados.add(Box.createVerticalStrut(4));
    dados.add(hp);
    add(dados, BorderLayout.CENTER);
  }

  @Override
  public void doLayout() {
    super.doLayout();
    dados.doLayout();
  }

  void apresentar(Pokemon pokemon) {
    hp.setVisible(pokemon != null);
    if (pokemon == null) {
      nome.setText("—");
      tipo.setText("");
      sprite.setIcon(null);
      sprite.setText("");
      return;
    }
    nome.setText(pokemon.getEspecie().getNome().toUpperCase(java.util.Locale.ROOT));
    String condicao = pokemon.estaApto()
        ? (pokemon.getStatus() == StatusPokemon.NENHUM ? "" : " · " + pokemon.getStatus().getDescricao())
        : " · desmaiado";
    tipo.setText(pokemon.getEspecie().getTipo().name() + condicao);
    ImageIcon imagem = visuais.carregarIcone(pokemon.getEspecie().getId(), tamanhoSprite, tamanhoSprite);
    sprite.setIcon(imagem);
    sprite.setText(imagem == null ? "◆" : "");
    hp.setMaximum(pokemon.getHpMaximo());
    hp.setValue(pokemon.getHpAtual());
    hp.setString("HP " + FormatadorTexto.formatarHp(pokemon));
    hp.setForeground(pokemon.getHpAtual() * 4 > pokemon.getHpMaximo()
        ? EstiloJogo.VERDE_HP : EstiloJogo.VERMELHO_HP);
  }
}
