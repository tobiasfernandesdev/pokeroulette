package br.com.roletakanto.service;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import br.com.roletakanto.model.AcaoBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Pokemon;

public final class SeletorAcaoIA implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CalculadoraDano calculadora;

    public SeletorAcaoIA(){
        this.calculadora = new CalculadoraDano();
    }

    public SeletorAcaoIA(CalculadoraDano calculadora) {
        this.calculadora = calculadora;
    }

    public AcaoBatalha escolher(Batalha batalha) {
        if(batalha == null) {
            return AcaoBatalha.desistir();
        }

        Pokemon ativoIA = batalha.getAtivoLider();
        Pokemon ativoJogador = batalha.obterAtivoJogador();
        
        if(ativoIA == null || !ativoIA.estaApto()) {
            return batalha.getEquipeLider().stream()
            .filter(Pokemon::estaApto)
            .findFirst()
            .map(pokemon -> AcaoBatalha.troca(pokemon.getId()))
            .orElse(AcaoBatalha.desistir());
        }

        Golpe golpeEscolhido = escolherGolpe(ativoIA, ativoJogador);
        if (golpeEscolhido == null) {
            return AcaoBatalha.desistir();
        }

        return AcaoBatalha.golpe(golpeEscolhido.getId());
    }

    public Golpe escolherGolpe(Pokemon ataPokemon, Pokemon defPokemon) {
        if(ataPokemon == null || ataPokemon.getEspecie() == null) {
            return null;
        }

        List<Golpe> golpes = ataPokemon.getEspecie().getGolpes();
        if(golpes == null || golpes.isEmpty()) {
            return null;
        }

        if(defPokemon == null) {
            return golpes.get(0);
        }

        return golpes.stream()
        .max(Comparator.comparingInt(golpe->calculadora.calcular(ataPokemon, defPokemon, golpe)))
        .orElse(golpes.get(0));
    }
}
