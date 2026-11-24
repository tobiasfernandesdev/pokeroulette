package br.com.roletakanto.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Equipe;
import br.com.roletakanto.model.EspeciePokemon;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.Jogador;
import br.com.roletakanto.model.Mochila;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.Reserva;
import br.com.roletakanto.model.ResultadoRoleta;
import br.com.roletakanto.model.ResultadoUsoItem;
import br.com.roletakanto.model.Rota;
import br.com.roletakanto.model.TipoEvento;
import br.com.roletakanto.persistence.RepositorioPartida;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CalculadoraDano;
import br.com.roletakanto.service.CatalogoJogo;
import br.com.roletakanto.service.SeletorAcaoIA;

/** Coordena os comandos da partida e aplica os resultados da roleta. */
public final class ControladorPartida {
  private final CatalogoJogo catalogo;
  private final RepositorioPartida repositorio;
  private final Random random;
  private final Map<String, EspeciePokemon> especies;
  private final Map<String, Item> itens;
  private Partida partida;

  public ControladorPartida() {
    this(
        new CatalogoJogo(),
        new RepositorioPartidaArquivo(Path.of("saves", "partida.dat")),
        new Random());
  }

  public ControladorPartida(CatalogoJogo catalogo, Random random) {
    this(catalogo, new RepositorioPartidaArquivo(Path.of("saves", "partida.dat")), random);
  }

  public ControladorPartida(
      CatalogoJogo catalogo, RepositorioPartida repositorio, Random random) {
    if (catalogo == null || repositorio == null || random == null) {
      throw new IllegalArgumentException(
          "Catalogo, repositorio e gerador aleatorio sao obrigatorios.");
    }

    this.catalogo = catalogo;
    this.repositorio = repositorio;
    this.random = random;
    this.especies = catalogo.carregarEspecies();
    this.itens =
        catalogo.criarItens().stream()
            .collect(Collectors.toUnmodifiableMap(Item::getId, item -> item));
  }

  /** Cria uma campanha e coloca o Pokemon inicial na equipe do jogador. */
  public void novaPartida(String idInicial) {
    Item pocao = itens.get(CatalogoJogo.ID_POCAO);
    Mochila mochila = new Mochila();
    if (pocao == null) {
      throw new IllegalStateException("Item de pocao nao encontrado no catalogo.");
    } else {
      mochila.adicionar(pocao, 2);
    }

    EspeciePokemon inicial = especies.get(idInicial);
    if (inicial == null) {
      throw new IllegalArgumentException("Pokemon inicial inexistente: " + idInicial);
    }

    Equipe equipe = new Equipe();
    equipe.adicionar(inicial.criarInstancia());
    Jogador jogador = new Jogador("Jogador", equipe, new Reserva(), mochila);
    partida = Partida.nova(jogador, catalogo.criarRotas(random), catalogo.criarLideres());
  }

  public Partida getPartida() {
    return partida;
  }

  /**
   * Consome um giro da rota atual e aplica seu resultado. A mensagem retornada
   * pode ser exibida diretamente no terminal ou na interface Swing.
   */
  public String girarRoleta() {
    return girarRoletaComResultado().mensagem();
  }

  /**
   * Executa um único sorteio e preserva o resultado usado pela animação da tela.
   * O método textual acima continua disponível para os clientes existentes.
   */
  public ResultadoGiro girarRoletaComResultado() {
    if (partida == null) {
      return ResultadoGiro.falha("Inicie uma partida antes de girar a roleta.");
    }
    if(!partida.podeContinuar()) {
      return ResultadoGiro.falha(
          "A partida terminou. Inicie uma nova para continuar jogando.");
    }
    if(partida.getFase() != FasePartida.ROTA) {
      return ResultadoGiro.falha("A roleta só pode ser girada durante a fase de rota.");
    }

    Rota rota = partida.getRotaAtual();
    if (!rota.temGiroDisponivel()) {
      return ResultadoGiro.falha("Sem giros disponíveis em " + rota.getNome() + ".");
    }

    rota.registrarGiro();
    ResultadoRoleta resultado = rota.getRoleta().sortear();
    String mensagem = aplicarResultado(resultado);

    return ResultadoGiro.sucesso(
        resultado,
        "Resultado: " + mensagem + System.lineSeparator()
            + "Giros restantes: " + rota.getGirosRestantes());
  }

  private String aplicarResultado(ResultadoRoleta resultado) {
    return switch (resultado.tipo()) {
      case POKEMON -> aplicarPokemon(resultado.idConteudo());
      case ITEM -> aplicarItem(resultado.idConteudo());
      case EVENTO -> aplicarEvento();
    };
  }

  private String aplicarPokemon(String idEspecie) {
    EspeciePokemon especie = especies.get(idEspecie);
    if (especie == null) {
      return "Pokemon sorteado inexistente: " + idEspecie + ".";
    }

    Pokemon pokemon = especie.criarInstancia();
    Jogador jogador = partida.getJogador();
    if (jogador.getEquipe().adicionar(pokemon)) {
      return especie.getNome() + " foi adicionado a equipe.";
    }
    if (jogador.getReserva().adicionar(pokemon)) {
      return especie.getNome() + " foi enviado para a reserva.";
    }

    return adicionarPocaoComoConversao(especie.getNome());
  }

  private String aplicarItem(String idItem) {
    Item item = itens.get(idItem);
    if (item == null) {
      return "Item sorteado inexistente: " + idItem + ".";
    }

    if (partida.getJogador().getMochila().adicionar(item, 1)) {
      return item.getNome() + " foi adicionado a mochila.";
    }
    return "A mochila esta cheia; " + item.getNome() + " nao foi recebido.";
  }

  private String aplicarEvento() {
    TipoEvento evento = TipoEvento.values()[random.nextInt(TipoEvento.values().length)];
    return switch (evento) {
      case CENTRO -> {
        Jogador jogador = partida.getJogador();
        jogador.restaurarEquipeNoCentro();
        jogador.getReserva().listar().forEach(Pokemon::restaurarParaCentro);
        yield "Evento Centro: todos os Pokemon foram restaurados.";
      }
      case RECOMPENSA -> {
        Item pocao = itens.get(CatalogoJogo.ID_POCAO);
        if (pocao == null) {
          throw new IllegalStateException("Item de pocao nao encontrado no catalogo.");
        }
        yield aplicarItem(CatalogoJogo.ID_POCAO)
            .replace(" foi adicionado a mochila.", " (recompensa da rota).");
      }
      case CAMINHO_VAZIO -> "Evento Caminho Vazio: nada aconteceu.";
    };
  }

  private String adicionarPocaoComoConversao(String nomePokemon) {
    Item pocao = itens.get(CatalogoJogo.ID_POCAO);
    if (pocao != null && partida.getJogador().getMochila().adicionar(pocao, 1)) {
      return "Equipe e reserva cheias. " + nomePokemon + " foi convertido em uma Pocao.";
    }
    return "Equipe, reserva e mochila cheias. " + nomePokemon + " nao foi recebido.";
  }

  /** Usa uma Pokebola Dourada para conceder um giro, ate o limite de tres. */
  public String usarGiroBonus() {
    if (partida == null) {
      return "Inicie uma partida antes de usar a Pokebola Dourada.";
    }
    if (!partida.podeContinuar()) {
      return "A partida terminou. Inicie uma nova para continuar jogando.";
    }
    if (partida.getFase() != FasePartida.ROTA) {
      return "A Pokebola Dourada so pode ser usada durante a fase de rota.";
    }

    ResultadoUsoItem resultado =
        partida
            .getJogador()
            .getMochila()
            .usar(
                CatalogoJogo.ID_POKEBOLA_DOURADA,
                new ContextoUsoItem(partida, null, null));
    if (!resultado.sucesso()) {
      return resultado.mensagem();
    }

    return resultado.mensagem() + " Giros restantes: " + partida.getRotaAtual().getGirosRestantes();
  }

  public void moverParaEquipe(String idPokemon) {
    validarReorganizacaoEquipe("mover um Pokemon para a equipe");
    if (partida
            .getJogador()
            .getReserva()
            .transferir(idPokemon, partida.getJogador().getEquipe())
        == null) {
      throw new IllegalStateException("Nao foi possivel mover o Pokemon para a equipe.");
    }
  }

  public void moverParaReserva(String idPokemon) {
    validarReorganizacaoEquipe("mover um Pokemon para a reserva");
    Equipe equipe = partida.getJogador().getEquipe();
    Pokemon alvo = equipe.obterPorId(idPokemon);
    if (alvo != null && alvo.estaApto() && equipe.contarAptos() <= 1) {
      throw new IllegalStateException("A equipe precisa manter ao menos um Pokemon apto.");
    }
    if (equipe.transferir(idPokemon, partida.getJogador().getReserva()) == null) {
      throw new IllegalStateException("Nao foi possivel mover o Pokemon para a reserva.");
    }
  }

  private void validarReorganizacaoEquipe(String acao) {
    if (partida == null) {
      throw new IllegalStateException("Inicie uma partida antes de " + acao + ".");
    }
    if (!partida.podeContinuar()) {
      throw new IllegalStateException("A partida terminou. Inicie uma nova para continuar jogando.");
    }
    if (partida.getFase() != FasePartida.ROTA && partida.getFase() != FasePartida.ORGANIZACAO) {
      throw new IllegalStateException(
          "So e possivel " + acao + " durante a rota ou organizacao.");
    }
  }

  public void confirmarBatalha() {
    if (partida == null) {
          throw new IllegalStateException("Inicie uma partida antes de confirmar a batalha.");
        }
        prepararBatalhaAtual();
      }

      private void prepararBatalhaAtual() {
        partida.iniciarBatalha();
        Batalha batalha =
            new Batalha(
                partida,
                partida.getJogador(),
                partida.getLiderAtual(),
                new CalculadoraDano(),
                new SeletorAcaoIA());
        partida.definirBatalhaAtual(batalha);
  }

  public void navegarParaFase(FasePartida fase) {
    if (partida == null) {
      throw new IllegalStateException("Inicie uma partida antes de navegar entre fases.");
    }
    if (fase == null) {
      throw new IllegalArgumentException("A fase de destino não pode ser nula.");
    }

    partida.validarEstado();

    switch (fase) {
      case ROTA -> {
        if (partida.getFase() == FasePartida.ORGANIZACAO) {
          partida.voltarParaRota();
        } else if (partida.getFase() != FasePartida.ROTA) {
          throw new IllegalStateException(
              "Não é possível ir para ROTA a partir da fase " + partida.getFase() + ".");
        }
      }
      case ORGANIZACAO -> {
        if (partida.getFase() == FasePartida.ROTA) {
          partida.iniciarOrganizacao();
        } else if (partida.getFase() != FasePartida.ORGANIZACAO) {
          throw new IllegalStateException(
              "Não é possível ir para ORGANIZACAO a partir da fase " + partida.getFase() + ".");
        }
      }
      case BATALHA -> {
        if (partida.getFase() != FasePartida.BATALHA) {
          prepararBatalhaAtual();
        } else if (partida.getBatalhaAtual() == null) {
          Batalha batalha =
              new Batalha(
                  partida,
                  partida.getJogador(),
                  partida.getLiderAtual(),
                  new CalculadoraDano(),
                  new SeletorAcaoIA());
          partida.definirBatalhaAtual(batalha);
        }
      }
      case MENU, ESCOLHA_INICIAL, VITORIA_GINASIO, DERROTA, VITORIA_FINAL -> {
        if (partida.getFase() != fase) {
          throw new IllegalStateException(
              "N�o � poss�vel transicionar manualmente para a fase " + fase + ".");
        }
      }
    }
  }

  public void continuarPartida() {
    if (repositorio == null) {
      throw new IllegalStateException("Repositório de salvamento não configurado.");
    }

    try {
      Partida partidaSalva =
          repositorio
              .carregar()
              .orElseThrow(
                  () -> new IllegalStateException("Nenhuma gravação encontrada para continuar."));

      partidaSalva.validarEstado();
      this.partida = partidaSalva;
      navegarParaFase(partidaSalva.getFase());
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar a gravação salva.", e);
    }
  }

  public void salvarPartida() {
    if (repositorio == null) {
      throw new IllegalStateException("Repositório de salvamento não configurado.");
    }
    if (partida == null) {
      throw new IllegalStateException("Inicie uma partida antes de salvar.");
    }

    partida.validarEstado();

    try {
      repositorio.salvar(partida);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar a partida.", e);
    }
  }
}