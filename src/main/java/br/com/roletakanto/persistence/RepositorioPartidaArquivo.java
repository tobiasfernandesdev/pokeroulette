package br.com.roletakanto.persistence;

import br.com.roletakanto.model.Partida;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class RepositorioPartidaArquivo implements RepositorioPartida {
  private final Path caminho;

  public RepositorioPartidaArquivo(Path caminho) {
    this.caminho = caminho;
  }

  @Override
  public Optional<Partida> carregar() throws IOException {
    if (!existeSalvamento()) {
      return Optional.empty();
    }

    try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(caminho))) {
      Object objeto = entrada.readObject();
      if (objeto instanceof Partida partida) {
        return Optional.of(partida);
      }
      throw new IOException("Não contém arquivo de salvamento válido.");
    } catch (ClassNotFoundException e) {
      throw new IOException("Erro ao desserializar a partida salva.", e);
    }
  }

  @Override
  public void salvar(Partida partida) throws IOException {
    if (partida == null) {
      throw new IllegalArgumentException("A partida não pode ser nula.");
    }

    Path destino = caminho.toAbsolutePath();
    Path diretorioPai = destino.getParent();
    Files.createDirectories(diretorioPai);
    Path temporario = Files.createTempFile(diretorioPai, "partida-", ".tmp");
    try {
      try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(temporario))) {
        saida.writeObject(partida);
      }
      try {
        Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporario);
    }
  }

  @Override
  public boolean existeSalvamento() {
    return Files.isRegularFile(caminho);
  }

  @Override
  public void apagar() throws IOException {
    Files.deleteIfExists(caminho);
  }
}
