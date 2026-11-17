package br.com.roletakanto.model;

/** Dados necessários para aplicar um Item. */
public record ContextoUsoItem(Partida partida, Batalha batalha, Pokemon alvo) {}
