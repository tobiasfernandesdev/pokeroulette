package br.com.roletakanto.model;
import java.io.Serializable;
public record Insignia(String id, String nome, String caminhoImagem) implements Serializable { }
