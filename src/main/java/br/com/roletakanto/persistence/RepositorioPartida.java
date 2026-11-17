package br.com.roletakanto.persistence;
import br.com.roletakanto.model.Partida; import java.io.IOException; import java.util.Optional;
/** Abstração para a gravação local da campanha. */
public interface RepositorioPartida { Optional<Partida> carregar() throws IOException; void salvar(Partida partida) throws IOException; boolean existeSalvamento(); void apagar() throws IOException; }
