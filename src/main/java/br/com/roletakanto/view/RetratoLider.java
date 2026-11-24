package br.com.roletakanto.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/** Primeiro pinta a arena; depois o JLabel coloca o líder por cima. */
final class RetratoLider extends JLabel {
  private static final long serialVersionUID = 1L;
  private final ImageIcon arena = new VisualPokemon().carregarCenario("arena", 753, 500);

  RetratoLider() {
    super("?", SwingConstants.CENTER);
    setToolTipText("Próximo desafio no ginásio");
  }

  @Override
  protected void paintComponent(Graphics grafico) {
    if (arena != null) {
      Graphics2D desenho = (Graphics2D) grafico.create();
      try {
        desenho.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // O arquivo também tem molduras de interface. Exibimos só o campo da arena.
        // O recorte preenche o retrato sem esticar a proporção do cenário.
        double escala = Math.max(getWidth() / 430.0, getHeight() / 290.0);
        int largura = (int) Math.ceil(430 * escala);
        int altura = (int) Math.ceil(290 * escala);
        int x = (getWidth() - largura) / 2;
        int y = (getHeight() - altura) / 2;
        desenho.drawImage(arena.getImage(), x, y, x + largura, y + altura,
            155, 95, 585, 385, this);
      } finally {
        desenho.dispose();
      }
    }
    super.paintComponent(grafico);
  }
}
