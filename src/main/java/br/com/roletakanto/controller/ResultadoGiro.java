package br.com.roletakanto.controller;

import br.com.roletakanto.model.ResultadoRoleta;

/** Resposta estruturada de um comando de giro para interfaces e outros clientes. */
public record ResultadoGiro(boolean sucesso, ResultadoRoleta resultado, String mensagem) {

  public static ResultadoGiro falha(String mensagem) {
    return new ResultadoGiro(false, null, mensagem);
  }

  public static ResultadoGiro sucesso(ResultadoRoleta resultado, String mensagem) {
    return new ResultadoGiro(true, resultado, mensagem);
  }
}
