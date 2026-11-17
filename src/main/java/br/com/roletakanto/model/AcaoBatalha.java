package br.com.roletakanto.model;
import java.io.Serializable;
/** Representa uma ação sem acoplar o motor de batalha à interface. */
public record AcaoBatalha(TipoAcaoBatalha tipo, String idAlvo) implements Serializable { public static AcaoBatalha golpe(String id){return new AcaoBatalha(TipoAcaoBatalha.GOLPE,id);} public static AcaoBatalha item(String id){return new AcaoBatalha(TipoAcaoBatalha.ITEM,id);} public static AcaoBatalha troca(String id){return new AcaoBatalha(TipoAcaoBatalha.TROCA,id);} public static AcaoBatalha desistir(){return new AcaoBatalha(TipoAcaoBatalha.DESISTIR,null);} }
