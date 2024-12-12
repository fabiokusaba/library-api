package io.github.fabiokusaba.libraryapi.repository;

import io.github.fabiokusaba.libraryapi.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransacoesTest {

    @Autowired
    TransacaoService transacaoService;

    // Toda vez que eu quiser fazer operações de escrita no banco de dados, ou seja, eu vou alterar informações no banco
    // de dados eu vou precisar de uma transação e para abrir uma transação no Spring Data eu coloco em cima do metodo
    // que vai realizar as operações uma annotation Transactional, isso indica que esse metodo aqui a gente vai ter uma
    // transação, então ele abre uma transação aqui no início da execução e no final da execução desse metodo ele vai
    // fazer um commit ou rollback
    // Commit -> confirmar as alterações
    // Rollback -> desfazer as alterações
    // Num cenário onde eu executo as operações e dá um erro, perceba que deu um Rollback, e não conseguimos ver nenhum
    // SQL aqui, mesmo tendo executado as operações antes de dar o erro a gente não consegue ver no console os inserts
    // porque as operações só são enviadas para o banco de dados no final da transação, na hora do commit, então se
    // chegar ao final do metodo e não tiver dado nenhum erro aí sim o JPA manda as instruções pro banco de dados
    // Então, é só quando acontece o flush/commit que a operação vai para o banco de dados, essas entidades a partir do
    // momento em que chamei o save no livro e chamei o save no autor ele vai transformar essas entidades que estão em
    // estado Transient (novo) para o estado Managed, quando ele vai fazer o commit ele vai mandar para o banco de dados
    //
    @Test
    void transacaoSimples() {
        transacaoService.executar();
    }

    @Test
    void transacaoEstadoManaged() {
        transacaoService.atualizacaoSemAtualizar();
    }
}
