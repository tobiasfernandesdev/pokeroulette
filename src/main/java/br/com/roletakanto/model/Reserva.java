package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Armazena até oito Pokémon fora da equipe. */
public final class Reserva implements Serializable {
  private static final int CAPACIDADE = 6;
  private final List<Pokemon> membros = new ArrayList<>();

  public boolean adicionar(Pokemon pokemon) {
    if (estaCheia() || pokemon == null) return false;
    return membros.add(pokemon);
  }

  public Pokemon remover(String id) {
    return membros.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .map(
            p -> {
              membros.remove(p);
              return p;
            })
        .orElse(null);
  }

  public List<Pokemon> listar() {
    return List.copyOf(membros);
  }

  public Pokemon obterPorId(String id) {
    return membros.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
  }

  public boolean estaCheia() {
    return membros.size() >= CAPACIDADE;
  }

  public int contarAptos() {
    return (int) membros.stream().filter(Pokemon::estaApto).count();
  }
  public Pokemon transferir(String id, Equipe equipe) {
    Pokemon pokemon = obterPorId(id);
    if (pokemon != null && equipe.adicionar(pokemon)) {
      remover(id);
      return pokemon;
    }else{
      return null;
    }
  }
}
