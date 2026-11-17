package br.com.roletakanto.model.item;
import br.com.roletakanto.model.*;
/** Concede giro bônus na rota atual. */
public final class PokebolaDourada extends Item { public PokebolaDourada(String id,String nome,int limitePilha){super(id,nome,limitePilha);} public ResultadoUsoItem usar(ContextoUsoItem contexto){return new ResultadoUsoItem(true,"TODO: giro bônus concedido");} }
