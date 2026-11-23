package br.com.roletakanto.view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import javax.imageio.ImageIO;

/** Carrega imagens locais; poderá usar imagem provisória se o arquivo não existir. */
public final class VisualPokemon {
  private final Map<String, ImageIcon> cache = new HashMap<>();

  public ImageIcon carregarIcone(String idEspecie, int largura, int altura) {
    return carregarRecurso("/assets/pokemon/" + idEspecie + ".png", largura, altura);
  }

  public boolean recursoExiste(String idEspecie) {
    return getClass().getResource("/assets/pokemon/" + idEspecie + ".png") != null;
  }

  public ImageIcon carregarLider(String idLider, int largura, int altura) {
    return carregarRecurso("/assets/leaders/" + idLider + ".png", largura, altura);
  }

  private ImageIcon carregarRecurso(String caminho, int largura, int altura) {
    String chave = caminho + "@" + largura + "x" + altura;
    if (cache.containsKey(chave)) {
      return cache.get(chave);
    }
    URL recurso = getClass().getResource(caminho);
    if (recurso == null || largura <= 0 || altura <= 0) {
      return null;
    }

    try {
      BufferedImage original = ImageIO.read(recurso);
      if (original == null) {
        return null;
      }

      double escala = Math.min((double) largura / original.getWidth(),
          (double) altura / original.getHeight());
      int novaLargura = Math.max(1, (int) Math.round(original.getWidth() * escala));
      int novaAltura = Math.max(1, (int) Math.round(original.getHeight() * escala));
      BufferedImage redimensionada = new BufferedImage(
          novaLargura, novaAltura, BufferedImage.TYPE_INT_ARGB);
      Graphics2D desenho = redimensionada.createGraphics();
      try {
        desenho.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        desenho.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);
        desenho.drawImage(original, 0, 0, novaLargura, novaAltura, null);
      } finally {
        desenho.dispose();
      }
      ImageIcon icone = new ImageIcon(redimensionada);
      cache.put(chave, icone);
      return icone;
    } catch (java.io.IOException erro) {
      System.err.println("Não foi possível carregar o sprite " + caminho + ": " + erro.getMessage());
      return null;
    }
  }
}
