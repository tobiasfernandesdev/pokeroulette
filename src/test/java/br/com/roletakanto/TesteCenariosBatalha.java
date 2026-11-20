package br.com.roletakanto;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.service.CatalogoJogo;
import java.util.List;
import java.util.Random;

public final class TesteCenariosBatalha {

  public static void main(String[] args) {
    CatalogoJogo catalogo = new CatalogoJogo();
    String idInicial = catalogo.carregarEspecies().keySet().iterator().next();

    testarUsoItemETroca(catalogo, idInicial);
    testarDesistencia(catalogo, idInicial);
    testarVitoria(catalogo, idInicial);

    System.out.println("=== TODOS OS CENARIOS EXTRAS CONCLUIDOS ===");
  }

  private static void testarUsoItemETroca(CatalogoJogo catalogo, String idInicial) {
    System.out.println("--- Cenario 1: Uso de Item e Troca ---");
    ControladorPartida cp = new ControladorPartida(catalogo, new Random(10L));
    ControladorBatalha cb = new ControladorBatalha();

    cp.novaPartida(idInicial);
    cb.definirPartida(cp.getPartida());
    cp.confirmarBatalha();

    Batalha batalha = cp.getPartida().getBatalhaAtual();
    Pokemon ativo = batalha.obterAtivoJogador();
    String idGolpe = ativo.getEspecie().getGolpes().get(0).getId();

    ResultadoTurno r1 = cb.executarGolpe(idGolpe);
    imprimirMensagens(r1);

    int pocoesAntes = quantidadeItens(cp, CatalogoJogo.ID_POCAO);
    if (r1.resultado() == ResultadoBatalha.EM_ANDAMENTO && pocoesAntes > 0) {
      ResultadoTurno rItem = cb.usarItem(CatalogoJogo.ID_POCAO);
      int pocoesDepois = quantidadeItens(cp, CatalogoJogo.ID_POCAO);
      System.out.println("[OK] Uso de pocao executado. Pocoes antes: " + pocoesAntes
          + ", depois: " + pocoesDepois);
      imprimirMensagens(rItem);
    }

    List<Pokemon> equipe = cp.getPartida().getJogador().getEquipe().listar();
    Pokemon substituto = null;
    for (Pokemon p : equipe) {
      if (p != batalha.obterAtivoJogador() && p.estaApto()) {
        substituto = p;
        break;
      }
    }

    if (substituto != null && batalha.verificarResultado() == ResultadoBatalha.EM_ANDAMENTO) {
      ResultadoTurno rTroca = cb.trocarPokemon(substituto.getId());
      System.out.println("[OK] Troca executada para: " + substituto.getEspecie().getNome());
      imprimirMensagens(rTroca);
    } else {
      System.out.println("[INFO] Sem segundo Pokemon apto na equipe ativa para testar troca neste cenario.");
    }
  }

  private static void testarDesistencia(CatalogoJogo catalogo, String idInicial) {
    System.out.println("--- Cenario 2: Desistencia ---");
    ControladorPartida cp = new ControladorPartida(catalogo, new Random(20L));
    ControladorBatalha cb = new ControladorBatalha();

    cp.novaPartida(idInicial);
    cb.definirPartida(cp.getPartida());
    cp.confirmarBatalha();

    Batalha batalha = cp.getPartida().getBatalhaAtual();
    cb.desistir();

    if (batalha.verificarResultado() != ResultadoBatalha.DERROTA) {
      throw new AssertionError("A desistencia deveria finalizar a batalha com DERROTA.");
    }
    if (cp.getPartida().getFase() != FasePartida.DERROTA) {
      throw new AssertionError("A fase da partida deveria ser DERROTA apos a desistencia.");
    }
    if (cp.getPartida().getBatalhaAtual() != null) {
      throw new AssertionError("A batalha atual deveria ser limpa apos a desistencia.");
    }

    System.out.println("[OK] Desistencia executada. Resultado da batalha: "
        + batalha.verificarResultado()
        + " | Fase da partida: " + cp.getPartida().getFase()
        + " | Batalha atual encerrada.");
  }

  private static void testarVitoria(CatalogoJogo catalogo, String idInicial) {
    System.out.println("--- Cenario 3: Tentativa de Vitoria ---");
    ControladorPartida cp = new ControladorPartida(catalogo, new Random(30L));
    ControladorBatalha cb = new ControladorBatalha();

    cp.novaPartida(idInicial);
    cb.definirPartida(cp.getPartida());
    cp.confirmarBatalha();

    ResultadoTurno ultimo = null;
    int turnos = 0;
    int limiteSeguranca = 100;

    while ((ultimo == null || ultimo.resultado() == ResultadoBatalha.EM_ANDAMENTO)
        && turnos < limiteSeguranca) {
      Batalha batalha = cp.getPartida().getBatalhaAtual();
      Pokemon ativo = batalha.obterAtivoJogador();
      if (ativo == null || !ativo.estaApto()) {
        break;
      }

      int pocoes = quantidadeItens(cp, CatalogoJogo.ID_POCAO);
      if (ativo.getHpAtual() > 0
          && ativo.getHpAtual() <= ativo.getHpMaximo() * 0.4
          && pocoes > 0) {
        ultimo = cb.usarItem(CatalogoJogo.ID_POCAO);
      } else {
        String idGolpe = escolherMelhorGolpe(ativo);
        ultimo = cb.executarGolpe(idGolpe);
      }
      turnos++;
    }

    ResultadoBatalha resFinal = (ultimo != null)
        ? ultimo.resultado()
        : cp.getPartida().getBatalhaAtual().verificarResultado();
    System.out.println("[OK] Fim do loop em " + turnos + " turnos. Resultado: " + resFinal
        + " | Fase da partida: " + cp.getPartida().getFase());
  }

  private static String escolherMelhorGolpe(Pokemon ativo) {
    if (ativo.getEspecie().getGolpes().size() > 1) {
      return ativo.getEspecie().getGolpes().get(1).getId();
    }
    return ativo.getEspecie().getGolpes().get(0).getId();
  }

  private static int quantidadeItens(ControladorPartida controlador, String idItem) {
    var pilha = controlador.getPartida()
        .getJogador()
        .getMochila()
        .obterPilhaPorId(idItem);
    return pilha == null ? 0 : pilha.getQuantidade();
  }

  private static void imprimirMensagens(ResultadoTurno resultado) {
    if (resultado == null) {
      return;
    }
    for (String msg : resultado.mensagens()) {
      System.out.println("  " + msg);
    }
  }
}