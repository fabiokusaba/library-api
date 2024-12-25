package io.github.fabiokusaba.libraryapi.controller.common;

import io.github.fabiokusaba.libraryapi.controller.dto.ErroCampo;
import io.github.fabiokusaba.libraryapi.controller.dto.ErroResposta;
import io.github.fabiokusaba.libraryapi.exceptions.CampoInvalidoException;
import io.github.fabiokusaba.libraryapi.exceptions.OperacaoNaoPermitidaException;
import io.github.fabiokusaba.libraryapi.exceptions.RegistroDuplicadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

// Vamos criar um componente Spring que ele vai lidar com as exceptions que a gente programar para ele lidar, uma das
// exceptions é a de validação então é uma exceção que podemos capturar de forma global e tratar ela
// Vamos adicionar uma annotation RestControllerAdvice porque ele vai capturar exceptions, o objetivo dele é esse, e ele
// vai dar uma resposta Rest
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Então, aqui a gente coloca como se a gente tivesse recebendo uma exception, como se fosse um try-catch, só que nesse
    // catch a gente vai retornar o que queremos que vá na resposta
    // Uma das sugestões é nomearmos o metodo com handle mais o nome da exceção que você está querendo tratar, aqui vamos
    // receber ela como parâmetro e eu preciso adicionar uma annotation que é ExceptionHandler essa annotation é o que faz
    // acontecer/capturar o erro e jogar aqui no parâmetro o erro capturado então a gente precisa dizer aqui nessa annotation
    // qual é a exception que ele vai capturar
    // Toda vez que o nosso código lançar esse erro ele vai cair aqui nesse metodo
    // Para mapearmos um retorno específico quando estou retornando um objeto e não estou retornando um ResponseEntity aqui
    // a gente tem a annotation ResponseStatus essa annotation serve para você mapear o retorno desse metodo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroResposta handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        log.error("Erro de validação: {}", e.getMessage());

        // Para descobrirmos quais campos que deram erro através dessa annotation fazemos da seguinte forma: ela tem um
        // metodo getFieldErrors que vai retornar os campos que deram erro, então eu tenho uma lista de FieldError e esse
        // objeto tem a capacidade de me dizer quais campos deram erro
        List<FieldError> fieldErrors = e.getFieldErrors();

        // Então, vou mapear essa lista de FieldError para uma lista de ErroCampo
        List<ErroCampo> listaErros = fieldErrors
                .stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        // Agora que já tenho a lista dos campos com erros posso vir aqui e retornar o ErroResposta
        return new ErroResposta(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de validação.", listaErros);
    }

    // Podemos melhorar a tratativa de erros dos nossos controllers eliminando o try-catch e deixando para a nossa class
    // GlobalExceptionHandler como sendo a responsável por tratar de todos os erros da nossa aplicação incluindo
    // RegistroDuplicadoException, OperacaoNaoPermitidaException e outros que possam vir a surgir dando a eles uma
    // resposta apropriada
    @ExceptionHandler(RegistroDuplicadoException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ErroResposta handleRegistroDuplicadoException(RegistroDuplicadoException e) {
        // Toda vez que der um RegistroDuplicadoException a gente já sabe que é conflito
        return ErroResposta.conflito(e.getMessage());
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErroResposta handleOperacaoNaoPermitidaException(OperacaoNaoPermitidaException e) {
        return ErroResposta.respostaPadrao(e.getMessage());
    }

    @ExceptionHandler(CampoInvalidoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroResposta handleCampoInvalidoException(CampoInvalidoException e) {
        return new ErroResposta(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de validação.",
                List.of(new ErroCampo(e.getCampo(), e.getMessage())));
    }

    // Aconteceu agora uma nova Exception, então toda vez que ocorrer uma Exception não tratada ele vai entrar no nosso
    // handleErrosNaoTratados, aquele erro de permissão de role foi um erro não tratado, a Exception que ocorreu ali foi
    // a Access Denied, precisamos dar uma tratativa específica
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ErroResposta handleAccessDeniedException(AccessDeniedException e) {
        return new ErroResposta(
            HttpStatus.FORBIDDEN.value(), "Acesso Negado.", List.of()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResposta handleErrosNaoTratados(RuntimeException e) {

        log.error("Erro inesperado", e);

        return new ErroResposta(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado. Entre em contato com a administração.",
                List.of());
    }
}
