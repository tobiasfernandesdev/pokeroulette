package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Insignia;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;

/** Mostra o desfecho do combate antes de o jogador abrir a próxima tela. */
public final class PainelResultado extends JPanel {
  private static final long serialVersionUID = 1L;
  private final VisualPokemon visuais = new VisualPokemon();
  private final JLabel titulo = EstiloJogo.faixa("RESULTADO", 26);
  private final JLabel retrato = new RetratoLider();
  private final JLabel resumo = EstiloJogo.faixa("", 14);
  private final JTextArea mensagens = new JTextArea(5, 40);
  private final JButton continuar = EstiloJogo.criarBotao("CONTINUAR", true);

  public PainelResultado() {
    super(new BorderLayout(12, 12));
    setBackground(EstiloJogo.LAVANDA);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(titulo, BorderLayout.NORTH);

    JPanel corpo = EstiloJogo.painel();
    JPanel retratoCentral = new JPanel(new java.awt.GridBagLayout());
    retratoCentral.setOpaque(false);
    retrato.setPreferredSize(new java.awt.Dimension(220, 200));
    retratoCentral.add(retrato);
    corpo.add(retratoCentral, BorderLayout.CENTER);
    mensagens.setEditable(false);
    mensagens.setLineWrap(true);
    mensagens.setWrapStyleWord(true);
    mensagens.setBackground(EstiloJogo.CREME);
    mensagens.setForeground(EstiloJogo.ROXO_ESCURO);
    mensagens.setFont(EstiloJogo.fonte(Font.PLAIN, 14));
    mensagens.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    corpo.add(new JScrollPane(mensagens), BorderLayout.SOUTH);
    add(corpo, BorderLayout.CENTER);

    JPanel rodape = EstiloJogo.painel();
    rodape.add(resumo, BorderLayout.NORTH);
    JPanel acoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    acoes.setOpaque(false);
    acoes.add(continuar);
    rodape.add(acoes, BorderLayout.SOUTH);
    add(rodape, BorderLayout.SOUTH);
  }

  void aoContinuar(Runnable acao) {
    continuar.addActionListener(evento -> acao.run());
  }

  void apresentar(Partida partida, LiderGinasio lider, ResultadoTurno turno) {
    retrato.setIcon(visuais.carregarLider(lider.getId(), 150, 176));
    retrato.setHorizontalAlignment(SwingConstants.CENTER);
    retrato.setText(retrato.getIcon() == null ? lider.getNome() : "");
    retrato.setToolTipText(lider.getNome());
    mostrarResumo(partida);
    if (turno.resultado() == ResultadoBatalha.DERROTA) {
      mostrarDerrota("A campanha terminou no ginásio de " + lider.getNome() + ".");
    } else {
      mostrarVitoria(lider.getInsignia());
      if (partida.getFase() == FasePartida.VITORIA_FINAL) {
        titulo.setText("VITÓRIA FINAL · CAMPEÃ(O) DE KANTO");
      }
    }
    if (!turno.mensagens().isEmpty()) {
      mensagens.append("\n\n" + String.join("\n", turno.mensagens()));
    }
    mensagens.setCaretPosition(0);
  }

  public void mostrarVitoria(Insignia insignia) {
    titulo.setText("VITÓRIA NO GINÁSIO");
    titulo.setBackground(EstiloJogo.AMARELO);
    mensagens.setText(insignia == null ? "Desafio vencido." : "Insígnia conquistada: " + insignia.nome());
  }

  public void mostrarDerrota(String mensagem) {
    titulo.setText("DERROTA");
    titulo.setBackground(EstiloJogo.ROSA);
    mensagens.setText(mensagem);
  }

  public void mostrarResumo(Partida partida) {
    var insignias = partida.getInsignias();
    String nomes = insignias.stream().map(Insignia::nome).collect(Collectors.joining(" · "));
    resumo.setText(insignias.isEmpty() ? "NENHUMA INSÍGNIA" : nomes);
    continuar.setText(partida.podeContinuar()
        ? "CONTINUAR · " + partida.getRotaAtual().getNome().toUpperCase(java.util.Locale.ROOT)
        : "VOLTAR AO MENU");
  }
}
