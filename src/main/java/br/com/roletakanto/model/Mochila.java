package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Armazena os itens do jogador, respeitando os limites de capacidade. */
public final class Mochila implements Serializable {
    private static final int CAPACIDADE_TIPOS = 5;

    private final List<PilhaItem> pilhas = new ArrayList<>();

    /** Retorna uma lista que nao permite alterar a estrutura da mochila. */
    public List<PilhaItem> listar() {
        return List.copyOf(pilhas);
    }

    public int contarTipos() {
        return pilhas.size();
    }

    public PilhaItem obterPilhaPorId(String idItem) {
        if (idItem == null || idItem.isBlank()) {
            return null;
        }

        return pilhas.stream()
                .filter(pilha -> pilha.getItem().getId().equals(idItem))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se a quantidade inteira cabe na mochila. A operacao de adicao
     * e atomica: ou adiciona tudo, ou nao altera nada.
     */
    public boolean podeAdicionar(Item item, int quantidade) {
        if (item == null || quantidade <= 0) {
            return false;
        }

        PilhaItem pilha = obterPilhaPorId(item.getId());
        if (pilha != null) {
            int espacoLivre = pilha.getItem().getLimitePilha() - pilha.getQuantidade();
            return quantidade <= espacoLivre;
        }

        return contarTipos() < CAPACIDADE_TIPOS
                && quantidade <= item.getLimitePilha();
    }

    public boolean adicionar(Item item, int quantidade) {
        if (!podeAdicionar(item, quantidade)) {
            return false;
        }

        PilhaItem pilha = obterPilhaPorId(item.getId());
        if (pilha != null) {
            return pilha.adicionar(quantidade) == quantidade;
        }

        pilhas.add(new PilhaItem(item, quantidade));
        return true;
    }

    public boolean remover(String idItem, int quantidade) {
        if (quantidade <= 0) {
            return false;
        }

        PilhaItem pilha = obterPilhaPorId(idItem);
        if (pilha == null || !pilha.remover(quantidade)) {
            return false;
        }

        if (pilha.getQuantidade() == 0) {
            pilhas.remove(pilha);
        }
        return true;
    }

    public boolean temQuantidade(String idItem, int quantidade) {
        if (quantidade <= 0) {
            return false;
        }

        PilhaItem pilha = obterPilhaPorId(idItem);
        return pilha != null && pilha.getQuantidade() >= quantidade;
    }

    /** Usa uma unidade somente se o efeito do item for aplicado com sucesso. */
    public ResultadoUsoItem usar(String idItem, ContextoUsoItem contexto) {
        if (contexto == null || contexto.partida() == null) {
            return new ResultadoUsoItem(false, "Contexto de uso invalido.");
        }

        PilhaItem pilha = obterPilhaPorId(idItem);
        if (pilha == null) {
            return new ResultadoUsoItem(false, "Item nao esta na mochila.");
        }

        Item item = pilha.getItem();
        if (!item.podeUsarEm(contexto.partida().getFase())) {
            return new ResultadoUsoItem(false, "Este item nao pode ser usado nesta fase.");
        }

        ResultadoUsoItem resultado = item.usar(contexto);
        if (resultado == null) {
            return new ResultadoUsoItem(false, "O item nao retornou um resultado valido.");
        }

        if (resultado.sucesso()) {
            remover(idItem, 1);
        }
        return resultado;
    }
}
