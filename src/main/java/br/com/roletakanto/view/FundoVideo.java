package br.com.roletakanto.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.HierarchyEvent;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

/** Hospeda o vídeo JavaFX atrás dos controles Swing de uma tela. */
final class FundoVideo extends JFXPanel {
    private static final long serialVersionUID = 1L;
    // A coleção e os players são acessados somente pela thread do JavaFX.
    private static final Set<MediaPlayer> PLAYERS = new HashSet<>();

    private final String caminhoVideo;
    private final boolean repetirVideo;

    // O player é criado, usado e liberado na thread do JavaFX.
    private transient MediaPlayer player;
    private volatile boolean deveTocar;

    FundoVideo(String nomeArquivo, boolean repetirVideo) {
        caminhoVideo = "/assets/backgrounds/" + nomeArquivo;
        this.repetirVideo = repetirVideo;
        setFocusable(false);

        // O Swing avisa quando o painel aparece ou sai da tela.
        addHierarchyListener(evento -> {
            if ((evento.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                deveTocar = isShowing();
                // Agenda a reprodução na thread do JavaFX a partir do evento do Swing.
                Platform.runLater(() -> {
                    if (deveTocar) {
                        iniciarVideo();
                    } else {
                        pausarVideo();
                    }
                });
            }
        });
    }

    void posicionarControles(JPanel controles, double posicaoVertical) {
        // Duas linhas flexíveis mantêm os controles na altura desejada ao redimensionar.
        GridBagLayout layout = new GridBagLayout();
        layout.rowHeights = new int[3];
        layout.rowWeights = new double[] {posicaoVertical, 0, 1 - posicaoVertical};
        setLayout(layout);

        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridy = 1;
        add(controles, posicao);
    }

    private void iniciarVideo() {
        if (player != null) {
            player.play();
            return;
        }

        // Busca o MP4 nos recursos do projeto, inclusive quando o jogo está em um JAR.
        URL arquivo = getClass().getResource(caminhoVideo);
        if (arquivo == null) {
            System.err.println("Vídeo de fundo ausente em " + caminhoVideo);
            return;
        }

        try {
            // Media identifica o arquivo e MediaPlayer controla a reprodução.
            Media midia = new Media(arquivo.toExternalForm());
            MediaPlayer novoPlayer = new MediaPlayer(midia);
            player = novoPlayer;
            PLAYERS.add(novoPlayer);

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
            novoPlayer.setOnReady(() -> {
                ajustar.run();
                if (deveTocar) {
                    novoPlayer.play();
                }
            });
            midia.setOnError(() -> tratarFalha(novoPlayer, midia.getError()));
            novoPlayer.setOnError(() -> tratarFalha(novoPlayer, novoPlayer.getError()));

            // Em um único ciclo, o player termina no último quadro e a cena continua visível.
            novoPlayer.setCycleCount(repetirVideo ? MediaPlayer.INDEFINITE : 1);
            novoPlayer.setMute(true);
            setScene(new Scene(area, Color.web("#7E59D6")));
        } catch (MediaException erro) {
            tratarFalha(player, erro);
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

    private void pausarVideo() {
        if (player != null) {
            // Mantém a cena ligada enquanto a janela existir. O JFXPanel pode
            // ter atualizações de escala pendentes ao alternar os cartões.
            player.pause();
            player.seek(javafx.util.Duration.ZERO);
        }
    }

    private void tratarFalha(MediaPlayer playerComFalha, MediaException erro) {
        // Um aviso atrasado de um player antigo não deve interromper o vídeo atual.
        if (playerComFalha == player) {
            System.err.println("Não foi possível reproduzir " + caminhoVideo + ": " + erro.getMessage());
            if (player != null) {
                PLAYERS.remove(player);
                player.dispose();
            }
            player = null;
        }
    }

    static void encerrarJavaFxDaPrevia() {
        Platform.runLater(() -> {
            PLAYERS.forEach(MediaPlayer::dispose);
            PLAYERS.clear();
            Platform.exit();
        });
    }
}
