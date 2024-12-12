package io.github.fabiokusaba.libraryapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// Mapeando a estrutura da entidade
// Obrigatória para dizer que estou fazendo mapeamento JPA de uma entidade
@Entity
// Opcional, mas importante porque através dele a gente pode modificar a definição dessa tabela/entidade
// name representa o mesmo nome da tabela criada no banco de dados
// schema dentro do PostgreSQL, banco de dados que estamos trabalhando, tem o schema padrão que é o public e as tabelas
// que criamos foram criadas dentro desse schema padrão, mas se você tiver uma organização de schemas você pode colocar
// quando o schema for public não é obrigatório colocar
@Table(name = "autor", schema = "public")
// Essas anotações do Lombok Getter e Setter vão fazer com que em tempo de compilação essas annotations façam com que
// seja gerado os getters e setters dessa entidade
@Getter
@Setter
// Geração de toString pelo Lombok
@ToString(exclude = {"livros"})
// Essa anotação vai dizer que essa classe aqui vai ficar escutando toda vez que eu fizer alguma operação nessa entidade
// e ele vai observar se tem as annotations CreatedDate e LastModifiedDate, se tiver ele vai jogar as datas nos campos de
// acordo com a operação que eu estiver fazendo
@EntityListeners(AuditingEntityListener.class)
public class Autor {

    // Mapeando as colunas
    // Quando é a chave primária, que é o caso desse campo, nós precisamos anotar com Id, aqui eu coloco o Column para
    // dizer qual é o nome da tabela, essa annotation além de dizer que ela é uma coluna lá da nossa tabela serve para
    // fazermos algumas parametrizações
    // Como vamos gerar automaticamente esse id eu vou colocar uma annotation aqui que é o GeneratedValue que serve para
    // dizer que esse id vai ser gerado automaticamente, ou seja, não vou precisar me preocupar para gerar esse valor
    // porque o próprio JPA vai gerar e aqui eu preciso passar o parâmetro para dizer qual é a estratégia
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Na definição de Column podemos especificar que esse nome vai ter apenas 100 caracteres conforme foi definido no
    // banco de dados e para isso utilizamos length, passamos também o nullable como false para identificar que ele é
    // not null, ou seja, não pode ser nulo
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    // O Column não é obrigatório, então se você não colocar aqui no mapeamento vai funcionar do mesmo jeito porque
    // automaticamente quando você marca uma entidade com Entity todos os campos ele vai reconhecer como colunas e ele
    // vai entender que esse nome da propriedade é o mesmo nome da coluna no banco de dados
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "nacionalidade", length = 50, nullable = false)
    private String nacionalidade;

    // Não existe coluna do tipo livro dentro da tabela autor, mas no mapeamento objeto-relacional a gente pode colocar
    // aqui, então eu vou dizer que esse autor aqui eu posso puxar os livros dele, uma lista de livros e para fazermos o
    // mapeamento aqui vamos utilizar o OneToMany que é o contrário do que está em Livro então um autor One para muitos
    // livros Many
    // Isso aqui vai fazer com que o JPA saiba que isso aqui não é uma coluna, mas que você pode referenciar nas buscas
    // você pode buscar os livros, você pode carregar os livros através do próprio autor sem precisar ir lá na entidade
    // de livro
    // Só que aqui eu preciso colocar o seguinte: mappedBy que significa mapeado por e aqui colocamos qual é o nome
    // dessa propriedade de Autor dentro dessa tabela/entidade de livros, na entidade Livro o nome da propriedade Autor
    // é autor, então dentro da entidade Livro como está mapeado esse Autor, esse relacionamento OneToMany através da
    // propriedade autor
    // Por que temos esse mappedBy? Porque na entidade Livro poderíamos ter dois autores, ou seja, um autor e um coautor
    //  então esse relacionamento aqui seria com qual propriedade? Seria com autor e não com coautor, um autor teria
    // muitos livros, mas o coautor poderia ter uma regra diferente que ele não teria muitos livros por isso que aqui
    // você referencia
    // Esse mappedBy vai dizer que essa entidade não tem essa coluna aqui livros, é apenas um mapeamento OneToMany
    // A annotation Transient faz com que ele não considere isso aqui como uma coluna, ou seja, é uma propriedade
    // transiente que não tem nada a ver com o meu mapeamento JPA, basicamente estou ignorando do mapeamento JPA
    // Nós temos esse relacionamento OneToMany onde eu digo que um autor pode ter muitos livros
    // Podemos utilizar o modo cascade, ou seja, quando eu salvar o autor ele vai salvar os livros junto
    // Então, nesse caso aqui pode ser que o cascade faça sentido porque o cadastro do livro, no formulário do exemplo,
    // só faz sentido se eu tiver o autor dele, então se eu excluir o autor todos os livros desse autor serão removidos
    // também, então o cascade vai resolver esse problema
    // Aqui nós temos o autor que tem um relacionamento OneToMany, ou seja, um para muitos com livros em que um autor
    // pode ter muitos livros, então aqui nós temos a possibilidade de colocar um carregamento EAGER ou LAZY, por padrão
    // o relacionamento ToMany, ou seja, para muitos ele é LAZY
    // Se eu colocar o EAGER, não recomendável, estou dizendo que toda vez que você for carregar o autor traga sempre
    // com ele os seus livros
    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY
            //, cascade = CascadeType.ALL
    )
    //@Transient
    private List<Livro> livros;

    // Aqui vamos usar o LocalDateTime porque na nossa tabela estamos utilizando timestamp que guarda a data e a hora e o
    // LocalDate guarda apenas a data por isso a utilizamos no campo dataNascimento porque precisamos guardar apenas a data
    // e não a hora e aqui nos importa a data e a hora
    // Não quero me preocupar em ficar atualizando esses dados, então eu posso delegar para o JPA que toda vez que a ente
    // for cadastrar um Autor ele mesmo já gerar uma data aqui e toda vez que a gente fizer um update ele gerar uma data
    // atualizada para essa nossa propriedade, para fazermos isso vamos mapear esses campos com as anotações CreatedDate, ou
    // seja, toda vez que eu for persistir ele já vai colocar a data e hora atual nesse campo sem eu precisar me preocupar com 
    // isso, agora aqui na data de atualização nós temos uma outra annotation que é o LastModifiedDate, então toda vez que eu
    // fizer um update no Autor ele vai preencher com a data e hora atual
    // Para que essas duas annotations funcionem nós precisamos fazer duas coisas: primeira coisa que precisamos fazer é vir na
    // entidade e colocar a annotation EntityListeners e aqui dentro vou botar a referência de uma classe que é a AuditingEntity
    // Listener.class. E para esse listener funcionar nós precisamos fazer uma segunda coisa que é vir na classe Application ou
    // em alguma classe de configuration e colocar a annotation EnableJpaAuditing
    @CreatedDate
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Ao invés de utilizar o idUsuario eu posso colocar diretamente o Usuario aqui, então vamos fazer o mapeamento de
    // relacionamento
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
