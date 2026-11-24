package br.com.roletakanto.app;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.ContextoUsoItem;
import br.com.roletakanto.model.Equipe;
import br.com.roletakanto.model.EspeciePokemon;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Insignia;
import br.com.roletakanto.model.Jogador;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Partida;
import br.com.roletakanto.model.PilhaItem;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.persistence.RepositorioPartidaArquivo;
import br.com.roletakanto.service.CatalogoJogo;
import br.com.roletakanto.util.FormatadorTexto;

/** Ponto de entrada para testar a campanha pelo terminal. */
public final class MainTerminal {
  private static final Path CAMINHO_SAVE = Path.of("saves", "partida-terminal.dat");

  private final Scanner entrada;
  private final ControladorPartida controlador;
  private final List<EspeciePokemon> iniciais;

  private MainTerminal(Scanner entrada) {
    this.entrada = entrada;
    CatalogoJogo catalogo = new CatalogoJogo();
    controlador = new ControladorPartida(
        catalogo, new RepositorioPartidaArquivo(CAMINHO_SAVE), new Random());
    var especies = catalogo.carregarEspecies();
    iniciais = List.of(
        especies.get("bulbasaur"), especies.get("charmander"), especies.get("squirtle"));
  }

  public static void main(String[] args) {
    try (Scanner entrada = new Scanner(System.in)) {
      new MainTerminal(entrada).executar();
    } catch (NoSuchElementException fimDaEntrada) {
      System.out.println();
    }
    System.out.println("Até a próxima!");
  }

  private void executar() {
    while (true) {
      Partida atual = controlador.getPartida();
      boolean podeRetomar = atual != null && atual.podeContinuar();
      System.out.println("\n=== Roleta Kanto ===");
      System.out.println("1. Nova partida\n2. Carregar partida salva");
      if (podeRetomar) {
        System.out.println("3. Retomar partida em andamento");
      }
      System.out.println("0. Sair");

      try {
        switch (lerOpcao(podeRetomar ? 3 : 2)) {
          case 0 -> { return; }
          case 1 -> {
            System.out.println("\nEscolha seu Pokémon inicial.");
            EspeciePokemon inicial = escolher(iniciais, MainTerminal::descreverEspecie);
            if (inicial == null) {
              continue;
            }
            controlador.novaPartida(inicial.getId());
          }
          case 2 -> controlador.continuarPartida();
          case 3 -> { /* A partida em memória já está carregada. */ }
        }
        jogar(controlador.getPartida());
      } catch (IllegalStateException erro) {
        System.out.println(erro.getMessage());
      }
    }
  }

  private void jogar(Partida partida) {
    ControladorBatalha combate = new ControladorBatalha(partida);
    while (true) {
      try {
        boolean continuar = switch (partida.getFase()) {
          case ROTA -> mostrarRota(partida);
          case ORGANIZACAO -> organizarEquipe(partida);
          case BATALHA -> jogarTurno(partida, combate);
          case DERROTA, VITORIA_FINAL -> {
            mostrarResultado(partida);
            yield false;
          }
          default -> {
            System.out.println("Fase indisponível no terminal: " + partida.getFase());
            yield false;
          }
        };
        if (!continuar) {
          return;
        }
      } catch (IllegalStateException erro) {
        System.out.println(erro.getMessage());
      }
    }
  }

  private boolean mostrarRota(Partida partida) {
    System.out.printf("%n=== %s (%d/%d) ===%n", partida.getRotaAtual().getNome(),
        partida.getNumeroRotaAtual(), partida.getTotalRotas());
    System.out.printf("Próximo líder: %s | Giros restantes: %d | Insígnias: %d/%d%n",
        partida.getLiderAtual().getNome(), partida.getRotaAtual().getGirosRestantes(),
        partida.getInsignias().size(), partida.getTotalRotas());
    System.out.print("""
        1. Girar roleta
        2. Abrir mochila
        3. Ver equipe e reserva
        4. Organizar equipe para o ginásio
        5. Salvar partida
        0. Voltar ao menu
        """);

    switch (lerOpcao(5)) {
      case 0 -> { return false; }
      case 1 -> System.out.println(controlador.girarRoleta());
      case 2 -> usarItemNaRota(partida);
      case 3 -> mostrarEquipeEReserva(partida.getJogador());
      case 4 -> controlador.navegarParaFase(FasePartida.ORGANIZACAO);
      case 5 -> salvar();
    }
    return true;
  }

  private void usarItemNaRota(Partida partida) {
    Jogador jogador = partida.getJogador();
    PilhaItem pilha = escolherItem(partida);
    if (pilha == null) {
      return;
    }

    String idItem = pilha.getItem().getId();
    if (CatalogoJogo.ID_POKEBOLA_DOURADA.equals(idItem)) {
      System.out.println(controlador.usarGiroBonus());
      return;
    }

    System.out.println("Escolha um Pokémon da equipe para usar o item.");
    Pokemon alvo = escolher(jogador.getEquipe().listar(), MainTerminal::descreverPokemon);
    if (alvo != null) {
      var resultado = jogador.getMochila().usar(idItem, new ContextoUsoItem(partida, null, alvo));
      System.out.println(resultado.mensagem());
    }
  }

  private boolean organizarEquipe(Partida partida) {
    Jogador jogador = partida.getJogador();
    LiderGinasio lider = partida.getLiderAtual();
    boolean podeVoltarParaRota = partida.getRotaAtual().temGiroDisponivel();
    System.out.println("\n=== Preparação para enfrentar " + lider.getNome() + " ===");
    mostrarEquipeEReserva(jogador);
    System.out.print("""
        1. Mover da reserva para a equipe
        2. Mover da equipe para a reserva
        3. Escolher quem começa a batalha
        4. Desafiar líder
        5. Salvar partida
        """);
    if (podeVoltarParaRota) {
      System.out.println("6. Voltar à rota");
    }
    System.out.println("0. Voltar ao menu");

    switch (lerOpcao(podeVoltarParaRota ? 6 : 5)) {
      case 0 -> { return false; }
      case 1 -> {
        System.out.println("Escolha um Pokémon da reserva.");
        Pokemon pokemon = escolher(jogador.getReserva().listar(), MainTerminal::descreverPokemon);
        if (pokemon != null) {
          controlador.moverParaEquipe(pokemon.getId());
        }
      }
      case 2 -> {
        System.out.println("Escolha um Pokémon da equipe.");
        Pokemon pokemon = escolher(jogador.getEquipe().listar(), MainTerminal::descreverPokemon);
        if (pokemon != null) {
          controlador.moverParaReserva(pokemon.getId());
        }
      }
      case 3 -> escolherPrimeiro(jogador.getEquipe());
      case 4 -> {
        if (!podeVoltarParaRota || confirmar("Ainda há giros disponíveis. Começar a batalha?")) {
          controlador.confirmarBatalha();
        }
      }
      case 5 -> salvar();
      case 6 -> controlador.navegarParaFase(FasePartida.ROTA);
    }
    return true;
  }

  private void escolherPrimeiro(Equipe equipe) {
    System.out.println("Escolha o primeiro Pokémon apto da equipe.");
    List<Pokemon> membros = equipe.listar();
    Pokemon primeiro = escolher(membros.stream().filter(Pokemon::estaApto).toList(),
        MainTerminal::descreverPokemon);
    if (primeiro != null) {
      equipe.trocarPosicao(0, membros.indexOf(primeiro));
    }
  }

  private boolean jogarTurno(Partida partida, ControladorBatalha combate) {
    Batalha batalha = partida.getBatalhaAtual();
    Pokemon ativo = batalha.obterAtivoJogador();
    System.out.println("\n=== Batalha contra " + batalha.getLider().getNome() + " ===");
    System.out.println("Seu Pokémon: " + descreverPokemon(ativo));
    System.out.println("Adversário: " + descreverPokemon(batalha.getAtivoLider()));
    System.out.print("""
        1. Atacar
        2. Usar item
        3. Trocar Pokémon
        4. Desistir
        5. Salvar partida
        0. Voltar ao menu
        """);

    switch (lerOpcao(5)) {
      case 0 -> { return false; }
      case 1 -> {
        System.out.println("Escolha um golpe.");
        Golpe golpe = escolher(ativo.getEspecie().getGolpes(), g -> descreverGolpe(batalha, ativo, g));
        if (golpe != null) {
          mostrarTurno(batalha, combate.executarGolpe(golpe.getId()));
        }
      }
      case 2 -> {
        PilhaItem pilha = escolherItem(partida);
        if (pilha != null) {
          mostrarTurno(batalha, combate.usarItem(pilha.getItem().getId()));
        }
      }
      case 3 -> {
        System.out.println("Escolha um substituto apto da equipe.");
        List<Pokemon> substitutos = partida.getJogador().getEquipe().listar().stream()
            .filter(batalha::podeTrocar).toList();
        Pokemon substituto = escolher(substitutos, MainTerminal::descreverPokemon);
        if (substituto != null) {
          mostrarTurno(batalha, combate.trocarPokemon(substituto.getId()));
        }
      }
      case 4 -> {
        if (confirmar("Desistir encerra a campanha. Confirmar?")) {
          combate.desistir();
          System.out.println("Você desistiu da batalha.");
        }
      }
      case 5 -> salvar();
    }
    return true;
  }

  private static void mostrarTurno(Batalha batalha, ResultadoTurno turno) {
    turno.mensagens().forEach(System.out::println);
    if (turno.resultado() == ResultadoBatalha.VITORIA) {
      System.out.println("Insígnia conquistada: " + batalha.getLider().getInsignia().nome());
    }
  }

  private void mostrarResultado(Partida partida) {
    System.out.println(partida.getFase() == FasePartida.VITORIA_FINAL
        ? "\n=== Vitória! Você venceu a campanha. ==="
        : "\n=== Derrota. A campanha terminou. ===");
    List<String> insignias = partida.getInsignias().stream().map(Insignia::nome).toList();
    System.out.println("Insígnias: " + (insignias.isEmpty() ? "nenhuma" : String.join(", ", insignias)));
    System.out.println("1. Salvar resultado e voltar ao menu\n0. Voltar ao menu");
    if (lerOpcao(1) == 1) {
      salvar();
    }
  }

  private void salvar() {
    controlador.salvarPartida();
    System.out.println("Partida salva em " + CAMINHO_SAVE + ".");
  }

  private PilhaItem escolherItem(Partida partida) {
    System.out.println("\nMochila");
    List<PilhaItem> itens = partida.getJogador().getMochila().listar().stream()
        .filter(pilha -> pilha.getItem().podeUsarEm(partida.getFase())).toList();
    return escolher(itens, pilha -> "%s (x%d)".formatted(pilha.getItem().getNome(), pilha.getQuantidade()));
  }

  private static void mostrarEquipeEReserva(Jogador jogador) {
    mostrarPokemon("Equipe", jogador.getEquipe().listar());
    mostrarPokemon("Reserva", jogador.getReserva().listar());
  }

  private static void mostrarPokemon(String grupo, List<Pokemon> membros) {
    System.out.println(grupo + (membros.isEmpty() ? " (vazia)" : ":"));
    membros.forEach(pokemon -> System.out.println("  " + descreverPokemon(pokemon)));
  }

  private static String descreverEspecie(EspeciePokemon especie) {
    return "%s (%s)".formatted(especie.getNome(), especie.getTipo());
  }

  private static String descreverPokemon(Pokemon pokemon) {
    return "%s | HP %s%s".formatted(descreverEspecie(pokemon.getEspecie()),
        FormatadorTexto.formatarHp(pokemon), pokemon.estaApto() ? "" : " | desmaiado");
  }

  private static String descreverGolpe(Batalha batalha, Pokemon ativo, Golpe golpe) {
    String descricao = "%s (%s, potência %d)".formatted(golpe.getNome(), golpe.getTipo(), golpe.getPotencia());
    if (golpe.ehIlimitado()) {
      return descricao + " | ilimitado";
    }
    return descricao + (batalha.possuiUsoDisponivel(ativo, golpe) ? " | uso limitado" : " | esgotado");
  }

  private <T> T escolher(List<T> opcoes, Function<T, String> descrever) {
    if (opcoes.isEmpty()) {
      System.out.println("Nenhuma opção disponível.");
      return null;
    }
    for (int i = 0; i < opcoes.size(); i++) {
      System.out.printf("%d. %s%n", i + 1, descrever.apply(opcoes.get(i)));
    }
    System.out.println("0. Voltar");
    int opcao = lerOpcao(opcoes.size());
    return opcao == 0 ? null : opcoes.get(opcao - 1);
  }

  private boolean confirmar(String pergunta) {
    System.out.println(pergunta);
    System.out.println("1. Confirmar\n0. Cancelar");
    return lerOpcao(1) == 1;
  }

  private int lerOpcao(int maximo) {
    while (true) {
      System.out.print("> ");
      System.out.flush();
      try {
        int opcao = Integer.parseInt(entrada.nextLine().strip());
        if (opcao >= 0 && opcao <= maximo) {
          return opcao;
        }
      } catch (NumberFormatException erro) {
        // O aviso abaixo também atende números fora do intervalo.
      }
      System.out.printf("Digite um número de 0 a %d.%n", maximo);
    }
  }
}
