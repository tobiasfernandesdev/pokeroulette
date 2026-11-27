package br.com.roletakanto.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import br.com.roletakanto.model.Partida;

/** Um arquivo por nome, com leitura compatível do antigo slot Swing. */
public final class SalvamentosJogador {
  private final Path raiz;

  public SalvamentosJogador(Path raiz) {
    this.raiz = raiz;
  }

  public RepositorioPartida abrir(String nome) {
    String dono = chave(nome);
    Path caminho = raiz.resolve("jogadores").resolve(hash(dono) + ".dat");
    RepositorioPartidaArquivo atual = new RepositorioPartidaArquivo(caminho);
    RepositorioPartidaArquivo antigo = new RepositorioPartidaArquivo(raiz.resolve("partida.dat"));

    return new RepositorioPartida() {
      @Override
      public Optional<Partida> carregar() throws IOException {
        boolean temSlot = atual.existeSalvamento();
        Optional<Partida> lida = temSlot ? atual.carregar() : antigo.carregar();
        if (lida.isEmpty()) {
          return lida;
        }
        Partida partida = lida.get();
        try {
          partida.validarEstado();
          if (dono.equals(chave(partida.getJogador().getNome()))) {
            return switch (partida.getFase()) {
              case ROTA, ORGANIZACAO, BATALHA, VITORIA_FINAL, DERROTA -> lida;
              default -> throw new IOException("A fase registrada não pode ser retomada.");
            };
          }
        } catch (IllegalArgumentException | IllegalStateException erro) {
          throw new IOException("A gravação não contém uma partida válida.", erro);
        }
        if (temSlot) {
          throw new IOException("A gravação pertence a outro jogador.");
        }
        return Optional.empty();
      }

      @Override
      public void salvar(Partida partida) throws IOException {
        if (!dono.equals(chave(partida.getJogador().getNome()))) {
          throw new IOException("Não é possível gravar a partida no perfil de outro jogador.");
        }
        atual.salvar(partida);
      }

      @Override
      public boolean existeSalvamento() {
        if (Files.exists(caminho)) {
          return true;
        }
        try {
          return carregar().isPresent();
        } catch (IOException erro) {
          // Um legado ilegível permanece intocado e nunca habilita Continuar.
          return false;
        }
      }

      @Override
      public void apagar() {
        throw new UnsupportedOperationException("A interface não apaga perfis.");
      }
    };
  }

  /** A confirmação troca o slot principal, mas mantém uma cópia do original. */
  public void guardarCopia(String nome) throws IOException {
    Path caminho = raiz.resolve("jogadores").resolve(hash(chave(nome)) + ".dat");
    if (Files.isRegularFile(caminho)) {
      Path pasta = caminho.getParent().resolve("historico");
      Files.createDirectories(pasta);
      Path copia = Files.createTempFile(pasta, caminho.getFileName() + "-", ".bak");
      Files.copy(caminho, copia, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static String chave(String nome) {
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("Informe um nome para o perfil.");
    }
    return Normalizer.normalize(nome.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFC);
  }

  private static String hash(String nome) {
    try {
      byte[] dados = nome.getBytes(StandardCharsets.UTF_8);
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados));
    } catch (NoSuchAlgorithmException erro) {
      throw new IllegalStateException("SHA-256 indisponível.", erro);
    }
  }
}
