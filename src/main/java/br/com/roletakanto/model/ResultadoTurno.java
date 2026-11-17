package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.List;

public record ResultadoTurno(
    List<String> mensagens, ResultadoBatalha resultado, String idPokemonAtivo)
    implements Serializable {
  public ResultadoTurno {
    mensagens = List.copyOf(mensagens);
  }
}
