package br.com.roletakanto.view;
import br.com.roletakanto.model.ResultadoRoleta; import javax.swing.*; import java.awt.*;
/** Componente exclusivo de desenho da roleta. */
public final class DesenhadorRoleta extends JComponent { public void definirAngulo(double graus){repaint();} public void definirResultado(ResultadoRoleta resultado){repaint();} @Override protected void paintComponent(Graphics g){super.paintComponent(g);/* TODO */} }
