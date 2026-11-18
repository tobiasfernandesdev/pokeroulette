package br.com.roletakanto.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** Janela principal que abriga os painéis do jogo. */
public final class JanelaPrincipal extends JFrame {
  private static final long serialVersionUID = 1L;

  private final NavegadorTelas navegador;

  public JanelaPrincipal(NavegadorTelas navegador) {
    super("Roleta Kanto");
    this.navegador = navegador;
    setContentPane(navegador);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public void mostrar() {
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  public void exibirTela(String idTela) {
    navegador.mostrar(idTela);
  }

  public void mostrarMensagem(String mensagem) {
    JOptionPane.showMessageDialog(this, mensagem);
  }
}
