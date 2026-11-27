package br.com.roletakanto.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Os cenários são desenhados numa tela de 160 × 90 para manter o traço em pixels. */
enum CenarioPixel {
  CAMPO(0xD6DDF4, 0xB693D5, 0x8573AB, 0xACD7AB),
  CAVERNA(0xB3A5CF, 0x83729E, 0x62527F, 0xD6DDF4),
  BOSQUE(0xD7E2D4, 0x9DA9BE, 0x6B8791, 0xE1CAEA),
  TORRE(0xC9BFDE, 0x9681B0, 0x5F4D7E, 0xFADE99),
  ROCHA(0xE0D8FA, 0xAB92BC, 0x77628B, 0xD5B58C),
  AGUA(0xD6DDF4, 0x92BDDA, 0x577AAB, 0xBCEEE9),
  ELETRICO(0xDAD6EF, 0x9385B0, 0x655684, 0xFADE66),
  PLANTA(0xDFE8D5, 0xA1B9A0, 0x6B8F80, 0xEFBBDA);

  private static final int LARGURA = 160;
  private static final int ALTURA = 90;
  private final BufferedImage arte;

  CenarioPixel(int ceu, int plano, int sombra, int luz) {
    arte = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = arte.createGraphics();
    try {
      Color base = new Color(ceu);
      g.setColor(base);
      g.fillRect(0, 0, LARGURA, ALTURA);
      g.setColor(new Color(plano));
      g.fillRect(0, 58, LARGURA, 32);
      Random pontos = new Random(18 + ordinal());
      for (int i = 0; i < 52; i++) {
        g.setColor(i % 3 == 0 ? new Color(luz) : base.brighter());
        g.fillRect(pontos.nextInt(LARGURA), pontos.nextInt(ALTURA), 1 + i % 3, 1);
      }
      switch (name()) {
        case "CAMPO", "BOSQUE", "PLANTA" -> desenharVegetacao(g, sombra, luz);
        case "CAVERNA", "ROCHA" -> desenharRochas(g, sombra, luz);
        case "TORRE", "ELETRICO" -> desenharInterior(g, sombra, luz);
        case "AGUA" -> desenharAgua(g, sombra, luz);
      }
    } finally {
      g.dispose();
    }
  }

  static CenarioPixel daRota(String id) {
    return switch (id) {
      case "caverna" -> CAVERNA;
      case "rota_2" -> BOSQUE;
      case "torre_pokemon" -> TORRE;
      default -> CAMPO;
    };
  }

  static CenarioPixel doLider(String id) {
    return switch (id) {
      case "misty" -> AGUA;
      case "lt_surge" -> ELETRICO;
      case "erika" -> PLANTA;
      default -> ROCHA;
    };
  }

  private void desenharVegetacao(Graphics2D g, int sombra, int luz) {
    g.setColor(new Color(luz));
    g.fillPolygon(new int[] {0, 26, 58, 86, 118, 160, 160, 0},
        new int[] {45, 23, 48, 28, 12, 43, 60, 60}, 8);
    for (int x = 0; x < LARGURA; x += name().equals("CAMPO") ? 22 : 8) {
      int altura = 7 + (x * 11 % 19);
      g.setColor(new Color(sombra));
      g.fillRect(x + 3, 51 - altura, 3, altura + 12);
      g.fillRect(x, 54 - altura, 9, altura - 3);
      g.setColor(new Color(luz));
      g.fillRect(x + 1, 55 - altura, 2, altura / 2);
      if (name().equals("PLANTA")) {
        g.fillRect(x + 5, 51 - altura, 3, 3);
      }
    }
    g.setColor(new Color(luz));
    g.fillPolygon(new int[] {70, 91, 124, 41}, new int[] {63, 63, 90, 90}, 4);
  }

  private void desenharRochas(Graphics2D g, int sombra, int luz) {
    for (int x = 0; x < LARGURA; x += 16) {
      int altura = 10 + x * 7 % 29;
      g.setColor(new Color(sombra));
      g.fillPolygon(new int[] {x, x + 14, x + 11, x + 4},
          new int[] {90, 90, 58 - altura, 61 - altura}, 4);
      if (name().equals("CAVERNA")) {
        g.fillRect(x, 0, 11, altura / 2);
        g.fillRect(x + 3, altura / 2, 5, 8);
      }
      g.setColor(new Color(luz));
      g.fillRect(x + 6, 67 - altura, 3, 5);
    }
  }

  private void desenharInterior(Graphics2D g, int sombra, int luz) {
    for (int x = 4; x < LARGURA; x += 25) {
      g.setColor(new Color(sombra));
      g.fillRect(x, 0, 4, 63);
      g.fillRect(x + 8, 16, 12, 25);
      g.setColor(new Color(luz));
      if (name().equals("ELETRICO")) {
        g.fillPolygon(new int[] {x + 15, x + 10, x + 14, x + 11, x + 18, x + 14},
            new int[] {19, 30, 30, 38, 26, 26}, 6);
        continue;
      }
      g.fillRect(x + 10, 20, 8, 16);
      g.setColor(new Color(sombra));
      g.fillRect(x + 13, 20, 2, 16);
      g.fillRect(x + 10, 25, 8, 2);
    }
    g.setColor(new Color(sombra));
    for (int y = 62; y < ALTURA; y += 7) {
      g.fillRect(0, y, LARGURA, 1);
    }
  }

  private void desenharAgua(Graphics2D g, int sombra, int luz) {
    g.setColor(new Color(sombra));
    g.fillRect(0, 37, LARGURA, 45);
    for (int y = 43; y < 82; y += 5) {
      for (int x = (y * 7) % 23; x < LARGURA; x += 23) {
        g.setColor(new Color(luz));
        g.fillRect(x, y, 8 + y % 4, 1);
      }
    }
  }

  void pintar(Graphics grafico, int largura, int altura) {
    Graphics2D g = (Graphics2D) grafico.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      double escala = Math.max(largura / (double) LARGURA, altura / (double) ALTURA);
      int w = (int) Math.ceil(LARGURA * escala);
      int h = (int) Math.ceil(ALTURA * escala);
      g.drawImage(arte, (largura - w) / 2, (altura - h) / 2, w, h, null);
    } finally {
      g.dispose();
    }
  }
}
