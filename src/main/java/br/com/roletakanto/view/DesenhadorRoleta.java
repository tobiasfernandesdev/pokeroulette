package br.com.roletakanto.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.util.List;

import javax.swing.JComponent;

import br.com.roletakanto.model.ResultadoRoleta;
import br.com.roletakanto.model.SegmentoRoleta;
import br.com.roletakanto.model.TipoResultadoRoleta;

/** Desenha os setores ponderados e mantém o ponteiro fixo durante a animação. */
public final class DesenhadorRoleta extends JComponent {
  private static final long serialVersionUID = 1L;
  private static final Color[] CORES_SETORES = {
      EstiloJogo.LAVANDA,
      EstiloJogo.AMARELO,
      EstiloJogo.AZUL_LAVANDA,
      EstiloJogo.ROSA,
      EstiloJogo.LILAS_CLARO,
      new Color(0xB7, 0x91, 0xE2),
      new Color(0xFF, 0xC7, 0x72),
      new Color(0xF5, 0xA6, 0xCF)
  };

  private List<SegmentoRoleta> segmentos = List.of();
  private double angulo;
  private ResultadoRoleta resultado;
  private boolean girando;

  public DesenhadorRoleta() {
    setOpaque(false);
    setPreferredSize(new Dimension(430, 430));
    setMinimumSize(new Dimension(280, 280));
    EstiloJogo.aplicarAcessibilidade(
        this,
        "Roleta de prêmios",
        "Roleta com Pokémon, itens e eventos da rota atual");
  }

  public void definirSegmentos(List<SegmentoRoleta> segmentos) {
    this.segmentos = segmentos == null ? List.of() : List.copyOf(segmentos);
    repaint();
  }

  public void definirAngulo(double graus) {
    angulo = graus;
    repaint();
  }

  public double getAngulo() {
    return angulo;
  }

  public void definirGirando(boolean girando) {
    this.girando = girando;
    repaint();
  }

  public void definirResultado(ResultadoRoleta resultado) {
    this.resultado = resultado;
    repaint();
  }

  /** Calcula um destino cujo setor sorteado termina centralizado sob o ponteiro. */
  public double calcularAnguloDestino(ResultadoRoleta sorteado, int voltas) {
    if (sorteado == null || segmentos.isEmpty()) {
      return angulo + Math.max(1, voltas) * 360.0;
    }

    int pesoTotal = segmentos.stream().mapToInt(SegmentoRoleta::peso).sum();
    double acumulado = 0;
    for (SegmentoRoleta segmento : segmentos) {
      double abertura = 360.0 * segmento.peso() / pesoTotal;
      if (corresponde(segmento, sorteado)) {
        double destinoNormalizado = normalizar(-acumulado - abertura / 2.0);
        double atualNormalizado = normalizar(angulo);
        double distancia = normalizar(destinoNormalizado - atualNormalizado);
        return angulo + Math.max(1, voltas) * 360.0 + distancia;
      }
      acumulado += abertura;
    }
    return angulo + Math.max(1, voltas) * 360.0;
  }

  @Override
  protected void paintComponent(Graphics grafico) {
    super.paintComponent(grafico);
    Graphics2D desenho = (Graphics2D) grafico.create();
    try {
      desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      desenho.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      int margem = 34;
      int diametro = Math.max(80, Math.min(getWidth(), getHeight()) - margem * 2);
      int x = (getWidth() - diametro) / 2;
      int y = (getHeight() - diametro) / 2 + 8;

      desenharSombra(desenho, x, y, diametro);
      desenharSetores(desenho, x, y, diametro);
      desenharAros(desenho, x, y, diametro);
      desenharCentro(desenho, x, y, diametro);
      desenharPonteiro(desenho, getWidth() / 2, y);
    } finally {
      desenho.dispose();
    }
  }

  private void desenharSombra(Graphics2D desenho, int x, int y, int diametro) {
    desenho.setColor(new Color(0x3D, 0x16, 0x57, 70));
    desenho.fillOval(x + 8, y + 10, diametro, diametro);
  }

  private void desenharSetores(Graphics2D desenho, int x, int y, int diametro) {
    if (segmentos.isEmpty()) {
      desenho.setColor(EstiloJogo.LILAS_CLARO);
      desenho.fillOval(x, y, diametro, diametro);
      return;
    }

    int pesoTotal = segmentos.stream().mapToInt(SegmentoRoleta::peso).sum();
    double inicioAcumulado = 0;
    for (int indice = 0; indice < segmentos.size(); indice++) {
      SegmentoRoleta segmento = segmentos.get(indice);
      double abertura = 360.0 * segmento.peso() / pesoTotal;
      double inicio = 90.0 + angulo + inicioAcumulado;
      desenho.setColor(CORES_SETORES[indice % CORES_SETORES.length]);
      desenho.fill(new Arc2D.Double(x, y, diametro, diametro, inicio, abertura, Arc2D.PIE));

      boolean destacado = resultado != null && corresponde(segmento, resultado) && !girando;
      desenho.setColor(destacado ? EstiloJogo.MAGENTA : EstiloJogo.ROXO_ESCURO);
      desenho.setStroke(new BasicStroke(destacado ? 5f : 2f));
      desenho.draw(new Arc2D.Double(x, y, diametro, diametro, inicio, abertura, Arc2D.PIE));

      desenharRotulo(
          desenho,
          rotulo(segmento),
          x + diametro / 2,
          y + diametro / 2,
          diametro,
          inicio + abertura / 2.0,
          abertura);
      inicioAcumulado += abertura;
    }
  }

  private void desenharRotulo(
      Graphics2D desenho,
      String texto,
      int centroX,
      int centroY,
      int diametro,
      double graus,
      double abertura) {
    double radianos = Math.toRadians(graus);
    int raio = (int) (diametro * 0.34);
    int textoX = centroX + (int) Math.round(Math.cos(radianos) * raio);
    int textoY = centroY - (int) Math.round(Math.sin(radianos) * raio);
    String[] linhas = quebrarRotulo(texto);
    int tamanhoBase = Math.max(9, Math.min(13, diametro / 31));
    int tamanho = abertura <= 20 ? Math.max(8, tamanhoBase - 2) : tamanhoBase;
    desenho.setFont(EstiloJogo.fonte(Font.BOLD, tamanho));
    desenho.setColor(EstiloJogo.ROXO_ESCURO);
    FontMetrics medidas = desenho.getFontMetrics();
    int alturaTotal = linhas.length * medidas.getHeight();
    int baseY = textoY - alturaTotal / 2 + medidas.getAscent();
    for (int indice = 0; indice < linhas.length; indice++) {
      int largura = medidas.stringWidth(linhas[indice]);
      desenho.drawString(linhas[indice], textoX - largura / 2, baseY + indice * medidas.getHeight());
    }
  }

  private void desenharAros(Graphics2D desenho, int x, int y, int diametro) {
    desenho.setColor(EstiloJogo.ROXO_ESCURO);
    desenho.setStroke(new BasicStroke(10f));
    desenho.drawOval(x, y, diametro, diametro);
    desenho.setColor(EstiloJogo.MAGENTA);
    desenho.setStroke(new BasicStroke(3f));
    desenho.drawOval(x + 8, y + 8, diametro - 16, diametro - 16);

    int pontos = 24;
    int centroX = x + diametro / 2;
    int centroY = y + diametro / 2;
    int raio = diametro / 2 + 1;
    desenho.setColor(EstiloJogo.AMARELO);
    for (int indice = 0; indice < pontos; indice++) {
      double radianos = Math.toRadians(indice * 360.0 / pontos);
      int px = centroX + (int) (Math.cos(radianos) * raio) - 3;
      int py = centroY - (int) (Math.sin(radianos) * raio) - 3;
      desenho.fillRect(px, py, 7, 7);
    }
  }

  private void desenharCentro(Graphics2D desenho, int x, int y, int diametro) {
    int centro = Math.max(52, diametro / 5);
    int centroX = x + (diametro - centro) / 2;
    int centroY = y + (diametro - centro) / 2;
    desenho.setColor(EstiloJogo.ROXO_ESCURO);
    desenho.fillOval(centroX - 5, centroY - 5, centro + 10, centro + 10);
    desenho.setColor(girando ? EstiloJogo.ROSA : EstiloJogo.AMARELO);
    desenho.fillOval(centroX, centroY, centro, centro);
    desenho.setColor(EstiloJogo.ROXO);
    desenho.setStroke(new BasicStroke(3f));
    desenho.drawOval(centroX, centroY, centro, centro);
    String texto = girando ? "..." : "RK";
    desenho.setFont(EstiloJogo.fonte(Font.BOLD, Math.max(13, centro / 4)));
    FontMetrics medidas = desenho.getFontMetrics();
    desenho.drawString(
        texto,
        centroX + (centro - medidas.stringWidth(texto)) / 2,
        centroY + (centro + medidas.getAscent() - medidas.getDescent()) / 2);
  }

  private void desenharPonteiro(Graphics2D desenho, int centroX, int topo) {
    Polygon ponteiro = new Polygon(
        new int[] {centroX - 18, centroX + 18, centroX},
        new int[] {topo - 20, topo - 20, topo + 18},
        3);
    desenho.setColor(EstiloJogo.ROXO_ESCURO);
    desenho.fillPolygon(ponteiro);
    desenho.setColor(EstiloJogo.AMARELO);
    desenho.setStroke(new BasicStroke(3f));
    desenho.drawPolygon(ponteiro);
  }

  private static boolean corresponde(SegmentoRoleta segmento, ResultadoRoleta sorteado) {
    return segmento.tipo() == sorteado.tipo()
        && segmento.idConteudo().equals(sorteado.idConteudo());
  }

  private static double normalizar(double valor) {
    double normalizado = valor % 360.0;
    return normalizado < 0 ? normalizado + 360.0 : normalizado;
  }

  private static String rotulo(SegmentoRoleta segmento) {
    if (segmento.tipo() == TipoResultadoRoleta.EVENTO) {
      return "EVENTO";
    }
    return switch (segmento.idConteudo()) {
      case "pocao" -> "POÇÃO";
      case "super_pocao" -> "SUPER POÇÃO";
      case "reviver" -> "REVIVER";
      case "pokebola_dourada" -> "POKÉBOLA DOURADA";
      default -> segmento.idConteudo().replace('_', ' ').toUpperCase();
    };
  }

  private static String[] quebrarRotulo(String texto) {
    if (texto.length() <= 9) {
      return new String[] {texto};
    }
    int espaco = texto.indexOf(' ');
    if (espaco < 0) {
      return new String[] {texto};
    }
    return new String[] {texto.substring(0, espaco), texto.substring(espaco + 1)};
  }
}
