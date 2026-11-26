package br.com.roletakanto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.roletakanto.model.AcaoBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.EscolhaEventoRota;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.StatusPokemon;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CalculadoraDano;
import br.com.roletakanto.service.CatalogoJogo;
import br.com.roletakanto.service.SeletorAcaoIA;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TesteStatusEEventosRota {
  @TempDir Path pastaTemp;

  @Test
  void golpeDePlantaPodeEnvenenarEEnvenenamentoCausaDanoNoFimDoTurno() {
    ControladorPartida controlador = criarControlador("bulbasaur");
    controlador.confirmarBatalha();
    var partida = controlador.getPartida();
    Batalha batalha =
        new Batalha(
            partida,
            partida.getJogador(),
            partida.getLiderAtual(),
            new CalculadoraDano(),
            new SeletorAcaoIA(),
            new Random() {
              @Override
              public int nextInt(int bound) {
                return 0;
              }
            });
    partida.definirBatalhaAtual(batalha);

    var turno = batalha.executarAcao(AcaoBatalha.golpe("ataque_planta"));

    assertEquals(StatusPokemon.ENVENENADO, batalha.getAtivoLider().getStatus());
    assertTrue(
        turno.mensagens().stream()
            .anyMatch(mensagem -> mensagem.contains("dano por envenenamento")));
  }

  @Test
  void paralisiaPodeImpedirUmaAcao() {
    ControladorPartida controlador = criarControlador("bulbasaur");
    controlador.confirmarBatalha();
    var partida = controlador.getPartida();
    Batalha batalha =
        new Batalha(
            partida,
            partida.getJogador(),
            partida.getLiderAtual(),
            new CalculadoraDano(),
            new SeletorAcaoIA(),
            new Random() {
              @Override
              public int nextInt(int bound) {
                return 0;
              }
            });
    partida.definirBatalhaAtual(batalha);
    Pokemon ativo = batalha.obterAtivoJogador();
    ativo.aplicarStatus(StatusPokemon.PARALISADO);
    int hpLider = batalha.getAtivoLider().getHpAtual();

    var turno = batalha.executarAcao(AcaoBatalha.golpe("investida"));

    assertEquals(hpLider, batalha.getAtivoLider().getHpAtual());
    assertTrue(
        turno.mensagens().stream()
            .anyMatch(mensagem -> mensagem.contains("não conseguiu agir")));
  }

  @Test
  void curaDeStatusConsomeItemSomenteQuandoRemoveUmaCondicao() {
    ControladorPartida controlador = criarControlador("bulbasaur");
    Pokemon inicial = controlador.getPartida().getJogador().getEquipe().obter(0);
    var cura =
        new CatalogoJogo().criarItens().stream()
            .filter(item -> item.getId().equals(CatalogoJogo.ID_CURA_STATUS))
            .findFirst()
            .orElseThrow();
    controlador.getPartida().getJogador().getMochila().adicionar(cura, 1);
    inicial.aplicarStatus(StatusPokemon.ENVENENADO);

    var resultado =
        controlador
            .getPartida()
            .getJogador()
            .getMochila()
            .usar(
                CatalogoJogo.ID_CURA_STATUS,
                new ContextoUsoItem(controlador.getPartida(), null, inicial));

    assertTrue(resultado.sucesso());
    assertEquals(StatusPokemon.NENHUM, inicial.getStatus());
    assertFalse(
        controlador
            .getPartida()
            .getJogador()
            .getMochila()
            .temQuantidade(CatalogoJogo.ID_CURA_STATUS, 1));
  }

  @Test
  void eventoOpcionalEntregaRecompensaUmaVezPorRota() {
    ControladorPartida controlador = criarControlador("bulbasaur");
    var partida = controlador.getPartida();

    String resultado = controlador.usarEventoOpcional(EscolhaEventoRota.SUPRIMENTOS);
    String repetido = controlador.usarEventoOpcional(EscolhaEventoRota.DESCANSO);

    assertTrue(resultado.contains("Super Pocao"));
    assertTrue(repetido.contains("ja foi utilizado"));
    assertEquals(
        1,
        partida
            .getJogador()
            .getMochila()
            .obterPilhaPorId(CatalogoJogo.ID_SUPER_POCAO)
            .getQuantidade());
    assertFalse(partida.getRotaAtual().eventoOpcionalDisponivel());
  }

  @Test
  void eventoDeDescansoRecuperaEquipeEReserva() {
    ControladorPartida controlador = criarControlador("bulbasaur");
    var partida = controlador.getPartida();
    Pokemon daEquipe = partida.getJogador().getEquipe().obter(0);
    Pokemon daReserva = new CatalogoJogo().carregarEspecies().get("pidgey").criarInstancia();
    partida.getJogador().getReserva().adicionar(daReserva);
    daEquipe.receberDano(20);
    daEquipe.aplicarStatus(StatusPokemon.PARALISADO);
    daReserva.receberDano(10);

    controlador.usarEventoOpcional(EscolhaEventoRota.DESCANSO);

    assertEquals(daEquipe.getHpMaximo(), daEquipe.getHpAtual());
    assertEquals(StatusPokemon.NENHUM, daEquipe.getStatus());
    assertEquals(daReserva.getHpMaximo(), daReserva.getHpAtual());
  }

  private ControladorPartida criarControlador(String inicial) {
    ControladorPartida controlador =
        new ControladorPartida(
            new CatalogoJogo(),
            new RepositorioPartidaArquivo(pastaTemp.resolve(inicial + ".dat")),
            new Random(42L));
    controlador.novaPartida(inicial);
    return controlador;
  }
}
