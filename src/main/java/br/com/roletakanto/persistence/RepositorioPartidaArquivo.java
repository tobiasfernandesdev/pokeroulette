package br.com.roletakanto.persistence;

import br.com.roletakanto.model.Partida;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    Path diretorioPai = caminho.getParent();
    if (diretorioPai != null) {
      Files.createDirectories(diretorioPai);
    }

    try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(caminho))) {
      saida.writeObject(partida);
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