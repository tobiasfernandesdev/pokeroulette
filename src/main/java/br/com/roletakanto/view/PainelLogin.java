package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Identifica o candidato antes de abrir o menu, sem autenticação. */
public final class PainelLogin extends JPanel {
  private static final long serialVersionUID = 1L;

  private final JTextField campoNome = new JTextField(22);
  private final JButton continuar = EstiloJogo.criarBotao("CONTINUAR", true);
  private final JLabel mensagem = EstiloJogo.texto(" ", 12, EstiloJogo.MAGENTA);
  private transient Consumer<String> aoContinuar = nome -> {};

  public PainelLogin() {
    super(new GridBagLayout());
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    EstiloJogo.aplicarAcessibilidade(
        this, "Identificação do candidato", "Informe seu nome para iniciar o jogo.");

    JPanel cartao = EstiloJogo.painel();
    cartao.setLayout(new BorderLayout(12, 16));
    cartao.setPreferredSize(new Dimension(560, 330));

    JLabel titulo = EstiloJogo.faixa("IDENTIFICAÇÃO DO CANDIDATO", 18);
    cartao.add(titulo, BorderLayout.NORTH);

    JPanel formulario = new JPanel(new GridBagLayout());
    formulario.setOpaque(false);
    GridBagConstraints posicao = new GridBagConstraints();
    posicao.gridx = 0;
    posicao.gridy = 0;
    posicao.insets = new Insets(8, 8, 8, 8);
    posicao.anchor = GridBagConstraints.CENTER;
    JLabel instrucao = EstiloJogo.texto("Digite seu nome para entrar em Kanto:", 14,
        EstiloJogo.ROXO_ESCURO);
    formulario.add(instrucao, posicao);

    posicao.gridy++;
    campoNome.setFont(EstiloJogo.fonte(Font.PLAIN, 16));
    campoNome.setForeground(EstiloJogo.ROXO_ESCURO);
    campoNome.setBackground(EstiloJogo.CREME);
    campoNome.setBorder(EstiloJogo.bordaComMargem(
        EstiloJogo.ROXO_ESCURO, EstiloJogo.LAVANDA, 8));
    campoNome.setPreferredSize(new Dimension(390, 52));
    EstiloJogo.aplicarAcessibilidade(campoNome, "Nome do candidato", "Digite seu nome.");
    formulario.add(campoNome, posicao);

    posicao.gridy++;
    mensagem.setHorizontalAlignment(SwingConstants.CENTER);
    formulario.add(mensagem, posicao);
    cartao.add(formulario, BorderLayout.CENTER);

    JPanel rodape = new JPanel(new GridBagLayout());
    rodape.setOpaque(false);
    continuar.setEnabled(false);
    continuar.addActionListener(evento -> confirmarNome());
    rodape.add(continuar);
    cartao.add(rodape, BorderLayout.SOUTH);

    GridBagConstraints centralizar = new GridBagConstraints();
    centralizar.anchor = GridBagConstraints.CENTER;
    add(cartao, centralizar);

    campoNome.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      @Override
      public void insertUpdate(javax.swing.event.DocumentEvent evento) {
        atualizarContinuar();
      }

      @Override
      public void removeUpdate(javax.swing.event.DocumentEvent evento) {
        atualizarContinuar();
      }

      @Override
      public void changedUpdate(javax.swing.event.DocumentEvent evento) {
        atualizarContinuar();
      }
    });
    campoNome.addActionListener(evento -> confirmarNome());
  }

  public void definirAcaoContinuar(Consumer<String> acao) {
    aoContinuar = acao == null ? nome -> {} : acao;
  }

  private void atualizarContinuar() {
    continuar.setEnabled(!campoNome.getText().isBlank());
    mensagem.setText(" ");
  }

  private void confirmarNome() {
    String nome = campoNome.getText().trim();
    if (nome.isEmpty()) {
      mensagem.setText("Informe seu nome para continuar.");
      continuar.setEnabled(false);
      return;
    }
    aoContinuar.accept(nome);
  }
}
