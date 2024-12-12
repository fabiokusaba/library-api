package io.github.fabiokusaba.libraryapi.exceptions;

// Vamos criar agora a validação de registro duplicado para o autor, então a gente vai verificar se o autor está
// duplicado com aquelas informações dos três campos que a gente recebe (nome, data nascimento, nacionalidade), se tiver
// a gente vai retornar Conflict para quem está consumindo a nossa API e não vai deixar cadastrar
// Primeira coisa que a gente vai fazer é criar um pacote para as nossas exceções de domínio, a exceção de domínio é uma
// exception que tem a ver com o negócio, com o domínio do seu negócio, então se eu tenho uma regra que diz que o
// registro não pode ser duplicado então eu tenho uma regra de negócio de registro duplicado por isso preciso criar
// dentro da camada de domínio
// Então, ela vai ser uma RuntimeException e para isso vou utilizar o extends
public class RegistroDuplicadoException extends RuntimeException {

    // Aqui estamos sobrescrevendo o construtor da super classe para podermos mandar uma mensagem customizada
    public RegistroDuplicadoException(String message) {
        super(message);
    }
}
