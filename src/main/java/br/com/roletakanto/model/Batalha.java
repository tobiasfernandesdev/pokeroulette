package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.roletakanto.service.CalculadoraDano;
import br.com.roletakanto.service.SeletorAcaoIA;

public final class Batalha implements Serializable {
  private final Partida partida;
  private final Jogador jogador;
  private final LiderGinasio lider;
  private final CalculadoraDano calculadora;
  private final SeletorAcaoIA ia;
  private final List<Pokemon> equipeLider;

  private Pokemon ativoJogador;
  private Pokemon ativoLider;
  private boolean desistiu;

  public Batalha(Jogador jogador, LiderGinasio lider, CalculadoraDano calculadora, SeletorAcaoIA ia) {
    this(null, jogador, lider, calculadora, ia);
  }

  public Batalha(
      Partida partida,
      Jogador jogador,
      LiderGinasio lider,
      CalculadoraDano calculadora,
      SeletorAcaoIA ia) {
    this.partida = partida;
    this.jogador = Objects.requireNonNull(jogador, "O jogador da batalha é obrigatório.");
    this.lider = Objects.requireNonNull(lider, "O líder de ginásio é obrigatório.");
    this.calculadora =
        Objects.requireNonNull(calculadora, "A calculadora de dano é obrigatória.");
    this.ia = Objects.requireNonNull(ia, "O seletor de ação da IA é obrigatório.");

    this.equipeLider = List.copyOf(lider.criarEquipeDeBatalha());
    this.ativoJogador = encontrarProximoApto(jogador.getEquipe().listar());
    this.ativoLider = encontrarProximoApto(equipeLider);
    this.desistiu = false;
  }

  public ResultadoTurno executarAcao(AcaoBatalha acao) {
    List<String> mensagens = new ArrayList<>();

    if (acao == null) {
      return criarResultadoInvalido("Ação inválida.");
    }

    if (!batalhaEmAndamento()) {
      return criarResultadoInvalido("A batalha já foi encerrada.");
    }

    return switch (acao.tipo()) {
      case DESISTIR -> processarDesistenciaJogador(mensagens);
      case TROCA -> processarTrocaJogador(acao, mensagens);
      case ITEM -> processarItemJogador(acao, mensagens);
      case GOLPE -> processarGolpeGeral(acao, mensagens);
    };
  }

  // --- Fluxos de Ação do Jogador ---

  private ResultadoTurno processarDesistenciaJogador(List<String> mensagens) {
    this.desistiu = true;
    mensagens.add(jogador.getNome() + " desistiu da batalha.");
    return new ResultadoTurno(mensagens, ResultadoBatalha.DERROTA, getIdAtivoJogador());
  }

  private ResultadoTurno processarTrocaJogador(AcaoBatalha acao, List<String> mensagens) {
    Pokemon escolhido = jogador.getEquipe().obterPorId(acao.idAlvo());

    if (!podeTrocar(escolhido)) {
      return criarResultadoInvalido("Não foi possível trocar para esse Pokémon.");
    }

    trocarAtivo(escolhido);
    mensagens.add(jogador.getNome() + " enviou " + ativoJogador.getEspecie().getNome() + "!");

    executarTurnoIA(mensagens);
    return finalizarTurno(mensagens);
  }

  private ResultadoTurno processarItemJogador(AcaoBatalha acao, List<String> mensagens) {
    Pokemon alvoItem = definirAlvoItem(acao.idAlvo());

    if (alvoItem == null) {
      return criarResultadoInvalido("Nenhum Pokémon válido para usar o item.");
    }

    ContextoUsoItem contexto = new ContextoUsoItem(partida, this, alvoItem);
    ResultadoUsoItem uso = jogador.getMochila().usar(acao.idAlvo(), contexto);
    mensagens.add(uso.mensagem());

    if (uso.sucesso()) {
      executarTurnoIA(mensagens);
    }

    return finalizarTurno(mensagens);
  }

  private ResultadoTurno processarGolpeGeral(AcaoBatalha acao, List<String> mensagens) {
    Pokemon jogadorAtivo = obterAtivoJogador();
    Pokemon liderAtivo = getAtivoLider();

    if (jogadorAtivo == null || !jogadorAtivo.estaApto()) {
      return criarResultadoInvalido("Nenhum Pokémon apto para atacar.");
    }
    if (liderAtivo == null || !liderAtivo.estaApto()) {
      return finalizarTurno(mensagens);
    }

    Golpe golpeJogador = buscarGolpe(jogadorAtivo, acao.idAlvo());
    if (golpeJogador == null) {
      return criarResultadoInvalido("Golpe inválido.");
    }

    AcaoBatalha acaoIA = ia.escolher(this);
    resolverOrdemDeAtaque(golpeJogador, acaoIA, mensagens);

    return finalizarTurno(mensagens);
  }

  private void resolverOrdemDeAtaque(Golpe golpeJogador, AcaoBatalha acaoIA, List<String> mensagens) {
    boolean iaFazAcaoNaoOfensiva =
        acaoIA != null
            && (acaoIA.tipo() == TipoAcaoBatalha.TROCA || acaoIA.tipo() == TipoAcaoBatalha.DESISTIR);
    boolean jogadorMaisRapido =
        ativoJogador.getEspecie().getVelocidade() >= ativoLider.getEspecie().getVelocidade();

    if (iaFazAcaoNaoOfensiva || !jogadorMaisRapido) {
      executarAcaoIA(acaoIA, mensagens);
      if (batalhaEmAndamento() && ativoJogador != null && ativoJogador.estaApto() && ativoLider != null && ativoLider.estaApto()) {
        aplicarGolpe(ativoJogador, ativoLider, golpeJogador, mensagens);
      }
    } else {
      aplicarGolpe(ativoJogador, ativoLider, golpeJogador, mensagens);
      if (batalhaEmAndamento() && ativoLider != null && ativoLider.estaApto() && ativoJogador != null && ativoJogador.estaApto()) {
        executarAcaoIA(acaoIA, mensagens);
      }
    }
  }

  // --- Lógica da Inteligência Artificial (Líder) ---

  private void executarTurnoIA(List<String> mensagens) {
    if (!batalhaEmAndamento()) {
      return;
    }
    AcaoBatalha acaoIA = ia.escolher(this);
    executarAcaoIA(acaoIA, mensagens);
  }

  private void executarAcaoIA(AcaoBatalha acaoIA, List<String> mensagens) {
    if (acaoIA == null || !batalhaEmAndamento()) {
      return;
    }

    switch (acaoIA.tipo()) {
      case DESISTIR -> processarDesistenciaIA(mensagens);
      case TROCA -> processarTrocaIA(acaoIA, mensagens);
      case GOLPE -> processarGolpeIA(acaoIA, mensagens);
      case ITEM -> {
        // Líder não utiliza itens na versão atual.
      }
    }
  }

  private void processarDesistenciaIA(List<String> mensagens) {
    mensagens.add(lider.getNome() + " ficou sem opções e recuou!");
  }

  private void processarTrocaIA(AcaoBatalha acaoIA, List<String> mensagens) {
    Pokemon proximo =
        equipeLider.stream()
            .filter(p -> p.getId().equals(acaoIA.idAlvo()) && p.estaApto())
            .findFirst()
            .orElse(encontrarProximoApto(equipeLider));

    if (proximo != null) {
      ativoLider = proximo;
      mensagens.add(lider.getNome() + " enviou " + ativoLider.getEspecie().getNome() + "!");
    }
  }

  private void processarGolpeIA(AcaoBatalha acaoIA, List<String> mensagens) {
    if (ativoLider != null && ativoLider.estaApto() && ativoJogador != null && ativoJogador.estaApto()) {
      Golpe golpeIA = buscarGolpe(ativoLider, acaoIA.idAlvo());

      if (golpeIA == null) {
        golpeIA = ia.escolherGolpe(ativoLider, ativoJogador);
      }

      if (golpeIA != null) {
        aplicarGolpe(ativoLider, ativoJogador, golpeIA, mensagens);
      }
    }
  }

  // --- Execução de Dano ---

  private void aplicarGolpe(
      Pokemon atacante, Pokemon defensor, Golpe golpe, List<String> mensagens) {
    if (atacante == null || defensor == null || golpe == null || !atacante.estaApto()) {
      return;
    }

    int dano = calculadora.calcular(atacante, defensor, golpe);
    defensor.receberDano(dano);

    mensagens.add(
        String.format(
            "%s usou %s e causou %d de dano em %s!",
            atacante.getEspecie().getNome(),
            golpe.getNome(),
            dano,
            defensor.getEspecie().getNome()));

    adicionarMensagemEfetividade(golpe, defensor, mensagens);

    if (!defensor.estaApto()) {
      mensagens.add(defensor.getEspecie().getNome() + " desmaiou!");
    }
  }

  private void adicionarMensagemEfetividade(
      Golpe golpe, Pokemon defensor, List<String> mensagens) {
    double mult = calculadora.multiplicador(golpe.getTipo(), defensor.getEspecie().getTipo());
    if (mult > 1.0) {
      mensagens.add("Foi super efetivo!");
    } else if (mult < 1.0) {
      mensagens.add("Não foi muito efetivo...");
    }
  }

  // --- Finalização e Gerenciamento de Turno ---

  private ResultadoTurno finalizarTurno(List<String> mensagens) {
    substituirPokemonDesmaiado(ativoLider, equipeLider, lider.getNome(), true, mensagens);
    substituirPokemonDesmaiado(
        ativoJogador, jogador.getEquipe().listar(), jogador.getNome(), false, mensagens);

    ResultadoBatalha resultado = verificarResultado();

    if (resultado == ResultadoBatalha.VITORIA) {
      mensagens.add("Vitória contra " + lider.getNome() + "!");
    } else if (resultado == ResultadoBatalha.DERROTA) {
      mensagens.add("Todos os seus Pokémon desmaiaram!");
    }

    return new ResultadoTurno(mensagens, resultado, getIdAtivoJogador());
  }

  private void substituirPokemonDesmaiado(
      Pokemon ativo,
      List<Pokemon> equipe,
      String nomeTreinador,
      boolean isLider,
      List<String> mensagens) {
    if (ativo != null && !ativo.estaApto()) {
      Pokemon proximo = encontrarProximoApto(equipe);
      if (proximo != null) {
        if (isLider) {
          ativoLider = proximo;
        } else {
          ativoJogador = proximo;
        }
        mensagens.add(nomeTreinador + " enviou " + proximo.getEspecie().getNome() + "!");
      }
    }
  }

  // --- Validações e Métodos Auxiliares ---

  public boolean podeTrocar(Pokemon pokemon) {
    return pokemon != null
        && pokemon.estaApto()
        && ativoJogador != null
        && !pokemon.getId().equals(ativoJogador.getId())
        && jogador.getEquipe().listar().contains(pokemon);
  }

  public void trocarAtivo(Pokemon pokemon) {
    if (podeTrocar(pokemon)) {
      this.ativoJogador = pokemon;
    }
  }

  public ResultadoBatalha verificarResultado() {
    if (desistiu || !jogador.possuiPokemonApto()) {
      return ResultadoBatalha.DERROTA;
    }

    boolean liderTemApto = equipeLider.stream().anyMatch(Pokemon::estaApto);
    if (!liderTemApto) {
      return ResultadoBatalha.VITORIA;
    }

    return ResultadoBatalha.EM_ANDAMENTO;
  }

  public Pokemon obterAtivoJogador() {
    if (ativoJogador == null || !ativoJogador.estaApto()) {
      ativoJogador = encontrarProximoApto(jogador.getEquipe().listar());
    }
    return ativoJogador;
  }

  public Pokemon getAtivoLider() {
    if (ativoLider == null || !ativoLider.estaApto()) {
      ativoLider = encontrarProximoApto(equipeLider);
    }
    return ativoLider;
  }

  private Pokemon definirAlvoItem(String idItem) {
      if (idItem == null || idItem.isBlank()) {
        return null;
      }

      if (idItem.toLowerCase().contains("reviver")) {
        return jogador.getEquipe().listar().stream()
            .filter(p -> !p.estaApto())
            .findFirst()
            .orElse(null);
      }

      Pokemon atual = obterAtivoJogador();
      return (atual != null && atual.estaApto()) ? atual : null;
  }
  private Pokemon encontrarProximoApto(List<Pokemon> lista) {
    return lista.stream().filter(Pokemon::estaApto).findFirst().orElse(null);
  }

  private Golpe buscarGolpe(Pokemon pokemon, String idGolpe) {
    if (pokemon == null || idGolpe == null) {
      return null;
    }
    return pokemon.getEspecie().getGolpes().stream()
        .filter(g -> g.getId().equals(idGolpe))
        .findFirst()
        .orElse(null);
  }

  private ResultadoTurno criarResultadoInvalido(String mensagem) {
    return new ResultadoTurno(List.of(mensagem), verificarResultado(), getIdAtivoJogador());
  }

  private boolean batalhaEmAndamento() {
    return verificarResultado() == ResultadoBatalha.EM_ANDAMENTO;
  }

  private String getIdAtivoJogador() {
    Pokemon ativo = obterAtivoJogador();
    return ativo != null ? ativo.getId() : null;
  }

  public List<Pokemon> getEquipeLider() {
    return equipeLider;
  }

  public LiderGinasio getLider() {
    return lider;
  }
}