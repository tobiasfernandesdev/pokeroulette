package br.com.roletakanto.service;
import br.com.roletakanto.model.*;
/** Estratégia inicial simples da IA. */
public final class SeletorAcaoIA { public AcaoBatalha escolher(Batalha batalha){return AcaoBatalha.desistir();} public Golpe escolherGolpe(Pokemon atacante,Pokemon defensor){return atacante.getEspecie().getGolpes().get(0);} }
