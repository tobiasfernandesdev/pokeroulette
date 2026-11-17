package br.com.roletakanto.model;
import java.io.Serializable;
/** Agrupa equipe, reserva e mochila do jogador. */
public final class Jogador implements Serializable { private final String nome; private final Equipe equipe; private final Reserva reserva; private final Mochila mochila; public Jogador(String nome,Equipe equipe,Reserva reserva,Mochila mochila){this.nome=nome;this.equipe=equipe;this.reserva=reserva;this.mochila=mochila;} public String getNome(){return nome;} public Equipe getEquipe(){return equipe;} public Reserva getReserva(){return reserva;} public Mochila getMochila(){return mochila;} public boolean possuiPokemonApto(){return equipe.podeIniciarBatalha();} public void restaurarEquipeNoCentro(){equipe.listar().forEach(Pokemon::restaurarParaCentro);} }
