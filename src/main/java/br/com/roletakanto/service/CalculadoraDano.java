package br.com.roletakanto.service;

import br.com.roletakanto.model.*;

/** Fórmula centralizada de dano e vantagem de tipo. */
public final class CalculadoraDano {
  public int calcular(Pokemon atacante, Pokemon defensor, Golpe golpe) {
    return Math.max(1, golpe.getPotencia());
  }

  public double multiplicador(TipoPokemon atacante, TipoPokemon defensor) {
    return 1.0;
  }
}
