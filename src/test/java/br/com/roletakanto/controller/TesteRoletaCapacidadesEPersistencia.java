package br.com.roletakanto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.Mochila;
import br.com.roletakanto.model.Roleta;
import br.com.roletakanto.model.Rota;
import br.com.roletakanto.model.SegmentoRoleta;
import br.com.roletakanto.model.TipoResultadoRoleta;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CatalogoJogo;

class TesteRoletaCapacidadesEPersistencia {

  @TempDir Path pastaTemp;

  private CatalogoJogo catalogo;
  private RepositorioPartidaArquivo repositorio;
  private ControladorPartida controladorPartida;

  @BeforeEach
  void preparar() {
    catalogo = new CatalogoJogo();
    repositorio = new RepositorioPartidaArquivo(pastaTemp.resolve("partida.dat"));
    controladorPartida = new ControladorPartida(catalogo, repositorio, new Random(123L));
    controladorPartida.novaPartida("bulbasaur");
  }

  @Test
  void deveConverterPokemonEmPocaoQuandoEquipeEReservaEstiveremCheias() {
    var partida = controladorPartida.getPartida();
    var especies = catalogo.carregarEspecies();

    // Preenche o restante da equipe (capacidade 3; já tem 1 inicial)
    partida.getJogador().getEquipe().adicionar(especies.get("pidgey").criarInstancia());
    partida.getJogador().getEquipe().adicionar(especies.get("rattata").criarInstancia());

    // Preenche toda a reserva (capacidade 6)
    List<String> idsReserva =
        List.of("pikachu", "zubat", "geodude", "sandshrew", "oddish", "abra");
    for (String id : idsReserva) {
      assertTrue(partida.getJogador().getReserva().adicionar(especies.get(id).criarInstancia()));
    }

    int pocoesAntes =
        partida
            .getJogador()
            .getMochila()
            .obterPilhaPorId(CatalogoJogo.ID_POCAO)
            .getQuantidade();

    // Substitui a roleta da rota atual por um segmento fixo de Pokémon para testar a conversão
    Rota rotaAtual = partida.getRotaAtual();
    Roleta roletaFixaPokemon =
        new Roleta(
            List.of(new SegmentoRoleta(TipoResultadoRoleta.POKEMON, "vulpix", 100)),
            new Random(1L));
    Rota rotaTeste =
        new Rota(rotaAtual.getId(), rotaAtual.getNome(), roletaFixaPokemon, rotaAtual.getIdLider());
    partida.getRotas().set(partida.getIndiceRotaAtual(), rotaTeste);

    String saida = controladorPartida.girarRoleta();
    int pocoesDepois =
        partida
            .getJogador()
            .getMochila()
            .obterPilhaPorId(CatalogoJogo.ID_POCAO)
            .getQuantidade();

    assertTrue(saida.toLowerCase().contains("pocao") || saida.toLowerCase().contains("poção"));
    assertEquals(pocoesAntes + 1, pocoesDepois);
  }

  @Test
  void deveRespeitarLimiteDeGiroBonusComPokebolaDourada() {
    var partida = controladorPartida.getPartida();
    Item dourada =
        catalogo.criarItens().stream()
            .filter(i -> i.getId().equals(CatalogoJogo.ID_POKEBOLA_DOURADA))
            .findFirst()
            .orElseThrow();
    partida.getJogador().getMochila().adicionar(dourada, 5);

    int usosConcedidos = 0;
    for (int i = 0; i < 5; i++) {
      String msg = controladorPartida.usarGiroBonus();
      if (msg.toLowerCase().contains("giro bonus concedido")) {
        usosConcedidos++;
      }
    }

    assertEquals(1, usosConcedidos);
  }

  @Test
  void deveRespeitarCapacidadesDaMochilaESalvarApagarPartida() throws Exception {
    Mochila mochila = controladorPartida.getPartida().getJogador().getMochila();
    Item pocao =
        catalogo.criarItens().stream()
            .filter(i -> i.getId().equals(CatalogoJogo.ID_POCAO))
            .findFirst()
            .orElseThrow();

    // Adiciona até atingir o limite da pilha (ex.: 99) e tenta exceder
    int espacoRestante =
        pocao.getLimitePilha()
            - mochila.obterPilhaPorId(CatalogoJogo.ID_POCAO).getQuantidade();
    if (espacoRestante > 0) {
      assertTrue(mochila.adicionar(pocao, espacoRestante));
    }
    assertFalse(mochila.adicionar(pocao, 1));

    assertFalse(repositorio.existeSalvamento());
    controladorPartida.salvarPartida();
    assertTrue(repositorio.existeSalvamento());

    controladorPartida.continuarPartida();
    assertEquals("bulbasaur", controladorPartida.getPartida().getJogador().getEquipe().listar().get(0).getEspecie().getId());

    repositorio.apagar();
    assertFalse(repositorio.existeSalvamento());
  }
}