package io.github.fabiokusaba.libraryapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "livro")
// Essa anotação Data ela vai utilizar algumas outras annotations aqui, por exemplo ela incorpora a Getter e Setter, e
// outras annotations do Lombok também ToString, EqualsAndHashCode, RequiredArgsConstructor vai gerar um construtor com
// todas as propriedades final
@Data
// Vai gerar um construtor sem argumentos
//@NoArgsConstructor
// Ele gera um construtor com todas as propriedades
@AllArgsConstructor
// Gera um construtor vazio
@NoArgsConstructor
// Gerando o ToString através do Lombok e excluindo o autor fazendo com que ele não faça parte do ToString
@ToString(exclude = "autor")
@EntityListeners(AuditingEntityListener.class)
public class Livro {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "isbn", length = 20, nullable = false)
    private String isbn;

    @Column(name = "titulo", length = 150, nullable = false)
    private String titulo;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    // Aqui eu preciso colocar uma anotação Enumerated e dizer qual o tipo de enumeração se é ordinal ou String, essa
    // aqui é String porque eu guardo esse valor em String no banco de dados, de forma que esse ORDINAL a título de
    // curiosidade seria se eu quisesse guardar a posição da enumeração (índice de cada enum), se você utiliza ORDINAL
    // e muda a posição dos enums o banco vai ficar bagunçado, ou seja, o que era FANTASIA virou FICCAO e vice-versa,
    // então para evitar esse tipo de problema se você for utilizar enum é uma boa prática utilizar ele como String
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 30, nullable = false)
    private GeneroLivro genero;

    // O tipo BigDecimal é a melhor forma de você trabalhar com valores monetários porque ele possui uma precisão maior
    @Column(name = "preco", precision = 18, scale = 2)
    private BigDecimal preco;

    // Aqui agora vamos fazer um relacionamento temos uma chave estrangeira da tabela autor aqui para a minha classe
    // livro, só que aqui estamos no mapeamento objeto-relacional então aqui vou referenciar o objeto e não vou
    // referenciar uma coluna
    // E aqui existe uma annotation específica para definir qual é o nome da coluna, quando é relacionamento ao invés de
    // Column a gente utiliza JoinColumn, além disso eu tenho que dizer que tipo de relacionamento é esse aqui
    // Geralmente quando a gente tem uma chave estrangeira, como é o nosso caso aqui, então é um caso de ManyToOne que
    // seria muitos livros para um autor nesse caso Many se refere a entidade atual e One se refere a entidade mapeada
    // aqui, o relacionamento
    // No mapeamento da nossa relação, nesse ManyToOne, temos a propriedade cascade com as opções de qual o tipo de
    // operação em cascata que eu quero executar nesse relacionamento de Livro com Autor, quando utilizamos o ALL quer
    // dizer que qualquer operação que fizermos no Livro ele vai trazer o Autor junto, ou seja, ele vai executar na
    // tabela de autor junto
    // Toda vez que você tiver um relacionamento ManyToOne, ou seja, só tenho um Autor dentro do Livro, ele vai por
    // padrão carregar esse autor junto
    // Como é que eu faço para não trazer o autor? Porque pode ser que eu não precise toda vez que eu precisar trazer o
    // livro eu preciso realmente dessa informação do autor, eu não posso simplesmente quando eu precisar saber quem é o
    // autor do livro aí eu faço outro select pra saber porque essa consulta pode ficar pesada, a entidade Autor pode
    // ter relacionamentos com outras entidades e isso vai ficando cada vez maior, torna-se um select em cascata muito
    // grande com muitos dados e às vezes eu quero simplesmente só os dados do livro
    // E aqui tem uma configuração que a gente coloca dentro do ManyToOne que é o fetch e ele vai dizer como é que eu
    // vou trazer esse autor aqui nesse relacionamento de muitos para um, qual estratégia que vou utilizar
    // O padrão é EAGER que é esse comportamento que vimos, ou seja, toda vez que temos um relacionamento ManyToOne por
    // padrão vai ser EAGER e ele vai trazer junto, se eu quiser que não traga aí eu chamo LAZY dessa forma aqui ele só
    // vai trazer os dados do livro e não vai trazer do autor
    // Ao trabalhar com LAZY para carregarmos o autor do livro basicamente temos que utilizar alguma estratégia sendo a
    // mais fácil você criar uma consulta no repositório de autor fazendo o join com livro e buscar o autor que está
    // associado a aquele livro, outra forma que temos aqui é colocar uma anotação em cima do metodo Transactional aqui
    // eu vou abrir uma transação e essa transação é como se fosse uma janela que eu abri com o banco, abri uma janela
    // para executar operações no banco e ele só vai fechar essa transação quando eu terminar a execução do metodo de
    // forma que quando eu acessar o nome do autor ele vai buscar
    // Como é LAZY, significa carregamento lento, ou seja, ele só vai carregar quando eu precisar e se eu estiver dentro
    // de uma transação
    @ManyToOne(
            //cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "id_autor")
    private Autor autor;

    @CreatedDate
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
