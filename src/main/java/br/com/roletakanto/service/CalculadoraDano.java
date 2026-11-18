package br.com.roletakanto.service;

import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.TipoPokemon;

public final class CalculadoraDano {

  public int calcular(Pokemon atacante, Pokemon defensor, Golpe golpe) {
    if (atacante == null || defensor == null || golpe == null) {
      return 0;
    }

    int ataque = atacante.getEspecie().getAtaque();
    int defesa = Math.max(1, defensor.getEspecie().getDefesa());
    int potencia = golpe.getPotencia();

    double stab = (golpe.getTipo() == atacante.getEspecie().getTipo()) ? 1.5 : 1.0;
    double multTipo = multiplicador(golpe.getTipo(), defensor.getEspecie().getTipo());

    double danoBase = (((double) potencia * ataque) / defesa) * 0.5;
    int danoFinal = (int) Math.round(danoBase * stab * multTipo);

    return Math.max(1, danoFinal);
  }

  public double multiplicador(TipoPokemon atacante, TipoPokemon defensor) {
    if (atacante == null || defensor == null) {
      return 1.0;
    }

    return switch (atacante) {
      case FOGO -> switch (defensor) {
        case PLANTA -> 2.0;
        case FOGO, AGUA, ROCHA -> 0.5;
        default -> 1.0;
      };
      case AGUA -> switch (defensor) {
        case FOGO, ROCHA -> 2.0;
        case AGUA, PLANTA -> 0.5;
        default -> 1.0;
      };
      case PLANTA -> switch (defensor) {
        case AGUA, ROCHA -> 2.0;
        case FOGO, PLANTA, VOADOR -> 0.5;
        default -> 1.0;
      };
      case ELETRICO -> switch (defensor) {
        case AGUA, VOADOR -> 2.0;
        case PLANTA, ELETRICO -> 0.5;
        default -> 1.0;
      };
      case ROCHA -> switch (defensor) {
        case FOGO, VOADOR -> 2.0;
        default -> 1.0;
      };
      case VOADOR -> switch (defensor) {
        case PLANTA -> 2.0;
        case ELETRICO, ROCHA -> 0.5;
        default -> 1.0;
      };
      case NORMAL -> switch (defensor) {
        case ROCHA -> 0.5;
        default -> 1.0;
      };
    };
  }
}