package br.com.roletakanto.view;

import java.awt.BorderLayout;
import java.awt.event.HierarchyEvent;
import java.net.URL;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

/** O JFXPanel hospeda a cena do JavaFX em um componente Swing. */
final class FundoVideoMenu extends JFXPanel {
    private static final long serialVersionUID = 1L;

    private static final String CAMINHO_VIDEO = "/assets/backgrounds/menuOficialBG.mp4";

    // O player é criado, usado e liberado na thread do JavaFX.
    private transient MediaPlayer player;

    FundoVideoMenu() {
        setLayout(new BorderLayout());
        setFocusable(false);

        // O Swing avisa quando o menu aparece ou sai da tela.
        addHierarchyListener(evento -> {
            if ((evento.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                boolean visivel = isShowing();
                // Agenda a reprodução na thread do JavaFX a partir do evento do Swing.
                Platform.runLater(() -> {
                    if (visivel) {
                        iniciarVideo();
                    } else {
                        liberarVideo();
                    }
                });
            }
        });
    }

    private void iniciarVideo() {
        if (player != null) {
            return;
        }

        // Busca o MP4 nos recursos do projeto, inclusive quando o jogo está em um JAR.
        URL arquivo = getClass().getResource(CAMINHO_VIDEO);
        if (arquivo == null) {
            System.err.println("Fundo do menu ausente em " + CAMINHO_VIDEO);
            return;
        }

        try {
            // Media identifica o arquivo e MediaPlayer controla a reprodução.
            Media midia = new Media(arquivo.toExternalForm());
            MediaPlayer novoPlayer = new MediaPlayer(midia);
            player = novoPlayer;

            // MediaView exibe os quadros. O tamanho e a posição são calculados abaixo.
            MediaView video = new MediaView(novoPlayer);
            video.setManaged(false);
            video.setSmooth(true);
            Pane area = new Pane(video);

            // Recalcula o enquadramento quando a janela muda de tamanho.
            Runnable ajustar = () -> preencherArea(area, video, midia);
            area.widthProperty().addListener((obs, anterior, atual) -> ajustar.run());
            area.heightProperty().addListener((obs, anterior, atual) -> ajustar.run());
            // As dimensões do MP4 ficam disponíveis quando o player está pronto.
            novoPlayer.setOnReady(ajustar);
            midia.setOnError(() -> registrarFalha(midia.getError()));
            novoPlayer.setOnError(() -> registrarFalha(novoPlayer.getError()));

            // Instala a cena no JFXPanel e inicia o vídeo em loop e sem áudio.
            novoPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            novoPlayer.setMute(true);
            setScene(new Scene(area, Color.rgb(252, 248, 230)));
            novoPlayer.play();
        } catch (MediaException erro) {
            registrarFalha(erro);
        }
    }

    private static void preencherArea(Pane area, MediaView video, Media midia) {
        double largura = area.getWidth();
        double altura = area.getHeight();
        if (largura <= 0 || altura <= 0 || midia.getWidth() <= 0 || midia.getHeight() <= 0) {
            return;
        }

        // Como background-size: cover no CSS, o maior fator de escala cobre os dois eixos.
        // Aplicar a mesma escala à largura e à altura preserva a proporção do vídeo.
        double escala = Math.max(largura / midia.getWidth(), altura / midia.getHeight());
        double larguraVideo = midia.getWidth() * escala;
        double alturaVideo = midia.getHeight() * escala;
        video.setFitWidth(larguraVideo);
        video.setFitHeight(alturaVideo);

        // Centraliza o vídeo para dividir igualmente o recorte que sobra nas bordas.
        video.setX((largura - larguraVideo) / 2);
        video.setY((altura - alturaVideo) / 2);
    }

    private void liberarVideo() {
        if (player != null) {
            // Ao sair do menu, devolve os recursos usados pela reprodução.
            player.dispose();
            player = null;
            setScene(null);
        }
    }

    private static void registrarFalha(MediaException erro) {
        System.err.println("Não foi possível reproduzir o fundo do menu: " + erro.getMessage());
    }
}
