package br.com.roletakanto.view;

import java.awt.Graphics;

import javax.swing.JComponent;

import br.com.roletakanto.model.ResultadoRoleta;

/** Área preparada para desenhar a roleta e exibir o resultado recebido do jogo. */
public final class DesenhadorRoleta extends JComponent {
  private static final long serialVersionUID = 1L;

  public void definirAngulo(double graus) {
    repaint();
  }

  public void definirResultado(ResultadoRoleta resultado) {
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g); /* TODO */
  }
}
