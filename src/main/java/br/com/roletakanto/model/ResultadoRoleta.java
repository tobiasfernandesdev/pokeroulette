package br.com.roletakanto.model;
import java.io.Serializable;
/** Resultado de um giro pronto para ser aplicado pelo controlador. */
public record ResultadoRoleta(TipoResultadoRoleta tipo, String idConteudo, String mensagem) implements Serializable { public static ResultadoRoleta pokemon(String id){return new ResultadoRoleta(TipoResultadoRoleta.POKEMON,id,"Pokémon encontrado");} public static ResultadoRoleta item(String id){return new ResultadoRoleta(TipoResultadoRoleta.ITEM,id,"Item recebido");} public static ResultadoRoleta evento(TipoEvento evento){return new ResultadoRoleta(TipoResultadoRoleta.EVENTO,evento.name(),"Evento da rota");} }
