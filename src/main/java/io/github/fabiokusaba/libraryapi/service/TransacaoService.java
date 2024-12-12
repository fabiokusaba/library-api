package io.github.fabiokusaba.libraryapi.service;

import io.github.fabiokusaba.libraryapi.model.Autor;
import io.github.fabiokusaba.libraryapi.model.GeneroLivro;
import io.github.fabiokusaba.libraryapi.model.Livro;
import io.github.fabiokusaba.libraryapi.repository.AutorRepository;
import io.github.fabiokusaba.libraryapi.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class TransacaoService {

    @Autowired
    private AutorRepository autorRepository;
    @Autowired
    private LivroRepository livroRepository;

    // Aqui temos uma transação, então quando eu puxo essa entidade de dentro do banco de dados o estado que ela vai
    // estar é Managed, vou fazer uma alteração e vou dar o commit e ele vai mandar o update para o banco, então não
    // preciso chamar o save aqui, esse código vai ser o suficiente para eu atualizar a data de publicação de um livro
    // porque eu estou com uma transação aberta, existe uma janela de transação
    // Perceba que essa entidade dentro da transação ela estava Managed, então qualquer alteração que eu fizer aqui
    // dentro na hora que fizer o commit na transação ele vai mandar para o banco de dados
    // Nesse cenário eu tenho uma entidade Managed, então qualquer alteração que eu fizer nela e fizer um flush/commit
    // ele vai alterar lá no banco de dados, ele vai fazer com que aquelas alterações que foram realizadas dentro da
    // transação sejam persistidas no banco, aqui a gente percebe que o JPA ele não é manual, ele também utiliza ali
    // esse conceito de estado, de transição e sincronização com Entity Manager
    // Qual a utilidade disso daqui? Imagine que você tem um cenário que esse livro aqui precisa guardar, por exemplo
    // vou cadastrar o livro e dentro do cadastro do livro eu faço o upload da foto/imagem desse livro, quando eu salvo
    // o livro eu tenho o seu id, um detalhe importante mesmo não tendo dado o commit, não tendo enviado a operação para
    // o banco ele já possui id, pro JPA ele já gerou o id dessa entidade, então peguei o id do livro e agora quero
    // salvar a foto do livro dentro de um bucket na nuvem e para isso posso chamar um service com um metodo salvar e
    // passar as informações daquilo que quero salvar, depois eu quero atualizar o nome do arquivo que foi salvo
    // Nesse exemplo, na hora de salvar o livro eu não sei qual é o id do livro, então não posso dizer qual é o nome
    // para ele jogar na nossa coluna nome_arquivo, na sequência gerei esse id, já que depois que eu salvei vou saber
    // qual é esse id, na hora que eu chamar o livro.setNomeArquivoFoto(id + ".png") aqui sim estou alterando uma
    // entidade que ficou no estado Managed e quando eu der o commit dessa operação ele vai persistir essa atualização
    // aqui sem precisar eu chamar repository.save(livro) que é o metodo de atualizar, então não preciso chamar o save
    // para salvar e o save novamente para atualizar o nome do arquivo da foto porque como estou dentro de uma transação
    // qualquer alteração que eu fizer nessa entidade depois que ela está no estado Managed é persistido no final quando
    // ele dá o commit para o banco de dados
    @Transactional
    public void atualizacaoSemAtualizar() {
        // Peguei o livro do banco
        var livro = livroRepository
                .findById(UUID.fromString("7cf21a21-ba67-4b7c-965f-8cc0084e156a"))
                .orElse(null);

        // Alterei a data de publicação
        livro.setDataPublicacao(LocalDate.of(2024, 6, 1));

        // Chamei o save
        //livroRepository.save(livro);
    }

    // O metodo para ser executado uma transação ele precisa ser public, ele não pode ser private, então o Transactional
    // só vai funcionar em metodos public
    // O metodo saveAndFlush do JPA Repository já executa na linha em que foi chamado, ou seja, já manda para o banco a
    // operação, agora se você utilizar o save ele só vai executar no final da transação quando for dar o commit, então
    // é mais interessante você utilizar o save e utilizar o flush só se for realmente necessário
    @Transactional
    public void executar() {
        // Criando autor
        Autor autor = new Autor();
        autor.setNome("Teste Francisco");
        autor.setNacionalidade("Brasileira");
        autor.setDataNascimento(LocalDate.of(1951, 1, 31));

        // Salvando o autor
        autorRepository.save(autor);

        // Criando um livro
        Livro livro = new Livro();
        livro.setIsbn("90887-84874");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("Teste Livro do Francisco");
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));

        // Setando o autor ao livro
        livro.setAutor(autor);

        // Salvando o livro
        livroRepository.save(livro);

        // Testando dois cenários: o cenário que vai dar sucesso e ele vai dar o commit e o cenário que vai dar erro e
        // ele vai dar o rollback
        if (autor.getNome().equals("Teste Francisco")) {
            throw new RuntimeException("Rollback");
        }
    }
}
