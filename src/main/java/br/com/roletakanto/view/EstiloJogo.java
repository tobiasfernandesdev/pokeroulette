package br.com.roletakanto.view;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/** Paleta e acabamentos compartilhados pelas telas Swing do jogo. */
final class EstiloJogo {
  static final Color LAVANDA = new Color(0xCA, 0xA6, 0xED);
  static final Color LILAS_CLARO = new Color(0xE0, 0xD8, 0xFA);
  static final Color MAGENTA = new Color(0xD6, 0x00, 0xD5);
  static final Color AZUL_LAVANDA = new Color(0xD6, 0xDD, 0xF4);
  static final Color AMARELO = new Color(0xFA, 0xDE, 0x66);
  static final Color ROXO = new Color(0x86, 0x15, 0x8A);
  static final Color ROSA = new Color(0xF7, 0x5A, 0x98);
  static final Color ROXO_ESCURO = new Color(0x3D, 0x16, 0x57);
  static final Color CREME = new Color(0xFF, 0xFA, 0xDE);
  static final Color VERDE_HP = new Color(0x62, 0xC9, 0x78);
  static final Color VERMELHO_HP = new Color(0xE8, 0x5D, 0x75);

  private EstiloJogo() {
  }

  static JPanel painel() {
    JPanel painel = new JPanel(new BorderLayout(10, 10));
    painel.setBackground(new Color(0xF4EFFF));
    painel.setBorder(bordaComMargem(ROXO_ESCURO, LILAS_CLARO, 9));
    return painel;
  }

  static JLabel texto(String texto, int tamanho, Color cor) {
    JLabel label = new JLabel(texto);
    label.setFont(fonte(Font.BOLD, tamanho));
    label.setForeground(cor);
    return label;
  }

  static JLabel faixa(String texto, int tamanho) {
    JLabel label = texto(texto, tamanho, ROXO_ESCURO);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(AZUL_LAVANDA);
    label.setBorder(bordaComMargem(ROXO_ESCURO, LAVANDA, 6));
    return label;
  }

  static Font fonte(int estilo, int tamanho) {
    Font pixel = new Font("Press Start 2P", estilo, tamanho);
    if (!"Press Start 2P".equalsIgnoreCase(pixel.getFamily())) {
      pixel = new Font(Font.MONOSPACED, estilo, tamanho);
    }
    return pixel;
  }

  static Border bordaPixel(Color externa, Color interna) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(externa, 3),
        BorderFactory.createLineBorder(interna, 2));
  }

  static Border bordaComMargem(Color externa, Color interna, int margem) {
    return BorderFactory.createCompoundBorder(
        bordaPixel(externa, interna),
        BorderFactory.createEmptyBorder(margem, margem, margem, margem));
  }

  static JButton criarBotao(String texto, boolean principal) {
    Color normal = principal ? AMARELO : LILAS_CLARO;
    Color sobre = principal ? new Color(0xFF, 0xEA, 0x8D) : AZUL_LAVANDA;
    JButton botao = new JButton(texto);
    botao.setFont(fonte(Font.BOLD, principal ? 16 : 13));
    botao.setForeground(ROXO_ESCURO);
    botao.setBackground(normal);
    botao.setBorder(bordaComMargem(ROXO_ESCURO, principal ? ROSA : LAVANDA, 8));
    botao.setFocusPainted(false);
    botao.setContentAreaFilled(true);
    botao.setOpaque(true);
    botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    botao.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent evento) {
        if (botao.isEnabled()) {
          botao.setBackground(sobre);
        }
      }

      @Override
      public void mouseExited(MouseEvent evento) {
        botao.setBackground(normal);
      }
    });
    return botao;
  }

  static void aplicarAcessibilidade(JComponent componente, String nome, String descricao) {
    if (componente.getAccessibleContext() != null) {
      componente.getAccessibleContext().setAccessibleName(nome);
      componente.getAccessibleContext().setAccessibleDescription(descricao);
    }
  }
}
