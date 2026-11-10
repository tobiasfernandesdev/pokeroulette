# Roleta Kanto

Roleta Kanto é um projeto acadêmico de jogo 2D em Java Swing, inspirado na primeira geração de Pokémon. Esta pasta contém o esqueleto inicial do aplicativo e a estrutura padrão Maven; as regras do jogo ainda serão implementadas.

## Requisitos

- JDK 17 ou superior
- Uma IDE com suporte a projetos Maven, como IntelliJ IDEA, Eclipse, Visual Studio Code ou Apache NetBeans
- Maven instalado para compilar e executar pelo terminal. As IDEs citadas também podem usar a integração Maven incluída nelas.

O projeto usa apenas Java SE e Swing, sem bibliotecas externas de aplicação.

## Abrir em uma IDE

Abra a pasta `pokeroullete`, que é a pasta que contém o arquivo `pom.xml`.

- **IntelliJ IDEA:** use **File > Open** e selecione a pasta ou o arquivo `pom.xml`. Confirme a importação como projeto Maven.
- **Eclipse:** use **File > Import > Maven > Existing Maven Projects** e selecione esta pasta.
- **Visual Studio Code:** instale o **Extension Pack for Java**, abra esta pasta e aceite a importação Maven quando aparecer.
- **Apache NetBeans:** use **File > Open Project** e selecione esta pasta Maven.

Configure o projeto para usar um JDK 17 ou superior. A classe inicial é `br.com.roletakanto.app.Main`.

## Executar

Na IDE, execute a classe `Main`. A janela inicial simples confirma que o projeto foi importado e iniciado; ela não representa o jogo completo.

Com Maven instalado, também é possível compilar e executar o JAR pelo terminal:

```powershell
mvn clean package
java -jar target/roleta-kanto-1.0.0.jar
```

## Estrutura

```text
pokeroullete/
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
