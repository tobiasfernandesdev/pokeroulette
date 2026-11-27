package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import br.com.roletakanto.model.EscolhaEventoRota;
import br.com.roletakanto.model.Rota;

/** Evento opcional da área. O modelo determina se a visita foi consumida. */
final class PainelEvento extends JPanel {
  private static final long serialVersionUID = 1L;
  private final JLabel titulo = EstiloJogo.faixa("EVENTO DA ROTA", 20);
  private final JLabel mensagem = EstiloJogo.faixa(" ", 13);
  private final JButton descanso = EstiloJogo.criarBotao("DESCANSAR", false);
  private final JButton suprimentos = EstiloJogo.criarBotao("PEGAR SUPRIMENTOS", true);
  private Rota rota;
  private Function<EscolhaEventoRota, String> aoEscolher;
  private Runnable aoVoltar = () -> {};

  PainelEvento() {
    super(new BorderLayout(12, 12));
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(titulo, BorderLayout.NORTH);
    JPanel corpo = EstiloJogo.painel();
    JTextArea texto = new JTextArea("""
        Você encontrou um ponto de descanso na rota.

        Descansar restaura a equipe e a reserva.
        Pegar suprimentos adiciona uma Super Poção à mochila.

        Escolha uma recompensa para esta visita.
        """);
    texto.setEditable(false);
    texto.setFocusable(false);
    texto.setBackground(EstiloJogo.CREME);
    texto.setForeground(EstiloJogo.ROXO_ESCURO);
    texto.setFont(EstiloJogo.fonte(Font.PLAIN, 16));
    texto.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    corpo.add(texto, BorderLayout.CENTER);
    corpo.add(mensagem, BorderLayout.SOUTH);
    add(corpo, BorderLayout.CENTER);

    JPanel botoes = EstiloJogo.painel();
    botoes.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
    JButton voltar = EstiloJogo.criarBotao("VOLTAR", false);
    voltar.addActionListener(evento -> voltar());
    descanso.addActionListener(evento -> escolher(EscolhaEventoRota.DESCANSO));
    suprimentos.addActionListener(evento -> escolher(EscolhaEventoRota.SUPRIMENTOS));
    botoes.add(voltar);
    botoes.add(descanso);
    botoes.add(suprimentos);
    add(botoes, BorderLayout.SOUTH);
  }

  void abrir(Rota rota, Function<EscolhaEventoRota, String> escolher, Runnable voltar) {
    this.rota = rota;
    aoEscolher = escolher;
    aoVoltar = voltar;
    titulo.setText("EVENTO · " + rota.getNome());
    mensagem.setText(" ");
    atualizarBotoes();
  }

  void voltar() {
    aoVoltar.run();
  }

  private void escolher(EscolhaEventoRota escolha) {
    if (!rota.eventoOpcionalDisponivel()) {
      return;
    }
    descanso.setEnabled(false);
    suprimentos.setEnabled(false);
    mensagem.setText(aoEscolher.apply(escolha));
    atualizarBotoes();
  }

  private void atualizarBotoes() {
    boolean disponivel = rota.eventoOpcionalDisponivel();
    descanso.setEnabled(disponivel);
    suprimentos.setEnabled(disponivel);
  }
}
