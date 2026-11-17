package br.com.roletakanto.model;
import java.io.Serializable; import java.util.*;
/** Até três Pokémon ativos. */
public final class Equipe implements Serializable {
    private static final int CAPACIDADE=3; private final List<Pokemon> membros=new ArrayList<>();
    public boolean adicionar(Pokemon pokemon){if(estaCheia()||pokemon==null)return false;return membros.add(pokemon);} public Pokemon remover(String id){return membros.stream().filter(p->p.getId().equals(id)).findFirst().map(p->{membros.remove(p);return p;}).orElse(null);} public boolean trocarPosicao(int origem,int destino){if(origem<0||destino<0||origem>=membros.size()||destino>=membros.size())return false;Collections.swap(membros,origem,destino);return true;} public Pokemon obter(int indice){return membros.get(indice);} public Pokemon obterPorId(String id){return membros.stream().filter(p->p.getId().equals(id)).findFirst().orElse(null);} public List<Pokemon> listar(){return List.copyOf(membros);} public int contarAptos(){return (int)membros.stream().filter(Pokemon::estaApto).count();} public boolean podeIniciarBatalha(){return contarAptos()>0;} public boolean estaCheia(){return membros.size()>=CAPACIDADE;}
}
