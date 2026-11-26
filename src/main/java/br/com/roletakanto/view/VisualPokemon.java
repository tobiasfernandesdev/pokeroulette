package br.com.roletakanto.view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import javax.imageio.ImageIO;

import br.com.roletakanto.model.TipoResultadoRoleta;

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

  /** O ID do item é o mesmo usado pela mochila: por exemplo, "super_pocao". */
  public ImageIcon carregarItem(String idItem, int largura, int altura) {
    String arquivo = "cura_status".equals(idItem) ? "pocao" : idItem;
    return carregarRecurso("/assets/items/" + arquivo + ".png", largura, altura);
  }

  public ImageIcon carregarCenario(String idCenario, int largura, int altura) {
    return carregarRecurso("/assets/backgrounds/" + idCenario + ".png", largura, altura);
  }

  /** A roleta escolhe a pasta pela categoria; eventos continuam representados por texto. */
  public ImageIcon carregarPremio(TipoResultadoRoleta tipo, String id, int tamanho) {
    if (tipo == null) {
      return null;
    }
    return switch (tipo) {
      case POKEMON -> carregarIcone(id, tamanho, tamanho);
      case ITEM -> carregarItem(id, tamanho, tamanho);
      case EVENTO -> null;
    };
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

      // Alguns sprites vêm numa tela transparente muito maior que o personagem.
      // Tiramos só essa sobra ao desenhar, para a miniatura não ficar minúscula.
      // O PNG no projeto permanece intacto; cenários mantêm suas dimensões originais.
      if (!caminho.startsWith("/assets/backgrounds/")) {
        original = semMargensTransparentes(original);
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

  private static BufferedImage semMargensTransparentes(BufferedImage imagem) {
    int esquerda = imagem.getWidth();
    int topo = imagem.getHeight();
    int direita = -1;
    int base = -1;
    for (int y = 0; y < imagem.getHeight(); y++) {
      for (int x = 0; x < imagem.getWidth(); x++) {
        if ((imagem.getRGB(x, y) >>> 24) != 0) {
          esquerda = Math.min(esquerda, x);
          topo = Math.min(topo, y);
          direita = Math.max(direita, x);
          base = Math.max(base, y);
        }
      }
    }
    return direita < esquerda ? imagem
        : imagem.getSubimage(esquerda, topo, direita - esquerda + 1, base - topo + 1);
  }
}
