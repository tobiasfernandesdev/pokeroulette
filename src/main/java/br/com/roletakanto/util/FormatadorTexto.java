package br.com.roletakanto.util;
import br.com.roletakanto.model.Pokemon;
public final class FormatadorTexto { private FormatadorTexto(){} public static String formatarHp(Pokemon pokemon){return pokemon.getHpAtual()+"/"+pokemon.getHpMaximo();} public static String formatarProbabilidade(int peso){return peso+"%";} public static String formatarQuantidade(int quantidade){return String.valueOf(quantidade);} }
