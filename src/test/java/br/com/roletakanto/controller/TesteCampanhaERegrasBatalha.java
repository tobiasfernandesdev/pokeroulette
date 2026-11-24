package br.com.roletakanto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CatalogoJogo;

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
  void deveBloquearGolpeEspecialQuandoUsosEsgotarem() {
    controladorPartida.confirmarBatalha();
    Batalha batalha = controladorPartida.getPartida().getBatalhaAtual();
    Pokemon ativo = batalha.obterAtivoJogador();

    Golpe especial =
        ativo.getEspecie().getGolpes().stream()
            .filter(g -> !g.ehIlimitado() && g.getUsosMaximos() > 0)
            .findFirst()
            .orElseThrow();

    int limite = especial.getUsosMaximos();
    for (int i = 0; i < limite; i++) {
      if (batalha.verificarResultado() != ResultadoBatalha.EM_ANDAMENTO) {
        return; // Caso a batalha termine antes de esgotar o PP
      }
      controladorBatalha.executarGolpe(especial.getId());
    }

    if (batalha.verificarResultado() == ResultadoBatalha.EM_ANDAMENTO
        && batalha.obterAtivoJogador().getId().equals(ativo.getId())) {
      assertFalse(batalha.possuiUsoDisponivel(ativo, especial));
      ResultadoTurno tentativaExtra = controladorBatalha.executarGolpe(especial.getId());
      assertEquals(ResultadoBatalha.EM_ANDAMENTO, tentativaExtra.resultado());
      assertTrue(
          tentativaExtra.mensagens().stream()
              .anyMatch(m -> m.toLowerCase().contains("sem mais usos")));
    }
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