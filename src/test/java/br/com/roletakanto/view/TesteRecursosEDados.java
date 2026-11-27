package br.com.roletakanto.view;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.persistence.SalvamentosJogador;
import br.com.roletakanto.service.CatalogoJogo;

final class TesteRecursosEDados {
  @TempDir Path pasta;

  @Test
  void separaPerfisELeLegadoApenasParaOMesmoDono() throws IOException {
    var salvamentos = new SalvamentosJogador(pasta);
    var ana = salvamentos.abrir("  Ana  ");
    var bruno = salvamentos.abrir("Bruno");
    assertTrue(ana.carregar().isEmpty());
    assertTrue(bruno.carregar().isEmpty());
    assertFalse(Files.exists(pasta.resolve("jogadores")));

    var controlador = new ControladorPartida(new CatalogoJogo(), new Random(159));
    controlador.novaPartida("squirtle", "Ana");
    controlador.girarRoletaComResultado();
    var antigo = new RepositorioPartidaArquivo(pasta.resolve("partida.dat"));
    controlador.salvarPartida(antigo);
    assertTrue(salvamentos.abrir("ANA").carregar().isPresent());
    assertTrue(bruno.carregar().isEmpty());

    controlador.salvarPartida(ana);
    salvamentos.guardarCopia("ana");
    controlador.novaPartida("charmander", "Bruno");
    assertThrows(IOException.class, () -> ana.salvar(controlador.getPartida()));
    controlador.salvarPartida(bruno);
    controlador.continuarPartida(ana);
    assertEquals("squirtle", controlador.getPartida().getJogador().getEquipe()
        .listar().getFirst().getEspecie().getId());
    assertEquals(1, controlador.getPartida().getRotaAtual().getGirosRestantes());
    controlador.continuarPartida(bruno);
    assertEquals("Bruno", controlador.getPartida().getJogador().getNome());
    assertTrue(antigo.existeSalvamento());
    try (var historico = Files.list(pasta.resolve("jogadores").resolve("historico"))) {
      assertEquals(1, historico.count());
    }
  }

  @Test
  void todosOsSonsConfiguradosEstaoNoClasspathEmPcm() throws Exception {
    for (var trilha : AudioJogo.Trilha.values()) {
      verificarWav("music/" + trilha.arquivo);
    }
    for (var efeito : AudioJogo.Efeito.values()) {
      verificarWav("sfx/" + efeito.name().toLowerCase(Locale.ROOT));
    }
  }

  private static void verificarWav(String arquivo) throws Exception {
    var url = TesteRecursosEDados.class.getResource("/assets/audio/" + arquivo + ".wav");
    assertNotNull(url, arquivo);
    var formato = AudioSystem.getAudioFileFormat(url);
    assertTrue(formato.getFrameLength() > 0, arquivo);
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, formato.getFormat().getEncoding(), arquivo);
    assertEquals(1, formato.getFormat().getChannels(), arquivo);
    assertEquals(16, formato.getFormat().getSampleSizeInBits(), arquivo);
  }
}
