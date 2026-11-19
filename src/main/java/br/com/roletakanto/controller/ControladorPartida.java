package br.com.roletakanto.controller;

import java.util.Map;
import java.util.Random;

import br.com.roletakanto.model.Equipe;
import br.com.roletakanto.model.EspeciePokemon;
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
import br.com.roletakanto.service.CatalogoJogo;

/** Coordena os comandos da partida e aplica os resultados da roleta. */
public final class ControladorPartida {
  private final CatalogoJogo catalogo;
  private final Random random;
  private final Map<String, EspeciePokemon> especies;
  private final Map<String, Item> itens;
  private Partida partida;

  public ControladorPartida() {
    this(new CatalogoJogo(), new Random());
  }

  public ControladorPartida(CatalogoJogo catalogo, Random random) {
    if (catalogo == null || random == null) {
      throw new IllegalArgumentException("Catalogo e gerador aleatorio sao obrigatorios.");
    }

    this.catalogo = catalogo;
    this.random = random;
    this.especies = catalogo.carregarEspecies();
    this.itens = catalogo.criarItens().stream()
        .collect(java.util.stream.Collectors.toUnmodifiableMap(Item::getId, item -> item));
  }

  /** Cria uma campanha e coloca o Pokemon inicial na equipe do jogador. */
  public void novaPartida(String idInicial) {
    EspeciePokemon inicial = especies.get(idInicial);
    if (inicial == null) {
      throw new IllegalArgumentException("Pokemon inicial inexistente: " + idInicial);
    }

    Equipe equipe = new Equipe();
    equipe.adicionar(inicial.criarInstancia());
    Jogador jogador = new Jogador("Jogador", equipe, new Reserva(), new Mochila());
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
    if (partida == null) {
      return "Inicie uma partida antes de girar a roleta.";
    }

    Rota rota = partida.getRotaAtual();
    if (!rota.temGiroDisponivel()) {
      return "Sem giros disponiveis em " + rota.getNome() + ".";
    }

    rota.registrarGiro();
    ResultadoRoleta resultado = rota.getRoleta().sortear();
    String mensagem = aplicarResultado(resultado);

    return "Resultado: " + mensagem + System.lineSeparator()
        + "Giros restantes: " + rota.getGirosRestantes();
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
      case RECOMPENSA -> aplicarItem(CatalogoJogo.ID_POCAO)
          .replace(" foi adicionado a mochila.", " (recompensa da rota).");
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

    ResultadoUsoItem resultado = partida.getJogador().getMochila().usar(
        CatalogoJogo.ID_POKEBOLA_DOURADA,
        new br.com.roletakanto.model.ContextoUsoItem(partida, null, null));
    if (!resultado.sucesso()) {
      return resultado.mensagem();
    }

    return resultado.mensagem() + " Giros restantes: "
        + partida.getRotaAtual().getGirosRestantes();
  }

  public void moverParaEquipe(String idPokemon) {
    /* Etapa posterior. */
  }

  public void moverParaReserva(String idPokemon) {
    /* Etapa posterior. */
  }

  public void confirmarBatalha() {
    /* Etapa posterior. */
  }

  public void continuarPartida() {
    /* Etapa posterior. */
  }

  public void salvarPartida() {
    /* Etapa posterior. */
  }
}
