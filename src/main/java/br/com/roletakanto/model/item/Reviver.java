package br.com.roletakanto.model.item;
import br.com.roletakanto.model.*;
/** Reativa um Pokémon desmaiado. */
public final class Reviver extends Item { public Reviver(String id,String nome,int limitePilha){super(id,nome,limitePilha);} public ResultadoUsoItem usar(ContextoUsoItem contexto){if(contexto.alvo()==null)return new ResultadoUsoItem(false,"Selecione um Pokémon");boolean aplicado=contexto.alvo().reviver(contexto.alvo().getHpMaximo()/2);return new ResultadoUsoItem(aplicado,aplicado?"Reviver aplicado":"O Pokémon já está ativo");} }
