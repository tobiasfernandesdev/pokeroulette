package br.com.roletakanto.view;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;

/** Mantém e alterna painéis por CardLayout. */
public final class NavegadorTelas extends JPanel {
  private static final long serialVersionUID = 1L;

  private final CardLayout layout = new CardLayout();
  private final HashMap<String, JPanel> paineis = new HashMap<>();

  public NavegadorTelas() {
    setLayout(layout);
  }

  public void registrar(String id, JPanel painel) {
    Objects.requireNonNull(id, "O ID da tela é obrigatório.");
    Objects.requireNonNull(painel, "O painel da tela é obrigatório.");
    if (paineis.containsKey(id) || paineis.containsValue(painel)) {
      throw new IllegalArgumentException("O ID ou painel já está registrado: " + id);
    }
    paineis.put(id, painel);
    add(painel, id);
  }

  public void mostrar(String id) {
    if (!paineis.containsKey(id)) {
      throw new IllegalArgumentException("Tela ainda não registrada: " + id);
    }
    layout.show(this, id);
  }

  /** Retorna o ID do painel visível, ou null enquanto nenhuma tela estiver registrada. */
  public String getTelaAtual() {
    for (Map.Entry<String, JPanel> tela : paineis.entrySet()) {
      if (tela.getValue().isVisible()) {
        return tela.getKey();
      }
    }
    return null;
  }
}
