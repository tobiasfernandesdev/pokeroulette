package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.persistence.RepositorioPartida;
import br.com.roletakanto.service.CatalogoJogo;

/** Coordena as telas e os pontos de gravação, consultando a fase da partida. */
final class FluxoCampanha {
  private final ControladorPartida controlador;
  private final NavegadorTelas navegador;
  private final AudioJogo audio;
  private final PainelRota rota = new PainelRota();
  private final PainelEquipe equipe = new PainelEquipe();
  private final PainelBatalha batalha = new PainelBatalha();
  private final PainelResultado resultado = new PainelResultado();
  private final MochilaVisual mochila = new MochilaVisual();
  private final PainelEvento evento = new PainelEvento();
  private final JPanel troca = EstiloJogo.painel();
  private String nomeCandidato = "Jogador";
  private RepositorioPartida perfil;
  private Runnable aoMenu;
  private boolean gravacaoFalhou;

  FluxoCampanha(NavegadorTelas navegador, ControladorPartida controlador) {
    this(navegador, controlador, AudioJogo.SILENCIO);
  }

  FluxoCampanha(NavegadorTelas navegador, ControladorPartida controlador, AudioJogo audio) {
    this.navegador = navegador;
    this.controlador = controlador;
    this.audio = audio;
    aoMenu = () -> navegador.mostrar("menu");
    navegador.registrar("rota", rota);
    navegador.registrar("equipe", equipe);
    navegador.registrar("batalha", batalha);
    navegador.registrar("resultado", resultado);
    navegador.registrar("mochila", mochila);
    navegador.registrar("evento", evento);
    navegador.registrar("troca", troca);
    rota.definirAcoes(this::mostrarEquipe, this::abrirMochilaRota, this::mostrarEquipe);
    rota.configurarInteracoes(audio, this::abrirEvento, this::salvar);
    equipe.conectar(controlador, this::voltarDaEquipe, this::desafiar);
    batalha.configurarInteracoes(audio, this::abrirMochilaBatalha, this::abrirTroca, this::salvar);
    resultado.aoContinuar(() -> {
      if (controlador.getPartida().podeContinuar()) {
        mostrarRota();
      } else {
        voltarAoMenu();
      }
    });
  }

  void definirPerfil(String nome, RepositorioPartida perfil, Runnable voltarAoMenu) {
    definirNomeCandidato(nome);
    this.perfil = perfil;
    aoMenu = voltarAoMenu;
    gravacaoFalhou = false;
  }

  void definirNomeCandidato(String nome) {
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("O nome do candidato é obrigatório.");
    }
    nomeCandidato = nome.strip();
  }

  void iniciar(String idInicial) {
    controlador.novaPartida(idInicial, nomeCandidato);
    rota.conectar(controlador);
    mostrarRota();
    salvar();
  }

  void continuar() {
    controlador.continuarPartida(perfil);
    var partida = controlador.getPartida();
    rota.conectar(controlador);
    switch (partida.getFase()) {
      case ROTA -> mostrarRota();
      case ORGANIZACAO -> {
        tocarRota();
        mostrarEquipe();
      }
      case BATALHA -> mostrarBatalha();
      case DERROTA, VITORIA_FINAL -> {
        boolean ganhou = partida.getFase() == FasePartida.VITORIA_FINAL;
        resultado.apresentar(partida, partida.getLiderAtual(),
            new ResultadoTurno(List.of(), ganhou ? ResultadoBatalha.VITORIA : ResultadoBatalha.DERROTA, null));
        audio.tocar(ganhou ? AudioJogo.Trilha.VITORIA : AudioJogo.Trilha.DERROTA);
        navegador.mostrar("resultado");
      }
      default -> throw new IllegalStateException("A fase gravada não está disponível na campanha.");
    }
  }

  void voltar() {
    switch (navegador.getTelaAtual()) {
      case "mochila" -> mochila.voltar();
      case "evento" -> evento.voltar();
      case "troca" -> retornarAoCombate();
      case "rota" -> {
        if (!rota.estaGirando()) {
          voltarAoMenu();
        }
      }
      case "equipe" -> {
        var partida = controlador.getPartida();
        if (partida.getFase() == FasePartida.ROTA || partida.getRotaAtual().temGiroDisponivel()) {
          voltarDaEquipe();
        } else {
          voltarAoMenu();
        }
      }
      case "batalha", "resultado" -> voltarAoMenu();
    }
  }

  boolean salvarSeJogando() {
    if (List.of("rota", "equipe", "batalha", "resultado", "mochila", "evento", "troca")
        .contains(navegador.getTelaAtual())) {
      salvar();
      return !gravacaoFalhou;
    }
    return true;
  }

  private void salvar() {
    if (perfil == null) {
      return;
    }
    try {
      controlador.salvarPartida(perfil);
    } catch (IllegalStateException erro) {
      String mensagem = erro.getMessage() + " Seu progresso atual está na janela.";
      rota.mostrarMensagem(mensagem, true);
      batalha.adicionarMensagem(mensagem);
      gravacaoFalhou = true;
      return;
    }
    gravacaoFalhou = false;
  }

  private void voltarAoMenu() {
    salvar();
    if (!gravacaoFalhou) {
      aoMenu.run();
    }
  }

  private void tocarRota() {
    audio.tocar(AudioJogo.Trilha.daRota(controlador.getPartida().getRotaAtual().getId()));
  }

  private void mostrarRota() {
    rota.apresentar(controlador.getPartida());
    tocarRota();
    navegador.mostrar("rota");
  }

  private void mostrarEquipe() {
    equipe.apresentarPartida();
    salvar();
    navegador.mostrar("equipe");
  }

  private void voltarDaEquipe() {
    controlador.navegarParaFase(FasePartida.ROTA);
    mostrarRota();
    salvar();
  }

  private void desafiar() {
    controlador.confirmarBatalha();
    mostrarBatalha();
    salvar();
  }

  private void mostrarBatalha() {
    var partida = controlador.getPartida();
    var lider = partida.getBatalhaAtual().getLider();
    batalha.conectar(partida, new ControladorBatalha(partida), turno -> {
      resultado.apresentar(partida, lider, turno);
      audio.resultado(turno.resultado() == ResultadoBatalha.VITORIA);
      navegador.mostrar("resultado");
    });
    audio.tocar(AudioJogo.Trilha.doLider(lider.getId()));
    navegador.mostrar("batalha");
  }

  private void abrirMochilaRota() {
    audio.efeito(AudioJogo.Efeito.ABRIR);
    mochila.abrir(controlador.getPartida(), (id, alvo) -> {
      var uso = controlador.usarItemNaRota(id, alvo);
      if (uso.sucesso()) {
        audio.efeito(switch (id) {
          case CatalogoJogo.ID_REVIVER -> AudioJogo.Efeito.REVIVER;
          case CatalogoJogo.ID_POKEBOLA_DOURADA -> AudioJogo.Efeito.ITEM;
          default -> AudioJogo.Efeito.CURA;
        });
        salvar();
      }
      rota.mostrarMensagem(uso.mensagem(), !uso.sucesso());
      return uso.mensagem();
    }, () -> {
      audio.efeito(AudioJogo.Efeito.VOLTAR);
      mostrarRota();
    });
    navegador.mostrar("mochila");
  }

  private void abrirMochilaBatalha() {
    audio.efeito(AudioJogo.Efeito.ABRIR);
    mochila.abrir(controlador.getPartida(), (id, alvo) -> batalha.usarItem(id), this::retornarAoCombate);
    navegador.mostrar("mochila");
  }

  private void abrirEvento() {
    var area = controlador.getPartida().getRotaAtual();
    audio.efeito(AudioJogo.Efeito.EVENTO);
    evento.abrir(area, escolha -> {
      String resposta = controlador.usarEventoOpcional(escolha);
      if (!area.eventoOpcionalDisponivel()) {
        audio.efeito(AudioJogo.Efeito.ITEM);
        salvar();
      }
      rota.mostrarMensagem(resposta, area.eventoOpcionalDisponivel());
      return resposta;
    }, () -> {
      audio.efeito(AudioJogo.Efeito.VOLTAR);
      mostrarRota();
    });
    navegador.mostrar("evento");
  }

  private void abrirTroca() {
    troca.removeAll();
    troca.add(EstiloJogo.faixa("ESCOLHA UM POKÉMON DA EQUIPE", 18), BorderLayout.NORTH);
    JPanel opcoes = new JPanel(new GridLayout(0, 1, 8, 8));
    opcoes.setOpaque(false);
    var visuais = new VisualPokemon();
    for (var pokemon : batalha.trocasDisponiveis()) {
      var botao = EstiloJogo.criarBotao(pokemon.getEspecie().getNome() + " · HP "
          + pokemon.getHpAtual() + "/" + pokemon.getHpMaximo(), true);
      botao.setIcon(visuais.carregarIcone(pokemon.getEspecie().getId(), 52, 52));
      botao.addActionListener(acao -> {
        audio.efeito(AudioJogo.Efeito.SELECAO);
        batalha.trocarPor(pokemon);
        if (controlador.getPartida().getFase() == FasePartida.BATALHA) {
          retornarAoCombate();
        }
      });
      opcoes.add(botao);
    }
    troca.add(opcoes, BorderLayout.CENTER);
    var voltar = EstiloJogo.criarBotao("VOLTAR", false);
    voltar.addActionListener(acao -> retornarAoCombate());
    troca.add(voltar, BorderLayout.SOUTH);
    troca.revalidate();
    navegador.mostrar("troca");
  }

  private void retornarAoCombate() {
    audio.efeito(AudioJogo.Efeito.VOLTAR);
    batalha.atualizarStatus();
    navegador.mostrar("batalha");
  }
}
