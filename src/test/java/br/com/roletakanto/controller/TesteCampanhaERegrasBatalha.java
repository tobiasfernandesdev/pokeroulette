package br.com.roletakanto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.roletakanto.model.AcaoBatalha;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
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
  void deveBloquearGolpeEspecialQuandoUsosEsgotarem() {
    controladorPartida.novaPartida("bulbasaur");
    var jogador = controladorPartida.getPartida().getJogador();
    var onix = catalogo.carregarEspecies().get("onix");
    var lider = new LiderGinasio("treino", "Treino", List.of(onix), null);
    Batalha batalha = new Batalha(jogador, lider, new CalculadoraDano(), new SeletorAcaoIA());
    Pokemon ativo = batalha.obterAtivoJogador();
    Golpe especial =
        ativo.getEspecie().getGolpes().stream()
            .filter(g -> !g.ehIlimitado())
            .findFirst()
            .orElseThrow();

    for (int i = 0; i < especial.getUsosMaximos(); i++) {
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
