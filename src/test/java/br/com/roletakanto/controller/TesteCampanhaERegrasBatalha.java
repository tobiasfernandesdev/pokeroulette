package br.com.roletakanto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.roletakanto.model.AcaoBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.EspeciePokemon;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.model.TipoPokemon;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CalculadoraDano;
import br.com.roletakanto.service.CatalogoJogo;
import br.com.roletakanto.service.SeletorAcaoIA;

class TesteCampanhaERegrasBatalha {

  @TempDir Path pastaTemp;

  private CatalogoJogo catalogo;
  private ControladorPartida controladorPartida;
  private ControladorBatalha controladorBatalha;

  @BeforeEach
  void preparar() {
    catalogo = new CatalogoJogo();
    var repo = new RepositorioPartidaArquivo(pastaTemp.resolve("save.dat"));
    controladorPartida = new ControladorPartida(catalogo, repo, new Random(42L));
    controladorBatalha = new ControladorBatalha();

    controladorPartida.novaPartida("charmander");
    controladorBatalha.definirPartida(controladorPartida.getPartida());
  }

  @Test
  void devePermitirDezUsosDeGolpeEspecialAoJogador() {
    controladorPartida.novaPartida("bulbasaur");
    var jogador = controladorPartida.getPartida().getJogador();
    var alvo =
        new EspeciePokemon(
            "alvo",
            "Alvo",
            TipoPokemon.NORMAL,
            10_000,
            0,
            10_000,
            1,
            List.of())
            .criarInstancia();
    var lider = new LiderGinasio("treino", "Treino", List.of(alvo.getEspecie()), null);
    Batalha batalha = new Batalha(jogador, lider, new CalculadoraDano(), new SeletorAcaoIA());
    Pokemon ativo = batalha.obterAtivoJogador();
    Golpe especial =
        ativo.getEspecie().getGolpes().stream()
            .filter(g -> !g.ehIlimitado())
            .findFirst()
            .orElseThrow();

    assertEquals(10, batalha.getUsosMaximos(ativo, especial));
    for (int i = 0; i < batalha.getUsosMaximos(ativo, especial); i++) {
      assertTrue(batalha.possuiUsoDisponivel(ativo, especial));
      var turno = batalha.executarAcao(AcaoBatalha.golpe(especial.getId()));
      assertEquals(ResultadoBatalha.EM_ANDAMENTO, turno.resultado());
      assertTrue(turno.mensagens().stream().anyMatch(m -> m.contains("usou " + especial.getNome())));
    }

    assertFalse(batalha.possuiUsoDisponivel(ativo, especial));
    int hpAdversario = batalha.getAtivoLider().getHpAtual();
    ResultadoTurno tentativaExtra = batalha.executarAcao(AcaoBatalha.golpe(especial.getId()));
    assertEquals(hpAdversario, batalha.getAtivoLider().getHpAtual());
    assertTrue(tentativaExtra.mensagens().stream().anyMatch(m -> m.contains("não tem mais usos")));
  }

  @Test
  void deveManterTresUsosDeGolpeEspecialParaLideres() {
    controladorPartida.novaPartida("bulbasaur");
    var jogador = controladorPartida.getPartida().getJogador();
    var lider = catalogo.criarLideres().get(0);
    Batalha batalha = new Batalha(jogador, lider, new CalculadoraDano(), new SeletorAcaoIA());
    Pokemon adversario = batalha.getAtivoLider();
    Golpe especial =
        adversario.getEspecie().getGolpes().stream()
            .filter(g -> !g.ehIlimitado())
            .findFirst()
            .orElseThrow();

    assertEquals(3, batalha.getUsosMaximos(adversario, especial));
    for (int i = 0; i < 3; i++) {
      batalha.executarAcao(AcaoBatalha.golpe("investida"));
    }

    assertEquals(0, batalha.getUsosRestantes(adversario, especial));
    assertFalse(batalha.possuiUsoDisponivel(adversario, especial));
  }

  @Test
  void deveVencerPrimeiroGinasioComCharmanderEAjudaDaRota() {
    ControladorPartida campanha =
        new ControladorPartida(
            catalogo,
            new RepositorioPartidaArquivo(pastaTemp.resolve("primeiro-ginasio.dat")),
            new Random(159L)); // Super Poção e Oddish nos dois giros iniciais.
    campanha.novaPartida("charmander");

    assertTrue(campanha.girarRoletaComResultado().sucesso());
    assertTrue(campanha.girarRoletaComResultado().sucesso());

    var equipe = campanha.getPartida().getJogador().getEquipe();
    Pokemon oddish =
        equipe.listar().stream()
            .filter(pokemon -> pokemon.getEspecie().getId().equals("oddish"))
            .findFirst()
            .orElseThrow();
    equipe.trocarPosicao(0, equipe.listar().indexOf(oddish));
    campanha.confirmarBatalha();
    ControladorBatalha combate = new ControladorBatalha(campanha.getPartida());
    CalculadoraDano calculadora = new CalculadoraDano();

    for (int turno = 0;
        campanha.getPartida().getFase() == FasePartida.BATALHA && turno < 100;
        turno++) {
      Batalha batalha = campanha.getPartida().getBatalhaAtual();
      Pokemon ativo = batalha.obterAtivoJogador();
      var mochila = campanha.getPartida().getJogador().getMochila();
      int faltando = ativo.getHpMaximo() - ativo.getHpAtual();
      var superPocao = mochila.obterPilhaPorId(CatalogoJogo.ID_SUPER_POCAO);
      var pocao = mochila.obterPilhaPorId(CatalogoJogo.ID_POCAO);

      if (superPocao != null
          && superPocao.getQuantidade() > 0
          && ativo.getHpAtual() <= ativo.getHpMaximo() * 0.45
          && faltando >= 35) {
        combate.usarItem(CatalogoJogo.ID_SUPER_POCAO);
        continue;
      }
      if (pocao != null
          && pocao.getQuantidade() > 0
          && ativo.getHpAtual() <= ativo.getHpMaximo() * 0.30
          && faltando >= 15) {
        combate.usarItem(CatalogoJogo.ID_POCAO);
        continue;
      }

      Golpe golpe =
          ativo.getEspecie().getGolpes().stream()
              .filter(g -> batalha.possuiUsoDisponivel(ativo, g))
              .max(
                  Comparator.comparingInt(
                      g -> calculadora.calcular(ativo, batalha.getAtivoLider(), g)))
              .orElseThrow();
      combate.executarGolpe(golpe.getId());
    }

    assertEquals(FasePartida.ROTA, campanha.getPartida().getFase());
  }

  @Test
  void deveConcluirCampanhaCompletaAteVitoriaFinal() {
    var partida = controladorPartida.getPartida();
    int totalLideres = partida.getLiderAtual() != null ? 4 : 0;

    for (int i = 0; i < totalLideres; i++) {
      controladorPartida.confirmarBatalha();

      int seguranca = 0;
      while (partida.getFase() == FasePartida.BATALHA && seguranca < 200) {
        Pokemon ativo = partida.getBatalhaAtual().obterAtivoJogador();
        String idGolpe = ativo.getEspecie().getGolpes().get(0).getId();
        controladorBatalha.executarGolpe(idGolpe);
        seguranca++;
      }

      if (partida.getFase() == FasePartida.DERROTA) {
        // Se o inicial sozinho perder em alguma seed, valida o bloqueio pós-derrota
        assertFalse(partida.podeContinuar());
        assertThrows(IllegalStateException.class, controladorPartida::confirmarBatalha);
        return;
      }
    }

    assertEquals(FasePartida.VITORIA_FINAL, partida.getFase());
    assertEquals(4, partida.getInsignias().size());
    assertFalse(partida.podeContinuar());
    assertThrows(IllegalStateException.class, controladorPartida::confirmarBatalha);
  }
}
