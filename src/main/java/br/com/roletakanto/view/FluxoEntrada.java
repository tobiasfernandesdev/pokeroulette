package br.com.roletakanto.view;

import java.io.IOException;
import java.util.List;

import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.persistence.RepositorioPartida;
import br.com.roletakanto.persistence.SalvamentosJogador;
import br.com.roletakanto.service.CatalogoJogo;

/** Liga a identificação ao slot certo e protege o progresso antes de uma nova partida. */
final class FluxoEntrada {
  private final NavegadorTelas navegador;
  private final AudioJogo audio;
  private final SalvamentosJogador salvamentos;
  private final PainelLogin login = new PainelLogin();
  private final PainelInicial inicial = new PainelInicial();
  private final PainelMenu menu;
  private final FluxoCampanha campanha;
  private String nome;
  private RepositorioPartida perfil;
  private boolean novaAutorizada;

  FluxoEntrada(NavegadorTelas navegador, ControladorPartida controlador,
      SalvamentosJogador salvamentos, AudioJogo audio) {
    this.navegador = navegador;
    this.salvamentos = salvamentos;
    this.audio = audio;
    menu = new PainelMenu(this::pedirNovaPartida, audio);
    navegador.registrar("login", login);
    navegador.registrar("menu", menu);
    navegador.registrar("inicial", inicial);
    campanha = new FluxoCampanha(navegador, controlador, audio);
    login.definirAcaoContinuar(this::identificar);
    menu.definirAcoes(this::continuar, this::mostrarLogin);
    inicial.configurarNavegacao(audio, () -> mostrarMenu(null));
    inicial.definirAcaoConfirmar(this::iniciar);
    mostrarLogin();
  }

  FluxoCampanha campanha() {
    return campanha;
  }

  private void identificar(String nome) {
    this.nome = nome;
    perfil = salvamentos.abrir(nome);
    campanha.definirPerfil(nome, perfil, () -> mostrarMenu(null));
    audio.efeito(AudioJogo.Efeito.CONFIRMA);
    mostrarMenu(null);
  }

  private void mostrarMenu(String aviso) {
    boolean podeContinuar = false;
    if (aviso == null) {
      aviso = " ";
    }
    try {
      podeContinuar = perfil.carregar().isPresent();
    } catch (IOException erro) {
      aviso = "Este progresso está indisponível. O arquivo foi preservado.";
    }
    menu.apresentar(nome, podeContinuar, aviso);
    audio.tocar(AudioJogo.Trilha.MENU);
    navegador.mostrar("menu");
  }

  private void mostrarLogin() {
    navegador.mostrar("login");
    audio.tocar(AudioJogo.Trilha.LOGIN);
  }

  private void pedirNovaPartida() {
    if (perfil.existeSalvamento()) {
      menu.confirmarNova(() -> mostrarIniciais(true));
    } else {
      mostrarIniciais(false);
    }
  }

  private void mostrarIniciais(boolean confirmou) {
    novaAutorizada = confirmou;
    var especies = new CatalogoJogo().carregarEspecies();
    inicial.exibirOpcoes(List.of(especies.get("bulbasaur"), especies.get("charmander"),
        especies.get("squirtle")));
    inicial.apresentarCandidato(nome);
    audio.tocar(AudioJogo.Trilha.INICIAL);
    navegador.mostrar("inicial");
  }

  private void iniciar(String id) {
    if (perfil.existeSalvamento() && !novaAutorizada) {
      mostrarMenu("Foi encontrado um progresso. Confirme antes de começar outra partida.");
      return;
    }
    if (novaAutorizada) {
      try {
        salvamentos.guardarCopia(nome);
      } catch (IOException erro) {
        mostrarMenu("Não foi possível arquivar o progresso. Tente novamente.");
        return;
      }
    }
    campanha.iniciar(id);
  }

  private void continuar() {
    try {
      campanha.continuar();
    } catch (IllegalStateException erro) {
      mostrarMenu(erro.getMessage());
    }
  }

  void voltar() {
    switch (navegador.getTelaAtual()) {
      case "menu" -> {
        if (menu.temOpcaoAberta()) {
          menu.fecharOpcao();
        } else {
          mostrarLogin();
        }
      }
      case "inicial" -> mostrarMenu(null);
      default -> campanha.voltar();
    }
  }
}
