package br.com.roletakanto.model;

import java.io.Serializable;

public record SegmentoRoleta(TipoResultadoRoleta tipo, String idConteudo, int peso)
    implements Serializable {}
