package br.com.roletakanto.model;
import java.io.Serializable; import java.util.*;
/** Define um líder e sua equipe de referência. */
public final class LiderGinasio implements Serializable { private final String id,nome; private final List<EspeciePokemon> equipe; private final Insignia insignia; public LiderGinasio(String id,String nome,List<EspeciePokemon> equipe,Insignia insignia){this.id=id;this.nome=nome;this.equipe=List.copyOf(equipe);this.insignia=insignia;} public String getId(){return id;} public String getNome(){return nome;} public List<EspeciePokemon> getEquipe(){return equipe;} public Insignia getInsignia(){return insignia;} public List<Pokemon> criarEquipeDeBatalha(){return equipe.stream().map(EspeciePokemon::criarInstancia).toList();} }
