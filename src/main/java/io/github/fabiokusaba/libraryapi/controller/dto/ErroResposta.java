package io.github.fabiokusaba.libraryapi.controller.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

// DTO que vai representar a nossa resposta para os erros da aplicação
public record ErroResposta(int status, String mensagem, List<ErroCampo> erros) {

    // No contrato da nossa API nós temos alguns erros diferentes, por exemplo Unprocessable Entity ele vai retornar uma
    // lista com os campos que deram erro de validação, já no Conflict registro duplicado não tem a lista dos campos só
    // sendo necessário saber o código de status e a mensagem de erro, então para atender a esses critérios vamos criar
    // um metodo estático aqui dentro do nosso record para construir erros de resposta de forma rápida
    // Então, por exemplo quando eu precisar criar uma resposta padrão já temos um metodo estático em que passamos a
    // mensagem e esse metodo vai se encarregar de criar a resposta
    public static ErroResposta respostaPadrao(String mensagem) {
        return new ErroResposta(HttpStatus.BAD_REQUEST.value(), mensagem, List.of());
    }

    public static ErroResposta conflito(String mensagem) {
        return new ErroResposta(HttpStatus.CONFLICT.value(), mensagem, List.of());
    }
}
