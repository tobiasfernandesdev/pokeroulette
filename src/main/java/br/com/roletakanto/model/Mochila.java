package br.com.roletakanto.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Mochila implements Serializable {
    private static final int CAPACIDADE_TIPOS = 5; // Máximo de 5 tipos diferentes de itens
    private final List<PilhaItem> pilhas = new ArrayList<>();

    public List<PilhaItem> listar() {
        return Collections.unmodifiableList(pilhas);
    }

    public int contarTipos() {
        return pilhas.size();
    }

    public PilhaItem obterPilhaPorId(String idItem) {
        return pilhas.stream()
                .filter(p -> p.getItem().getId().equals(idItem))
                .findFirst()
                .orElse(null);
    }

    // Verifica se pode adicionar uma quantidade do item
    public boolean podeAdicionar(Item item, int quantidade) {
        PilhaItem pilha = obterPilhaPorId(item.getId());
        if (pilha != null) {
            // Se já existe uma pilha, verifica se cabe na pilha existente (limite de 5 por item)
            int livreNaPilha = item.getLimitePilha() - pilha.getQuantidade();
            return quantidade <= livreNaPilha;
        } else {
            // Se não existe, precisa ter espaço para um novo tipo de item (máximo 5 tipos)
            return pilhas.size() < CAPACIDADE_TIPOS && quantidade <= item.getLimitePilha();
        }
    }

    // Adiciona o item respeitando os limites de tipos e quantidade por pilha
    public boolean adicionar(Item item, int quantidade) {
        if (!podeAdicionar(item, quantidade)) {
            return false;
        }

        PilhaItem pilha = obterPilhaPorId(item.getId());
        if (pilha != null) {
            pilha.adicionar(quantidade);
        } else {
            pilhas.add(new PilhaItem(item, quantidade));
        }
        return true;
    }

    public boolean remover(String idItem, int quantidade) {
        PilhaItem pilha = obterPilhaPorId(idItem);
        if (pilha != null && pilha.remover(quantidade)) {
            if (pilha.getQuantidade() == 0) {
                pilhas.remove(pilha); // Remove o tipo de item da mochila se a quantidade zerar
            }
            return true;
        }
        return false;
    }

    public boolean temQuantidade(String idItem, int quantidade) {
        PilhaItem pilha = obterPilhaPorId(idItem);
        return pilha != null && pilha.getQuantidade() >= quantidade;
    }
}