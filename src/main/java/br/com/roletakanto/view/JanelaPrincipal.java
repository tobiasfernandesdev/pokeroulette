package br.com.roletakanto.view;
import javax.swing.JFrame;
/** Janela principal que abriga os painéis do jogo. */
public final class JanelaPrincipal extends JFrame { public JanelaPrincipal(NavegadorTelas navegador){super("Roleta Kanto"); setContentPane(navegador); setDefaultCloseOperation(EXIT_ON_CLOSE);} public void mostrar(){pack();setLocationRelativeTo(null);setVisible(true);} public void exibirTela(String idTela){/* TODO */} public void mostrarMensagem(String mensagem){/* TODO */} }
