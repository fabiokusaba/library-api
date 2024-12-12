package io.github.fabiokusaba.libraryapi.controller.dto;

// DTO que vai representar os erros nos campos, então ele vai indicar qual o campo que deu erro e uma mensagem dizendo
// qual foi o erro que aconteceu
public record ErroCampo(String campo, String erro) {
}
