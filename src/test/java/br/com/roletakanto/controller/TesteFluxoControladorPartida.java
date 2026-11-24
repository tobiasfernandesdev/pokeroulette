package br.com.roletakanto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.persistence.RepositorioPartida;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CatalogoJogo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TesteFluxoControladorPartida {

  @TempDir Path pastaTemp;

  private CatalogoJogo catalogo;
  private RepositorioPartida repositorio;
  private ControladorPartida controlador;
  private String idInicial;

  @BeforeEach
  void preparar() {
    catalogo = new CatalogoJogo();
    Path arquivoSave = pastaTemp.resolve("partida-teste.dat");
    repositorio = new RepositorioPartidaArquivo(arquivoSave);
    controlador = new ControladorPartida(catalogo, repositorio, new Random(42L));

    idInicial = catalogo.carregarEspecies().keySet().iterator().next();
    controlador.novaPartida(idInicial);
  }

  @Test
  void deveMoverPokemonEntreReservaEEquipe() {
    Pokemon extra = catalogo.carregarEspecies().values().stream()
        .skip(1)
        .findFirst()
        .orElseThrow()
        .criarInstancia();

    assertTrue(controlador.getPartida().getJogador().getReserva().adicionar(extra));

    controlador.moverParaEquipe(extra.getId());

    assertNotNull(controlador.getPartida().getJogador().getEquipe().obterPorId(extra.getId()));
    assertNull(controlador.getPartida().getJogador().getReserva().obterPorId(extra.getId()));

    controlador.moverParaReserva(extra.getId());

    assertNull(controlador.getPartida().getJogador().getEquipe().obterPorId(extra.getId()));
    assertNotNull(controlador.getPartida().getJogador().getReserva().obterPorId(extra.getId()));
  }

  @Test
  void naoDeveMoverUltimoPokemonAptoParaReserva() {
    Pokemon inicialNaEquipe = controlador.getPartida().getJogador().getEquipe().obter(0);

    assertThrows(
        IllegalStateException.class,
        () -> controlador.moverParaReserva(inicialNaEquipe.getId()));
  }

  @Test
  void deveNavegarEntreRotaEOrganizacao() {
    assertEquals(FasePartida.ROTA, controlador.getPartida().getFase());

    controlador.navegarParaFase(FasePartida.ORGANIZACAO);
    assertEquals(FasePartida.ORGANIZACAO, controlador.getPartida().getFase());

    controlador.navegarParaFase(FasePartida.ROTA);
    assertEquals(FasePartida.ROTA, controlador.getPartida().getFase());
  }

  @Test
  void deveSalvarEContinuarPartidaComEstadoPreservado() {
    controlador.navegarParaFase(FasePartida.ORGANIZACAO);
    controlador.salvarPartida();

    ControladorPartida novoControlador =
        new ControladorPartida(catalogo, repositorio, new Random(42L));
    novoControlador.continuarPartida();

    assertNotNull(novoControlador.getPartida());
    assertEquals(FasePartida.ORGANIZACAO, novoControlador.getPartida().getFase());
  }

  @Test
  void deveRetomarBatalhaComHpPreservado() {
    controlador.novaPartida("bulbasaur");
    controlador.salvarPartida();
    controlador.confirmarBatalha();
    var combate = new ControladorBatalha(controlador.getPartida());
    combate.executarGolpe("ataque_planta");
    var batalha = controlador.getPartida().getBatalhaAtual();
    int hpJogador = batalha.obterAtivoJogador().getHpAtual();
    int hpAdversario = batalha.getAtivoLider().getHpAtual();
    controlador.salvarPartida();

    var novoControlador = new ControladorPartida(catalogo, repositorio, new Random(42L));
    novoControlador.continuarPartida();
    var retomada = novoControlador.getPartida();
    assertEquals(FasePartida.BATALHA, retomada.getFase());
    assertEquals(hpJogador, retomada.getBatalhaAtual().obterAtivoJogador().getHpAtual());
    assertEquals(hpAdversario, retomada.getBatalhaAtual().getAtivoLider().getHpAtual());
  }

  @Test
  void deveFalharAoContinuarSemGravacaoOuComArquivoInvalido() throws Exception {
    RepositorioPartida repoVazio =
        new RepositorioPartidaArquivo(pastaTemp.resolve("inexistente.dat"));
    ControladorPartida ctrlSemSave =
        new ControladorPartida(catalogo, repoVazio, new Random(42L));

    assertThrows(IllegalStateException.class, ctrlSemSave::continuarPartida);

    Path arquivoCorrompido = pastaTemp.resolve("corrompido.dat");
    Files.writeString(arquivoCorrompido, "conteudo-invalido");
    ControladorPartida ctrlCorrompido =
        new ControladorPartida(
            catalogo, new RepositorioPartidaArquivo(arquivoCorrompido), new Random(42L));

    assertThrows(IllegalStateException.class, ctrlCorrompido::continuarPartida);
  }
}
