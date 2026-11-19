package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.Objects;

/** Área de roleta associada ao próximo líder[cite: 25, 60]. */
public final class Rota implements Serializable {
  private static final int GIROS_INICIAIS = 2;
  private static final int MAXIMO_BONUS_POR_ROTA = 1;

  private final String id;
  private final String nome;
  private final String idLider;
  private final Roleta roleta;

  private int girosRestantes = GIROS_INICIAIS;
  private int bonusConcedidos = 0;

  public Rota(String id, String nome, Roleta roleta, String idLider) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("O ID da rota é obrigatório.");
    }
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("O nome da rota é obrigatório.");
    }
    if (idLider == null || idLider.isBlank()) {
      throw new IllegalArgumentException("O ID do líder é obrigatório.");
    }
    this.id = id;
    this.nome = nome;
    this.roleta = Objects.requireNonNull(roleta, "A roleta da rota é obrigatória.");
    this.idLider = idLider;
  }

  public String getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public String getIdLider() {
    return idLider;
  }

  public Roleta getRoleta() {
    return roleta;
  }

  public boolean temGiroDisponivel() {
    return girosRestantes > 0;
  }

  public int registrarGiro() {
    if (!temGiroDisponivel()) {
      return -1;
    }
    return --girosRestantes;
  }

  public boolean concederGiroBonus() {
    if (bonusConcedidos >= MAXIMO_BONUS_POR_ROTA) {
      return false;
    }
    bonusConcedidos++;
    girosRestantes++;
    return true;
  }

  public int getGirosRestantes() {
    return girosRestantes;
  }
}