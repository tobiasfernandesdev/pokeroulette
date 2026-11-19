# Roleta Kanto

Roleta Kanto é um projeto acadêmico de jogo 2D em Java Swing, inspirado na primeira geração de Pokémon. Esta pasta contém o esqueleto inicial do aplicativo e a estrutura padrão Maven; as regras do jogo ainda serão implementadas.

## Requisitos

- JDK 25 ou superior
- Uma IDE com suporte a projetos Maven, como IntelliJ IDEA, Eclipse, Visual Studio Code ou Apache NetBeans
- Maven instalado para compilar e executar pelo terminal. As IDEs citadas também podem usar a integração Maven incluída nelas.

As telas usam Swing. O Maven baixa o JavaFX 25 para reproduzir os vídeos do menu e da escolha do inicial, incluindo as bibliotecas nativas do sistema operacional.

## Abrir em uma IDE

Abra a pasta `pokeroulette`, que contém o arquivo `pom.xml`.

- **IntelliJ IDEA:** use **File > Open** e selecione a pasta ou o arquivo `pom.xml`. Confirme a importação como projeto Maven.
- **Eclipse:** use **File > Import > Maven > Existing Maven Projects** e selecione esta pasta. Se precisar atualizar as dependências, clique com o botão direito no projeto e escolha **Maven > Update Project**.
- **Visual Studio Code:** instale o **Extension Pack for Java**, abra esta pasta e aceite a importação Maven quando aparecer.
- **Apache NetBeans:** use **File > Open Project** e selecione esta pasta Maven.

Configure o projeto para usar um JDK 25 ou superior. O Maven compila o projeto com `--release 25`. A classe inicial é `br.com.roletakanto.app.Main`.

## Executar

### Prévia do menu e dos iniciais

Use o perfil `previa-menu` para abrir `TestePainelMenu`. O Maven prepara o JavaFX e compila a prévia em um diretório separado. A primeira execução precisa baixar as dependências.

```powershell
mvn -Pprevia-menu javafx:run
```

No **Eclipse**, clique com o botão direito no projeto e escolha **Run As > Maven build...**. Preencha **Goals** com `javafx:run`, **Profiles** com `previa-menu` e selecione na aba **JRE** o JDK do projeto. No **VS Code**, use o mesmo comando no terminal da pasta do projeto se o Maven estiver disponível no PATH.

### Jogo

Na IDE, execute a classe `Main` para abrir a janela inicial simples.

Com Maven instalado, também é possível compilar e executar o JAR pelo terminal:

```powershell
mvn clean package
java -jar target/roleta-kanto-1.0.0.jar
```

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
