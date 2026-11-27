# Origem das imagens adicionadas

As imagens são locais: a interface funciona sem consultar sites durante a partida.
Os arquivos existentes (iniciais, Brock e vídeos) foram mantidos. Os PNGs importados
foram preservados sem alterações; na tela, o Swing desconsidera a sobra transparente
ao redor dos sprites e os redimensiona mantendo a proporção. O redimensionamento usa
vizinho mais próximo para conservar os pixels. Os cenários não passam por esse corte
de margens transparentes.

## Acervos e direitos

- [PokeAPI/sprites](https://github.com/PokeAPI/sprites): 21 sprites da geração V e quatro ícones de itens. A [licença original](licenses/PokeAPI-LICENCE.txt) inclui CC0, mas declara expressamente que o conteúdo das imagens pertence à The Pokémon Company. Não interpretar CC0 como autorização sobre personagens e marcas.
- [pret/pokefirered](https://github.com/pret/pokefirered): retratos de Misty, Lt. Surge e Erika, extraídos do acervo gráfico de FireRed. São gráficos do jogo Pokémon, não obras originais deste projeto. Direitos dos respectivos titulares (Nintendo, Game Freak e The Pokémon Company).
- [smogon/pokemon-showdown-client](https://github.com/smogon/pokemon-showdown-client): cenário `bg-gen3-arena.png`. O projeto declara AGPLv3 para seu código; isso não deve ser confundido com uma licença independente para os gráficos Pokémon. Nenhum código desse repositório foi incorporado.

A origem pública no GitHub não garante permissão para qualquer uso. Estes créditos registram as fontes do trabalho acadêmico; não concedem direitos adicionais sobre os recursos.

## Como as imagens entram na tela

- `VisualPokemon` procura o arquivo pelo ID do catálogo e guarda o resultado na memória.
- `PainelRota` mostra os sprites da equipe, os ícones da mochila e a imagem do prêmio sorteado.
- `DesenhadorRoleta` exibe miniaturas quando há espaço nos setores; os nomes continuam visíveis.
- `RetratoLider` desenha o cenário atrás do líder. Exibe apenas a área de batalha do PNG (x=155, y=95, largura=430, altura=290), sem as molduras do arquivo original. É uma arena ilustrativa, não uma imagem específica de cada ginásio.
- A **Pokébola Dourada** é um item próprio do trabalho. Usamos **Luxury Ball** como representação visual provisória por seus detalhes dourados. O nome, o efeito e o ID do item não foram modificados.
- Eventos continuam com texto porque não correspondem a um único item ou Pokémon.

## Arte da campanha

Os oito cenários de rota e de ginásio são desenhados no código em `CenarioPixel`.
O PNG `items/cura_status.png` é uma miniatura original feita para Roleta Kanto.
Os sprites existentes de Bulbasaur e Wartortle foram renomeados de acordo com a
espécie ilustrada. Squirtle recebeu um sprite da PokéAPI.

## Arquivos e versões exatas

As URLs abaixo fixam o commit, evitando que mudanças futuras no repositório alterem a referência.

| Arquivo local em assets | Origem |
| --- | --- |
| `pokemon/squirtle.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/7.png) |
| `pokemon/pidgey.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/16.png) |
| `pokemon/rattata.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/19.png) |
| `pokemon/pikachu.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/25.png) |
| `pokemon/zubat.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/41.png) |
| `pokemon/geodude.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/74.png) |
| `pokemon/sandshrew.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/27.png) |
| `pokemon/oddish.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/43.png) |
| `pokemon/abra.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/63.png) |
| `pokemon/meowth.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/52.png) |
| `pokemon/gastly.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/92.png) |
| `pokemon/cubone.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/104.png) |
| `pokemon/vulpix.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/37.png) |
| `pokemon/onix.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/95.png) |
| `pokemon/staryu.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/120.png) |
| `pokemon/starmie.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/121.png) |
| `pokemon/voltorb.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/100.png) |
| `pokemon/raichu.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/26.png) |
| `pokemon/bellsprout.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/69.png) |
| `pokemon/weepinbell.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/70.png) |
| `pokemon/gloom.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/pokemon/versions/generation-v/black-white/44.png) |
| `items/pocao.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/items/potion.png) |
| `items/super_pocao.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/items/super-potion.png) |
| `items/reviver.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/items/revive.png) |
| `items/pokebola_dourada.png` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/sprites/items/luxury-ball.png) |
| `leaders/misty.png` | [arquivo original](https://raw.githubusercontent.com/pret/pokefirered/ce6406d8c2eaf46d543c8a2606de0c3cb1951982/graphics/trainers/front_pics/leader_misty_front_pic.png) |
| `leaders/lt_surge.png` | [arquivo original](https://raw.githubusercontent.com/pret/pokefirered/ce6406d8c2eaf46d543c8a2606de0c3cb1951982/graphics/trainers/front_pics/leader_lt_surge_front_pic.png) |
| `leaders/erika.png` | [arquivo original](https://raw.githubusercontent.com/pret/pokefirered/ce6406d8c2eaf46d543c8a2606de0c3cb1951982/graphics/trainers/front_pics/leader_erika_front_pic.png) |
| `backgrounds/arena.png` | [arquivo original](https://raw.githubusercontent.com/smogon/pokemon-showdown-client/36ba1106bbb0e95b457e29a07b5f9e9db29ac85a/play.pokemonshowdown.com/fx/bg-gen3-arena.png) |
| `licenses/PokeAPI-LICENCE.txt` | [arquivo original](https://raw.githubusercontent.com/PokeAPI/sprites/c0542f7c51215d9672439012838c99f520596cbd/LICENCE.txt) |

