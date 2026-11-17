package br.com.roletakanto.persistence;
import br.com.roletakanto.model.Partida; import java.io.IOException; import java.nio.file.Path; import java.util.Optional;
/** Implementação de arquivo local. */
public final class RepositorioPartidaArquivo implements RepositorioPartida { private final Path caminho; public RepositorioPartidaArquivo(Path caminho){this.caminho=caminho;} public Optional<Partida> carregar() throws IOException{return Optional.empty();} public void salvar(Partida partida) throws IOException{/* TODO */} public boolean existeSalvamento(){return java.nio.file.Files.isRegularFile(caminho);} public void apagar() throws IOException{java.nio.file.Files.deleteIfExists(caminho);} }
