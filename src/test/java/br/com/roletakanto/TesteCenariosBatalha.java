package br.com.roletakanto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.roletakanto.controller.ControladorBatalha;
import br.com.roletakanto.controller.ControladorPartida;
import br.com.roletakanto.model.Batalha;
import br.com.roletakanto.model.FasePartida;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Pokemon;
import br.com.roletakanto.model.ResultadoBatalha;
import br.com.roletakanto.model.ResultadoTurno;
import br.com.roletakanto.service.CatalogoJogo;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TesteCenariosBatalha {

  private CatalogoJogo catalogo;
  private String idInicial;

  @BeforeEach
  void preparar() {
    catalogo = new CatalogoJogo();
    idInicial = catalogo.carregarEspecies().keySet().iterator().next();
  }

  @Test
  void deveUsarItemETrocarPokemonDuranteBatalha() {
    ControladorPartida cp = new ControladorPartida(catalogo, new Random(10L));
    ControladorBatalha cb = new ControladorBatalha();

    cp.novaPartida(idInicial);
    // Adiciona um segundo Pokémon apto para garantir o cenário de troca
    cp.getPartida()
        .getJogador()
        .getEquipe()
        .adicionar(catalogo.carregarEspecies().get("pidgey").criarInstancia());

    cb.definirPartida(cp.getPartida());
    cp.confirmarBatalha();

    Batalha batalha = cp.getPartida().getBatalhaAtual();
    Pokemon ativo = batalha.obterAtivoJogador();
    String idGolpe = ativo.getEspecie().getGolpes().get(0).getId();

    ResultadoTurno r1 = cb.executarGolpe(idGolpe);
    assertNotNull(r1);

    int pocoesAntes = quantidadeItens(cp, CatalogoJogo.ID_POCAO);
    if (r1.resultado() == ResultadoBatalha.EM_ANDAMENTO && pocoesAntes > 0) {
      ResultadoTurno rItem = cb.usarItem(CatalogoJogo.ID_POCAO);
      int pocoesDepois = quantidadeItens(cp, CatalogoJogo.ID_POCAO);
      assertNotNull(rItem);
      assertEquals(pocoesAntes - 1, pocoesDepois);
    }

    List<Pokemon> equipe = cp.getPartida().getJogador().getEquipe().listar();
    Pokemon substituto =
        equipe.stream()
            .filter(p -> p != batalha.obterAtivoJogador() && p.estaApto())
            .findFirst()
            .orElse(null);

    if (substituto != null && batalha.verificarResultado() == ResultadoBatalha.EM_ANDAMENTO) {
      ResultadoTurno rTroca = cb.trocarPokemon(substituto.getId());
      assertNotNull(rTroca);
      assertEquals(substituto.getId(), batalha.obterAtivoJogador().getId());
    }
  }

  @Test
  void deveEncerrarBatalhaEPartidaComDerrotaAoDesistir() {
    ControladorPartida cp = new ControladorPartida(catalogo, new Random(20L));
    ControladorBatalha cb = new ControladorBatalha();

    cp.novaPartida(idInicial);
    cb.definirPartida(cp.getPartida());
    cp.confirmarBatalha();

    Batalha batalha = cp.getPartida().getBatalhaAtual();
    cb.desistir();

    assertEquals(
        ResultadoBatalha.DERROTA,
        batalha.verificarResultado(),
        "A desistência deveria finalizar a batalha com DERROTA.");
    assertEquals(
        FasePartida.DERROTA,
        cp.getPartida().getFase(),
        "A fase da partida deveria ser DERROTA após a desistência.");
    assertNull(
        cp.getPartida().getBatalhaAtual(),
        "A batalha atual deveria ser limpa após a desistência.");
  }

  @Test
  void deveExecutarLoopDeBatalhaAteDesfechoValido() {
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
        String idGolpe = escolherGolpeDisponivel(batalha, ativo);
        ultimo = cb.executarGolpe(idGolpe);
      }
      turnos++;
    }

    assertTrue(turnos > 0 && turnos <= limiteSeguranca);
    assertTrue(
        cp.getPartida().getFase() == FasePartida.ROTA
            || cp.getPartida().getFase() == FasePartida.DERROTA);
  }

  private String escolherGolpeDisponivel(Batalha batalha, Pokemon ativo) {
    List<Golpe> golpes = ativo.getEspecie().getGolpes();
    if (golpes.size() > 1 && batalha.possuiUsoDisponivel(ativo, golpes.get(1))) {
      return golpes.get(1).getId();
    }
    return golpes.get(0).getId();
  }

  private int quantidadeItens(ControladorPartida controlador, String idItem) {
    var pilha = controlador.getPartida().getJogador().getMochila().obterPilhaPorId(idItem);
    return pilha == null ? 0 : pilha.getQuantidade();
  }
}