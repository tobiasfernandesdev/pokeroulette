package br.com.roletakanto.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.com.roletakanto.model.EspeciePokemon;
import br.com.roletakanto.model.Golpe;
import br.com.roletakanto.model.Insignia;
import br.com.roletakanto.model.Item;
import br.com.roletakanto.model.LiderGinasio;
import br.com.roletakanto.model.Roleta;
import br.com.roletakanto.model.Rota;
import br.com.roletakanto.model.SegmentoRoleta;
import br.com.roletakanto.model.TipoPokemon;
import br.com.roletakanto.model.TipoResultadoRoleta;
import br.com.roletakanto.model.item.Pocao;
import br.com.roletakanto.model.item.PokebolaDourada;
import br.com.roletakanto.model.item.Radar;
import br.com.roletakanto.model.item.Reviver;
import br.com.roletakanto.model.item.SuperPocao;

public final class CatalogoJogo {
  public static final String ID_POCAO = "pocao";
  public static final String ID_SUPER_POCAO = "super_pocao";
  public static final String ID_REVIVER = "reviver";
  public static final String ID_RADAR = "radar";
  public static final String ID_POKEBOLA_DOURADA = "pokebola_dourada";

  private static final int PESO_ESPECIE = 15;
  private static final int PESO_POCAO = 15;
  private static final int PESO_SUPER_POCAO = 5;
  private static final int PESO_REVIVER = 5;
  private static final int PESO_POKEBOLA_DOURADA = 10;
  private static final int PESO_RADAR = 10;
  private static final int PESO_EVENTO = 10;

  /** Retorna as 15 espécies capturáveis e as 8 exclusivas dos líderes. */
  public Map<String, EspeciePokemon> carregarEspecies() {
    Map<String, EspeciePokemon> especies = new LinkedHashMap<>();

    adicionar(especies, especie("bulbasaur", "Bulbasaur", TipoPokemon.PLANTA, 70, 17, 13, 12));
    adicionar(especies, especie("charmander", "Charmander", TipoPokemon.FOGO, 62, 20, 10, 17));
    adicionar(especies, especie("squirtle", "Squirtle", TipoPokemon.AGUA, 72, 15, 17, 10));

    adicionar(especies, especie("pidgey", "Pidgey", TipoPokemon.NORMAL, 48, 14, 10, 18));
    adicionar(especies, especie("rattata", "Rattata", TipoPokemon.NORMAL, 45, 17, 8, 20));
    adicionar(especies, especie("pikachu", "Pikachu", TipoPokemon.ELETRICO, 52, 20, 9, 21));
    adicionar(especies, especie("zubat", "Zubat", TipoPokemon.NORMAL, 50, 15, 11, 19));
    adicionar(especies, especie("geodude", "Geodude", TipoPokemon.ROCHA, 60, 20, 20, 5));
    adicionar(especies, especie("sandshrew", "Sandshrew", TipoPokemon.ROCHA, 58, 17, 17, 9));
    adicionar(especies, especie("oddish", "Oddish", TipoPokemon.PLANTA, 55, 17, 14, 8));
    adicionar(especies, especie("abra", "Abra", TipoPokemon.NORMAL, 40, 23, 6, 19));
    adicionar(especies, especie("meowth", "Meowth", TipoPokemon.NORMAL, 48, 16, 9, 20));
    adicionar(especies, especie("gastly", "Gastly", TipoPokemon.NORMAL, 46, 22, 8, 18));
    adicionar(especies, especie("cubone", "Cubone", TipoPokemon.ROCHA, 58, 17, 16, 9));
    adicionar(especies, especie("vulpix", "Vulpix", TipoPokemon.FOGO, 52, 18, 11, 16));

    adicionar(especies, especie("onix", "Onix", TipoPokemon.ROCHA, 82, 18, 24, 8));
    adicionar(especies, especie("staryu", "Staryu", TipoPokemon.AGUA, 64, 19, 12, 18));
    adicionar(especies, especie("starmie", "Starmie", TipoPokemon.AGUA, 78, 22, 16, 21));
    adicionar(especies, especie("voltorb", "Voltorb", TipoPokemon.ELETRICO, 58, 17, 10, 22));
    adicionar(especies, especie("raichu", "Raichu", TipoPokemon.ELETRICO, 76, 23, 14, 22));
    adicionar(especies, especie("bellsprout", "Bellsprout", TipoPokemon.PLANTA, 58, 19, 9, 12));
    adicionar(especies, especie("weepinbell", "Weepinbell", TipoPokemon.PLANTA, 68, 21, 13, 14));
    adicionar(especies, especie("gloom", "Gloom", TipoPokemon.PLANTA, 74, 18, 16, 10));

    return Map.copyOf(especies);
  }

  public List<Rota> criarRotas(Random random) {
    if (random == null) {
      throw new IllegalArgumentException("O gerador aleatorio Ã© obrigatorio.");
    }

    List<Rota> rotas =
        List.of(
            rota("rota_1", "Rota 1", "brock", random, "pidgey", "rattata", "pikachu"),
            rota("caverna", "Caverna", "misty", random, "zubat", "geodude", "sandshrew"),
            rota("rota_2", "Rota 2", "lt_surge", random, "oddish", "abra", "meowth"),
            rota("torre_pokemon", "Torre Pokemon", "erika", random, "gastly", "cubone", "vulpix"));
    validarPesosDasRotas(rotas);
    return rotas;
  }

  /** Cria os quatro lÃƒÂ­deres e suas equipes fixas. */
  public List<LiderGinasio> criarLideres() {
    Map<String, EspeciePokemon> especies = carregarEspecies();
    return List.of(
        lider("brock", "Brock", especies, "pedra", "InsiÂ­gnia Pedra", "geodude", "onix"),
        lider("misty", "Misty", especies, "cascata", "InsiÂ­gnia Cascata", "staryu", "starmie"),
        lider(
            "lt_surge",
            "Lt. Surge",
            especies,
            "trovao",
            "Insignia TrovÃ£o",
            "voltorb",
            "pikachu",
            "raichu"),
        lider(
            "erika",
            "Erika",
            especies,
            "arco_iris",
            "Insignia Arco-iris",
            "bellsprout",
            "weepinbell",
            "gloom"));
  }

  public List<Item> criarItens() {
    return List.of(
        new Pocao(ID_POCAO, "PoÃ§Ã£o"),
        new SuperPocao(ID_SUPER_POCAO, "Super poÃ§Ã£o"),
        new Reviver(ID_REVIVER, "Reviver"),
        new Radar(ID_RADAR, "Radar"),
        new PokebolaDourada(ID_POKEBOLA_DOURADA, "Pokebola Dourada"));
  }

  /** Confere quantidades, IDs e pesos definidos pelo manual. */
  public void validarCatalogo() {
    if (carregarEspecies().size() != 23) {
      throw new IllegalStateException("O catalogo deve conter 23 especies.");
    }
    if (criarItens().stream().map(Item::getId).distinct().count() != 5) {
      throw new IllegalStateException("Os itens devem possuir IDs Ãºnicos.");
    }
    List<Rota> rotas = criarRotas(new Random(0));
    if (rotas.size() != 4 || criarLideres().size() != 4) {
      throw new IllegalStateException("A campanha deve possuir quatro rotas e quatro lÃ­Â­deres.");
    }
    validarPesosDasRotas(rotas);
  }

  private EspeciePokemon especie(
      String id, String nome, TipoPokemon tipo, int hp, int ataque, int defesa, int velocidade) {
    return new EspeciePokemon(
        id,
        nome,
        tipo,
        hp,
        ataque,
        defesa,
        velocidade,
        List.of(
            new Golpe("investida", "Investida", TipoPokemon.NORMAL, 12, 0), golpeEspecial(tipo)));
  }

  private Golpe golpeEspecial(TipoPokemon tipo) {
    return switch (tipo) {
      case FOGO -> new Golpe("ataque_fogo", "Ataque de Fogo", tipo, 20, 3);
      case AGUA -> new Golpe("ataque_agua", "Ataque de Ã¡gua", tipo, 20, 3);
      case PLANTA -> new Golpe("ataque_planta", "Ataque de Planta", tipo, 20, 3);
      case ELETRICO -> new Golpe("ataque_eletrico", "Ataque Eletrico", tipo, 20, 3);
      case ROCHA -> new Golpe("ataque_rocha", "Ataque de Rocha", tipo, 20, 3);
      case NORMAL -> new Golpe("ataque_normal", "Ataque Normal", tipo, 20, 3);
    };
  }

  private Rota rota(
      String id,
      String nome,
      String idLider,
      Random random,
      String primeira,
      String segunda,
      String terceira) {
    List<SegmentoRoleta> segmentos = new ArrayList<>();
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.POKEMON, primeira, PESO_ESPECIE));
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.POKEMON, segunda, PESO_ESPECIE));
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.POKEMON, terceira, PESO_ESPECIE));
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.ITEM, ID_POCAO, PESO_POCAO));
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.ITEM, ID_SUPER_POCAO, PESO_SUPER_POCAO));
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.ITEM, ID_REVIVER, PESO_REVIVER));
    segmentos.add(
        new SegmentoRoleta(TipoResultadoRoleta.ITEM, ID_POKEBOLA_DOURADA, PESO_POKEBOLA_DOURADA));
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.ITEM, ID_RADAR, PESO_RADAR));
    // O serviÃƒÂ§o que aplicar o resultado escolherÃƒÂ¡ CENTRO, RECOMPENSA ou CAMINHO_VAZIO em partes
    // iguais.
    segmentos.add(new SegmentoRoleta(TipoResultadoRoleta.EVENTO, "evento", PESO_EVENTO));
    return new Rota(id, nome, new Roleta(segmentos, random), idLider);
  }

  private LiderGinasio lider(
      String id,
      String nome,
      Map<String, EspeciePokemon> especies,
      String idInsignia,
      String nomeInsignia,
      String... idsDaEquipe) {
    List<EspeciePokemon> equipe = new ArrayList<>();
    for (String idEspecie : idsDaEquipe) {
      EspeciePokemon especie = especies.get(idEspecie);
      if (especie == null) {
        throw new IllegalStateException("Especie inexistente para liÂ­der: " + idEspecie);
      }
      equipe.add(especie);
    }
    return new LiderGinasio(id, nome, equipe, new Insignia(idInsignia, nomeInsignia, ""));
  }

  private void adicionar(Map<String, EspeciePokemon> especies, EspeciePokemon especie) {
    if (especies.putIfAbsent(especie.getId(), especie) != null) {
      throw new IllegalStateException("ID de especie duplicado: " + especie.getId());
    }
  }

  private void validarPesosDasRotas(List<Rota> rotas) {
    for (Rota rota : rotas) {
      int total = rota.getRoleta().getSegmentos().stream().mapToInt(SegmentoRoleta::peso).sum();
      if (total != 100) {
        throw new IllegalStateException("Os pesos de " + rota.getNome() + " devem somar 100.");
      }
    }
  }
}
