package br.com.roletakanto.service;
import br.com.roletakanto.model.*; import java.util.*;
/** Cria dados fixos do jogo. */
public final class CatalogoJogo { public Map<String,EspeciePokemon> carregarEspecies(){return new HashMap<>();} public List<Rota> criarRotas(Random random){return new ArrayList<>();} public List<LiderGinasio> criarLideres(){return new ArrayList<>();} public List<Item> criarItens(){return new ArrayList<>();} public void validarCatalogo(){/* TODO */} }
