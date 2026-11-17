package br.com.roletakanto.util;

public final class Validacao {
  private Validacao() {}

  public static void exigirNaoNulo(Object valor, String campo) {
    if (valor == null) throw new IllegalArgumentException(campo + " não pode ser nulo");
  }

  public static void exigirIntervalo(int valor, int minimo, int maximo, String campo) {
    if (valor < minimo || valor > maximo)
      throw new IllegalArgumentException(campo + " fora do intervalo");
  }

  public static void exigirTexto(String texto, String campo) {
    if (texto == null || texto.isBlank())
      throw new IllegalArgumentException(campo + " não pode ser vazio");
  }
}
