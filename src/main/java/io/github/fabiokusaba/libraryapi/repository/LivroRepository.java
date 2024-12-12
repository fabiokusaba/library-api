package io.github.fabiokusaba.libraryapi.repository;

import io.github.fabiokusaba.libraryapi.model.Autor;
import io.github.fabiokusaba.libraryapi.model.GeneroLivro;
import io.github.fabiokusaba.libraryapi.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Como a gente sabe extendemos de JpaRepository e o JpaRepository ele extende algumas outras interfaces adicionando
// mais recursos ao repositório e tem um que não está aqui que é o JpaSpecificationExecutor e já que ele não está lá
// por padrão vamos adicionar aqui passando a nossa entidade Livro
// Essa interface JpaSpecificationExecutor adiciona os mesmos metodos de pesquisa que a gente viu ali, mas aqui ele
// recebe um objeto do tipo Specification que é uma interface do Spring Data JPA nela conseguimos botar algumas
// cláusulas (not, where, and, or...) e a gente consegue criar critérios orientados a objeto, então da mesma forma que
// o Example é orientado a objeto, ou seja, a gente não precisa escrever código SQL, até escrevemos alguns códigos SQL
// com a annotation Query, mas com Example e Specification a gente trabalha orientado a objeto
// As Specifications nada mais são do que abstrações da Criteria API do JPA, dentro do JPA nativo tem uma API pra você
// fazer pesquisas chamada de Criteria API
// Basicamente eu tenho o EntityManager, faz parte do core do JPA, e através dele eu crio um objeto CriteriaBuilder
// então ele vai criar um objeto do tipo Criteria, é um objeto que consigo construir critérios dentro de uma query,
// critérios são as cláusulas, por exemplo se eu quiser fazer uma consulta pelo nome do autor, isso é um critério, quero
// pesquisar pelo nome e pela data de nascimento, aqui botei dois critérios, então CriteriaBuilder é um builder pra você
// construir critério de uma pesquisa
// Através do CriteriaBuilder ele cria um CriteriaQuery para realizar queries na entidade, o objeto Root representa os
// dados da entidade então podemos usar metodos como getId para obter o valor da propriedade, baseado nos critérios
// construídos ele cria uma query através do createQuery e depois ele pega o resultado dessa pesquisa
public interface LivroRepository extends JpaRepository<Livro, UUID>, JpaSpecificationExecutor<Livro> {

    // Query Method (metodo de consulta)
    // É uma convenção do Spring Data JPA você utilizar o findBy quando você quiser trazer os dados de alguma entidade
    // quando você quiser criar um metodo específico de consulta, um metodo customizado que é o que estamos fazendo aqui
    // depois do By você coloca o campo que você quer buscar
    // Então, o que vai acontecer aqui? O próprio Spring Data JPA em tempo de compilação, quando ele compilar essa class
    // ele vai gerar uma consulta: "select * from livro where id_autor = id", e o JPA faz isso porque ele vai buscar o
    // mapeamento em Livro representado pelo JoinColumn e vai pegar o nome do campo, por isso que quando passamos o
    // Autor aqui ele sabe que passando esse nome da propriedade ele vai buscar, na verdade, o mapeamento dessa entidade
    // para comparar a propriedade "id_autor = id"
    // Então, não precisamos fazer nada, isso aqui é uma interface declarativa, ou seja, eu declaro o metodo e o Spring
    // Data JPA faz o resto pra mim, dessa forma ele vai conseguir trazer os livros do autor
    List<Livro> findByAutor(Autor autor);

    // O repository é a camada que acessa os dados, então logo ele quem vai ter essa parte de fazer as consultas no
    // banco para retornar o resultado da pesquisa pra gente
    // select * from livro where titulo = titulo
    List<Livro> findByTitulo(String titulo);

    // select * from livro where isbn = ?
    // Por exemplo, podemos criar esse metodo de consulta como retornando um Optional, ou seja, ele vai tentar encontrar
    // um livro com base no isbn fornecido e pode ser que não exista o livro com esse isbn e aí retornamos o Optional de
    // Livro
    Optional<Livro> findByIsbn(String isbn);

    // Aqui podemos trabalhar com expressões concatenando propriedades
    // select * from livro where titulo = ? and preco = ?
    List<Livro> findByTituloAndPreco(String titulo, BigDecimal preco);

    // Além do And temos o Or, neste caso estaríamos pesquisando pelo titulo ou pelo Isbn e ordenando o resultado pelo
    // titulo
    // select * from livro where titulo = ? or isbn = ?
    List<Livro> findByTituloOrIsbnOrderByTitulo(String titulo, String isbn);

    // select * from livro where data_publicacao between ? and ?
    // Aqui eu consigo listar todos os livros que foram publicados entre, por exemplo, janeiro/2000 e fevereiro/2002
    List<Livro> findByDataPublicacaoBetween(LocalDate inicio, LocalDate fim);

    // Já vimos aqui que os query methods a gente declara as propriedades seguindo um padrão, utilizando algumas
    // palavras chaves e o Spring Data JPA vai montar a query em tempo de execução, mas existe uma forma da gente criar
    // essas queries da forma que a gente quiser e também quando a gente precisar fazer queries mais complexas não fica
    // muito interessante a gente utilizar esse modelo query method, então utilizamos a annotation Query para fazer
    // queries maiores e mais complexas
    // Um detalhe importante é que pra gente criar essas queries a gente vai ter que estudar um pouco sobre JPQL que
    // é uma linguagem SQL específica para JPA, significa Java Persistence Query Language, é muito parecido com SQL só
    // que ele tem algumas variantes, algumas diferenças
    // Aqui podemos nomear o metodo da forma que a gente quiser porque ele não vai considerar a sintaxe do nome do
    // metodo como temos nos query methods
    // Aqui temos que colocar o nome da entidade, não é mais o nome da tabela, então dentro do JPQL referencia as
    // entidades e as propriedades
    // Um detalhe que você tem que sempre retornar o objeto do tipo que você está listando aqui
    // select l.* from livro as l order by l.titulo, l.preco
    @Query("select l from Livro as l order by l.titulo, l.preco")
    List<Livro> listarTodosOrdenadoPorTituloAndPreco();

    // select a.* from livro l join autor a on a.id = l.id_autor
    // Basicamente esse JPQL é equivalente ao SQL de cima e já fizemos o join entre as tabelas, perceba que não preciso
    // dizer qual é a chave porque a propriedade autor já tem a chave, já fizemos o mapeamento JPA dizendo que aqui é
    // uma JoinColumn e que é id_autor
    @Query("select a from Livro l join l.autor a")
    List<Autor> listarAutoresDosLivros();

    // select distinct l.* from livro l
    @Query("select distinct l.titulo from Livro l")
    List<String> listarNomesDiferentesLivros();

    @Query("""
            select l.genero
            from Livro l
            join l.autor a
            where a.nacionalidade = 'Brasileira'
            order by l.genero
    """)
    List<String> listarGenerosAutoresBrasileiros();

    // Named Parameters -> parâmetros nomeados, ou seja, ele vai jogar o valor pelo nome do parâmetro
    @Query("select l from Livro l where l.genero = :genero order by :paramOrdenacao")
    List<Livro> findByGenero(
            @Param("genero") GeneroLivro generoLivro,
            @Param("paramOrdenacao") String nomePropriedade
    );

    // Positional Parameters
    @Query("select l from Livro l where l.genero = ?1 order by ?2")
    List<Livro> findByGeneroPositionalParameters(GeneroLivro generoLivro, String nomePropriedade);

    // Toda vez que você for fazer operação de escrita aqui no JpaRepository você precisa colocar a anotação Modifying
    // porque aqui estou dizendo que eu vou modificar registros e outra annotation que precisamos é o Transactional,
    // então eu preciso dessas duas annotations quando eu vou fazer uma operação de escrita porque eu preciso abrir uma
    // transação, diferentemente de uma pesquisa que eu não preciso abrir transação pra fazer, mas quando eu vou fazer
    // uma operação de escrita (insert, update, delete) eu preciso utilizar uma transação
    // Basicamente aqui ele vai abrir uma transação para executar uma operação de escrita e depois ele vai dar um commit
    // e um rollback
    // Um detalhe importante nunca faça um update ou delete sem a cláusula where
    @Modifying
    @Transactional
    @Query("delete from Livro where genero = ?1")
    void deleteByGenero(GeneroLivro genero);

    @Modifying
    @Transactional
    @Query("update Livro set dataPublicacao = ?1")
    void updateDataPublicacao(LocalDate novaData);

    // Vai retornar verdadeiro se existir qualquer livro cadastrado com esse autor
    boolean existsByAutor(Autor autor);

    // Pesquisa paginada -> imagine que a gente tenha uma base de dados com mais de um milhão de registros e você quer
    // fazer uma pesquisa mas você não sabe qual registro é então ao usuário clicar em consultar ele vai trazer muitos
    // registros, vai fazer uma consulta bem pesada e isso não é produtivo, tanto vai ser ruim para o usuário que ele
    // vai estar lá esperando o resultado da consulta como também não vai ser bom para a performance da aplicação
    // Os objetos Page e Pageable são os objetos que o Spring Data disponibiliza pra gente fazer a paginação, então o
    // Pageable é uma interface que vai definir quais são os parâmetros da paginação, por exemplo ele vai dizer qual é
    // a página que você quer, quantos registros você quer por página, ordenação, e a página é representada por esse
    // objeto Page e ele tem aqui o tanto de páginas, o tanto de elementos e aqui ele vai retornar pra gente uma
    // collection que vai representar os registros daquela página, então vamos supor que eu pedi a página um com dez
    // registros
}
