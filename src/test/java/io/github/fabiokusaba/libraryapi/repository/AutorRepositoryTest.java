package io.github.fabiokusaba.libraryapi.repository;

import io.github.fabiokusaba.libraryapi.model.Autor;
import io.github.fabiokusaba.libraryapi.model.GeneroLivro;
import io.github.fabiokusaba.libraryapi.model.Livro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest // Essa anotação é utilizada para subir o contexto do Spring Boot para realização dos testes
public class AutorRepositoryTest {

    @Autowired // Com o contexto levantado a gente pode injetar dependências que estão lá dentro do container
    AutorRepository autorRepository;

    @Autowired
    LivroRepository livroRepository;

    @Test // Com essa anotação eu consigo executar o código que está aqui dentro
    public void salvarTest() {
        Autor autor = new Autor();
        autor.setNome("Maria");
        autor.setNacionalidade("Brasileira");
        autor.setDataNascimento(LocalDate.of(1951, 1, 31));

        var autorSalvo = autorRepository.save(autor);
        System.out.println("Autor Salvo: " + autorSalvo);
    }

    @Test
    public void atualizarTest() {
        var id = UUID.fromString("a0ffdf96-eed8-416e-a739-b66ab5ba7ff0");

        Optional<Autor> possivelAutor = autorRepository.findById(id);

        if (possivelAutor.isPresent()) {
            Autor autorEncontrado = possivelAutor.get();
            System.out.println("Dados do Autor:");
            System.out.println(autorEncontrado);

            autorEncontrado.setDataNascimento(LocalDate.of(1960, 1, 30));

            // O metodo save (salvar) tanto salva quanto atualiza e o JpaRepository vai saber que é para atualizar esse
            // registro quando ele já estiver salvo no banco e ele sabe disso quando esse registro já possui um id, caso
            // contrário ele vai salvar como se fosse um novo registro
            autorRepository.save(autorEncontrado);
        }
    }

    @Test
    public void listarTest() {
        List<Autor> lista = autorRepository.findAll();

        // Aqui estamos utilizando um metodo de referência (method reference) em que basicamente ele vai pegar cada
        // elemento dessa lista e vai chamar o System.out.println()
        lista.forEach(System.out::println);
    }

    @Test
    public void countTest() {
        System.out.println("Contagem de autores: " + autorRepository.count());
    }

    @Test
    public void deletePorIdTest() {
        var id = UUID.fromString("a0ffdf96-eed8-416e-a739-b66ab5ba7ff0");
        autorRepository.deleteById(id);
    }

    @Test
    public void deleteTest() {
        var id = UUID.fromString("56deae85-f5a9-4a6d-b170-7cbaea41efa4");
        var maria = autorRepository.findById(id).get();
        autorRepository.delete(maria);
    }

    @Test
    void salvarAutorComLivrosTest() {
        Autor autor = new Autor();
        autor.setNome("Antonio");
        autor.setNacionalidade("Americano");
        autor.setDataNascimento(LocalDate.of(1970, 8, 5));

        Livro livro = new Livro();
        livro.setIsbn("20887-84874");
        livro.setPreco(BigDecimal.valueOf(204));
        livro.setGenero(GeneroLivro.MISTERIO);
        livro.setTitulo("O roubo da casa assombrada");
        livro.setDataPublicacao(LocalDate.of(1999, 1, 2));
        livro.setAutor(autor);

        Livro livro2 = new Livro();
        livro2.setIsbn("99999-84874");
        livro2.setPreco(BigDecimal.valueOf(650));
        livro2.setGenero(GeneroLivro.MISTERIO);
        livro2.setTitulo("O roubo da casa assombrada vol.2");
        livro2.setDataPublicacao(LocalDate.of(2000, 1, 2));
        livro2.setAutor(autor);

        // Inicializando a lista de livros do autor
        autor.setLivros(new ArrayList<>());
        // Peguei a lista de livros do autor e utilizando o metodo add vou estar adicionando esse livro na lista
        autor.getLivros().add(livro);
        autor.getLivros().add(livro2);

        // A abordagem mais fácil e manual seria primeiro salvar o autor, em seguida chamar o LivroRepository para
        // salvar a lista de livros
        autorRepository.save(autor);

        // Utilizando o cascade não precisaríamos chamar o saveAll, no trecho de código acima com o metodo save ele já
        // vai salvar tudo
//        livroRepository.saveAll(autor.getLivros());
    }

    @Test
    //@Transactional
    void listarLivrosAutor() {
        var id = UUID.fromString("ae97281d-b9f5-4ba0-bc73-958e2d022578");
        var autor = autorRepository.findById(id).get();

        // Para carregar os livros sendo LAZY, bem aqui eu preciso fazer alguma coisa pra quando for imprimir os livros
        // ele imprima e não dê uma Exception porque não quero usar a anotação Transactional e eu quero utilizar o
        // metodo manual, quero carregar os livros antes pra poder setar aqui, quero ter o controle para quando, por
        // exemplo, for mostrar em um formulário eu tenho que primeiro trazer os dados do autor, em seguida pego os
        // livros daquele autor e apresento na tela que seria a forma correta
        // Então, como que a gente faz pra resolver isso nós temos aqui o livroRepository e a ideia é buscar os livros
        // do autor só que o repository tem apenas metodos genéricos que não são específicos para o meu negócio, só que
        // a gente precisa criar um específico para essa entidade, eu quero criar um metodo que execute esse SQL aqui:
        // "select * from livro where id_autor = 'ae97281d-b9f5-4ba0-bc73-958e2d022578'" e para fazer isso vamos no
        // LivroRepository

        // Agora eu carreguei apropriadamente a lista de livros, então essa é a melhor forma de você carregar dados LAZY
        // de uma entidade principalmente quando for lista, então sempre trabalhem com fetch do tipo LAZY, não utilize
        // EAGER
        List<Livro> livrosLista = livroRepository.findByAutor(autor);
        autor.setLivros(livrosLista);

        autor.getLivros().forEach(System.out::println);
    }
}
