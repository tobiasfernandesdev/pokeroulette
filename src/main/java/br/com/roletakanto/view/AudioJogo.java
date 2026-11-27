package br.com.roletakanto.view;

import java.io.IOException;
import java.util.EnumMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/** Todas as operações de áudio passam por uma única fila fora da thread Swing. */
final class AudioJogo implements AutoCloseable {
  static final AudioJogo SILENCIO = new AudioJogo(false);

  enum Trilha {
    LOGIN("menu-music"), MENU("puzzle-flow"), INICIAL("shopkeeper"),
    ROTA_1("overworld-march"), CAVERNA("ashen-fields"), ROTA_2("cavalry-charge"),
    TORRE("moonlit-castle"), BROCK("battle-rush"), MISTY("march-of-blades"),
    SURGE("nomad-riders"), ERIKA("serpent-temple"), HEROI("hero-jingle"),
    VITORIA("victory-loop"), DERROTA("sorrowful-banner");

    final String arquivo;

    Trilha(String arquivo) {
      this.arquivo = arquivo;
    }

    static Trilha daRota(String id) {
      return switch (id) {
        case "caverna" -> CAVERNA;
        case "rota_2" -> ROTA_2;
        case "torre_pokemon" -> TORRE;
        default -> ROTA_1;
      };
    }

    static Trilha doLider(String id) {
      return switch (id) {
        case "misty" -> MISTY;
        case "lt_surge" -> SURGE;
        case "erika" -> ERIKA;
        default -> BROCK;
      };
    }
  }

  enum Efeito {
    CLIQUE, CONFIRMA, SELECAO, ABRIR, VOLTAR, CURA, REVIVER, GIRO, TICK,
    POKEMON, ITEM, EVENTO, ATAQUE, DANO, DESMAIO, DERROTA
  }

  private final ScheduledThreadPoolExecutor fila;
  private final EnumMap<Efeito, Clip> efeitos = new EnumMap<>(Efeito.class);
  private Trilha desejada;
  private Trilha faixaAtual;
  private Clip musica;
  private ScheduledFuture<?> fimDaVinheta;
  private boolean musicaLigada = true;
  private boolean efeitosLigados = true;
  private int volumeMusica = 35;
  private int volumeEfeitos = 45;

  AudioJogo() {
    this(true);
  }

  private AudioJogo(boolean ativo) {
    fila = ativo ? new ScheduledThreadPoolExecutor(1, tarefa -> {
      Thread thread = new Thread(tarefa, "Roleta Kanto — áudio");
      thread.setDaemon(true);
      return thread;
    }) : null;
    if (fila != null) {
      fila.setRemoveOnCancelPolicy(true);
      fila.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }
  }

  void tocar(Trilha trilha) {
    enviar(() -> {
      if (desejada == trilha) {
        return;
      }
      desejada = trilha;
      trocarMusica(trilha, true);
    });
  }

  void resultado(boolean vitoria) {
    if (!vitoria) {
      tocar(Trilha.DERROTA);
      efeito(Efeito.DERROTA);
      return;
    }
    enviar(() -> {
      desejada = Trilha.VITORIA;
      trocarMusica(Trilha.HEROI, false);
      if (musica == null) {
        trocarMusica(Trilha.VITORIA, true);
      }
    });
  }

  void efeito(Efeito efeito) {
    enviar(() -> {
      if (!efeitosLigados || volumeEfeitos == 0) {
        return;
      }
      Clip clip = efeitos.computeIfAbsent(efeito,
          id -> carregar("sfx/" + id.name().toLowerCase(java.util.Locale.ROOT), volumeEfeitos));
      if (clip != null && !clip.isRunning()) {
        clip.setFramePosition(0);
        clip.start();
      }
    });
  }

  void pararGiro() {
    enviar(() -> {
      pararEfeito(Efeito.GIRO);
      pararEfeito(Efeito.TICK);
    });
  }

  void configurar(boolean tocarMusica, boolean tocarEfeitos, int musicaPercentual, int efeitosPercentual) {
    enviar(() -> {
      boolean eraAudivel = musicaLigada && volumeMusica > 0;
      musicaLigada = tocarMusica;
      efeitosLigados = tocarEfeitos;
      volumeMusica = Math.clamp(musicaPercentual, 0, 100);
      volumeEfeitos = Math.clamp(efeitosPercentual, 0, 100);
      boolean audivel = musicaLigada && volumeMusica > 0;
      if (eraAudivel != audivel) {
        trocarMusica(desejada, true);
      } else if (musica != null) {
        volume(musica, volumeMusica);
      }
      for (Clip clip : efeitos.values()) {
        if (!efeitosLigados || volumeEfeitos == 0) {
          clip.stop();
        }
        volume(clip, volumeEfeitos);
      }
    });
  }

  private void trocarMusica(Trilha trilha, boolean repetir) {
    if (fimDaVinheta != null) {
      fimDaVinheta.cancel(false);
      fimDaVinheta = null;
    }
    if (musica != null) {
      musica.stop();
      musica.close();
      musica = null;
    }
    faixaAtual = null;
    if (trilha == null || !musicaLigada || volumeMusica == 0) {
      return;
    }
    musica = carregar("music/" + trilha.arquivo, volumeMusica);
    if (musica == null) {
      return;
    }
    faixaAtual = trilha;
    if (repetir) {
      musica.loop(Clip.LOOP_CONTINUOUSLY);
    } else {
      musica.start();
      synchronized (this) {
        if (!fila.isShutdown()) {
          fimDaVinheta = fila.schedule(() -> {
            if (faixaAtual == Trilha.HEROI) {
              trocarMusica(Trilha.VITORIA, true);
            }
          }, musica.getMicrosecondLength(), TimeUnit.MICROSECONDS);
        }
      }
    }
  }

  private void pararEfeito(Efeito efeito) {
    Clip clip = efeitos.get(efeito);
    if (clip != null) {
      clip.stop();
    }
  }

  private Clip carregar(String caminho, int nivel) {
    var arquivo = AudioJogo.class.getResource("/assets/audio/" + caminho + ".wav");
    if (arquivo == null) {
      return null;
    }
    Clip clip = null;
    try (var dados = AudioSystem.getAudioInputStream(arquivo)) {
      clip = AudioSystem.getClip();
      clip.open(dados);
      volume(clip, nivel);
      return clip;
    } catch (IOException | UnsupportedAudioFileException | LineUnavailableException
             | IllegalArgumentException | SecurityException erro) {
      if (clip != null) {
        clip.close();
      }
      return null;
    }
  }

  private static void volume(Clip clip, int nivel) {
    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      var ganho = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      float db = (float) (20 * Math.log10(Math.max(0.001, nivel / 100.0)));
      ganho.setValue(Math.clamp(db, ganho.getMinimum(), ganho.getMaximum()));
    }
  }

  private synchronized void enviar(Runnable acao) {
    if (fila != null && !fila.isShutdown()) {
      fila.execute(acao);
    }
  }

  @Override
  public synchronized void close() {
    if (fila != null && !fila.isShutdown()) {
      fila.execute(() -> {
        trocarMusica(null, false);
        efeitos.values().forEach(Clip::close);
        efeitos.clear();
      });
      fila.shutdown();
    }
  }
}
