package br.com.roletakanto.view;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.FasePartida;

/** Coordena apenas as telas. A partida continua sendo a fonte da fase e dos dados. */
final class FluxoCampanha {
  private final ControladorPartida controlador;
  private final NavegadorTelas navegador;
  private final PainelRota rota = new PainelRota();
  private final PainelEquipe equipe = new PainelEquipe();
  private final PainelBatalha batalha = new PainelBatalha();
  private final PainelResultado resultado = new PainelResultado();
  private String nomeCandidato = "Jogador";

  FluxoCampanha(NavegadorTelas navegador, ControladorPartida controlador) {
    this.navegador = navegador;
    this.controlador = controlador;
    navegador.registrar("rota", rota);
    navegador.registrar("equipe", equipe);
    navegador.registrar("batalha", batalha);
    navegador.registrar("resultado", resultado);
    rota.definirAcoes(this::mostrarEquipe, null, this::mostrarEquipe);
    equipe.conectar(controlador, this::voltarDaEquipe, this::desafiar);
    resultado.aoContinuar(() -> {
      if (controlador.getPartida().podeContinuar()) {
        mostrarRota();
      } else {
        navegador.mostrar("menu");
      }
    });
  }

  void definirNomeCandidato(String nome) {
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("O nome do candidato é obrigatório.");
    }
    nomeCandidato = nome.trim();
  }

  void iniciar(String idInicial) {
    controlador.novaPartida(idInicial, nomeCandidato);
    rota.conectar(controlador);
    navegador.mostrar("rota");
  }

  boolean rotaGirando() {
    return rota.estaGirando();
  }

  private void mostrarRota() {
    rota.apresentar(controlador.getPartida());
    navegador.mostrar("rota");
  }

  private void mostrarEquipe() {
    equipe.apresentarPartida();
    navegador.mostrar("equipe");
  }

  private void voltarDaEquipe() {
    controlador.navegarParaFase(FasePartida.ROTA);
    mostrarRota();
  }

  private void desafiar() {
    controlador.confirmarBatalha();
    var partida = controlador.getPartida();
    var lider = partida.getBatalhaAtual().getLider();
    batalha.conectar(partida, new ControladorBatalha(partida), turno -> {
      // Guardamos o líder enfrentado porque, após vencer, a partida já aponta para a próxima rota.
      resultado.apresentar(partida, lider, turno);
      navegador.mostrar("resultado");
    });
    navegador.mostrar("batalha");
  }
}
