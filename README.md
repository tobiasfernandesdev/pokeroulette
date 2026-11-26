# Roleta Kanto

Roleta Kanto é um projeto acadêmico de jogo 2D em Java Swing, inspirado na primeira geração de Pokémon. O repositório inclui o domínio do jogo, a janela inicial provisória e uma prévia do menu e da escolha do Pokémon inicial.

## Requisitos

- JDK 25 ou superior. Para a equipe, use de preferência o JDK 25 LTS.
- Uma IDE com suporte a projetos Maven, como IntelliJ IDEA, Eclipse, Visual Studio Code ou Apache NetBeans
- Maven instalado para compilar e executar pelo terminal. As IDEs citadas também podem usar a integração Maven incluída nelas.

As telas usam Swing. O Maven baixa o JavaFX para reproduzir os vídeos do menu e da escolha do inicial, incluindo as bibliotecas nativas do sistema operacional. Os MP4 ficam em `src/main/resources/assets/backgrounds` e são carregados do classpath. O JavaFX é configurado pelo `pom.xml`.

## Abrir em uma IDE

Abra a pasta `pokeroulette`, que contém o arquivo `pom.xml`.

- **IntelliJ IDEA:** use **File > Open** e selecione a pasta ou o arquivo `pom.xml`. Confirme a importação como projeto Maven.
- **Visual Studio Code:** instale o **Extension Pack for Java**, abra esta pasta e aceite a importação Maven quando aparecer.
- **Apache NetBeans:** use **File > Open Project** e selecione esta pasta Maven.

Configure o projeto para usar um JDK 25 ou superior. O Maven compila o projeto com `--release 25`. A classe inicial é `br.com.roletakanto.app.Main`.

### Eclipse

1. Use o Eclipse IDE for Java Developers 2025-12 ou mais recente. Em **Window > Preferences > Java > Installed JREs**, escolha **Add > Standard VM**, adicione a pasta do JDK e selecione-o. Em **Java > Execution Environments**, confira a associação com **JavaSE-25**.
2. Abra **File > Import > Maven > Existing Maven Projects**. Selecione a pasta que contém `pom.xml` e conclua a importação com os perfis padrão.
3. No projeto, use **Maven > Update Project** para atualizar dependências se necessário. Confira em **Properties > Java Build Path** se o projeto reconheceu `JavaSE-25` e o `Maven Dependencies`.
4. Para abrir a janela inicial provisória, abra `Main.java` e escolha **Run As > Java Application**. Para ver o menu e os iniciais com vídeo, use a configuração Maven abaixo.

## Executar

### Prévia do menu, dos iniciais e da roleta

Use o perfil `previa-menu` para abrir `TestePainelMenu`. O fluxo permite iniciar no menu, escolher o Pokémon inicial e abrir a rota com a roleta conectada à partida. O Maven prepara o JavaFX e compila a prévia em um diretório separado. A primeira execução precisa baixar as dependências.

```powershell
mvn -Pprevia-menu javafx:run
```

No **Eclipse**, clique com o botão direito no projeto e escolha **Run As > Maven build...**. Preencha **Goals** com `javafx:run`, **Profiles** com `previa-menu` e selecione na aba **JRE** o JDK do projeto. O perfil se aplica a essa execução da prévia. No **VS Code**, use o mesmo comando no terminal da pasta do projeto se o Maven estiver disponível no PATH.

Os sprites de Pokémon, itens e líderes usados na rota ficam em `src/main/resources/assets` e funcionam sem internet. As fontes, versões e condições de uso estão em [Créditos das imagens](src/main/resources/assets/CREDITOS.md). A arena aparece atrás do próximo líder; a mochila e o resultado do giro exibem ícones dos itens.

Para dar mais espaço à preparação, cada rota oferece dois giros. O primeiro caminho inclui Oddish como opção de equipe contra Brock. Em batalha, golpes especiais do jogador têm até 10 usos por Pokémon; os líderes mantêm o limite original, e seus ataques causam 65% do dano calculado.

Golpes de Planta podem envenenar e golpes Elétricos podem paralisar, cada um com 25% de chance. Pokémon envenenados perdem 5% do HP máximo ao fim de cada turno, e os paralisados podem perder a ação. A Cura de Status remove essas condições; os Centros Pokémon também as removem ao restaurar a equipe. Cada rota ainda oferece um evento opcional, usado uma vez: descansar para restaurar equipe e reserva ou receber uma Super Poção.

### Janela inicial provisória

Na IDE, execute a classe `Main` para abrir a janela inicial simples. A classe `RoletaKantoApp` encaminha para a mesma entrada.

Com Maven instalado, também é possível compilar e executar o JAR pelo terminal:

```powershell
mvn clean package
java -jar target/roleta-kanto-1.0.0.jar
```

### Versão jogável pelo terminal

Para jogar sem abrir janelas, execute `MainTerminal` pela IDE ou use Maven:

```powershell
mvn -q compile
java -cp target/classes br.com.roletakanto.app.MainTerminal
```

Digite o número da opção e pressione Enter. Você pode escolher o inicial, girar a roleta, usar itens, organizar a equipe e enfrentar os líderes. Use **Salvar partida** para gravar a fase atual, inclusive durante uma batalha. O terminal usa `saves/partida-terminal.dat`.

### Compilar e rodar os testes

```powershell
mvn clean compile
mvn test
```

O segundo comando executa `TesteFluxoBatalha` e `TesteCenariosBatalha`. A prévia das telas é executada à parte.

## Estrutura

```text
pokeroulette/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/br/com/roletakanto/
    │   │   ├── app/          # inicialização do aplicativo
    │   │   ├── controller/   # comandos e fluxo entre telas e regras
    │   │   ├── model/        # estado e regras do domínio
    │   │   │   └── item/     # itens e efeitos
    │   │   ├── persistence/  # gravação e leitura local
    │   │   ├── service/      # roleta, catálogo e cálculos
    │   │   ├── util/         # formatação e validações comuns
    │   │   └── view/         # janela e painéis Swing
    │   └── resources/assets/
    │       ├── backgrounds/
    │       ├── icons/
    │       ├── pokemon/
    │       └── sounds/
    └── test/java/br/com/roletakanto/
```

O código fica em `src/main/java`, os recursos visuais e sonoros em `src/main/resources`, e os testes em `src/test/java`. O arquivo `.form` específico do editor visual do IntelliJ foi removido; a janela inicial é criada com Swing padrão para não depender desse editor.

## Próxima etapa

Comece completando as classes de modelo descritas no Manual de Desenvolvimento. Depois ligue-as aos controladores e painéis. Mantenha as regras fora dos componentes Swing para que o comportamento do jogo não dependa da IDE.
