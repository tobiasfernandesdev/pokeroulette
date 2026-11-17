package br.com.roletakanto.view;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/** Mantém e alterna painéis por CardLayout. */
public final class NavegadorTelas extends JPanel {
  private final CardLayout layout = new CardLayout();
  private final Map<String, JPanel> paineis = new HashMap<>();

  public NavegadorTelas() {
    setLayout(layout);
  }

  public void registrar(String id, JPanel painel) {
    paineis.put(id, painel);
    add(painel, id);
  }

  public void mostrar(String id) {
    layout.show(this, id);
  }

  public String getTelaAtual() {
    return "TODO";
  }
}
