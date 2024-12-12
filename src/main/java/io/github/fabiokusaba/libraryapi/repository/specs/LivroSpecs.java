package io.github.fabiokusaba.libraryapi.repository.specs;

import io.github.fabiokusaba.libraryapi.model.GeneroLivro;
import io.github.fabiokusaba.libraryapi.model.Livro;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

// A gente pode criar uma classe só para guardar as Specifications pra gente ficar reutilizando elas e eliminar
// esse código da camada service
// Todas as Specifications de Livro vou colocar aqui, e aqui coloco eles como sendo estáticos
public class LivroSpecs {

    // Uma Specification nada mais é do que uma interface funcional
    // No equal passamos dois parâmetros: o primeiro parâmetro é qual é o campo que eu quero comparar e o segundo
    // parâmetro é o valor que eu quero comparar, quem me diz quem é o campo que eu quero comparar é o root, o root
    // é a projeção do meu query
    public static Specification<Livro> isbnEqual(String isbn) {
        return (root, query, cb) -> cb.equal(root.get("isbn"), isbn);
    }

    // Aqui já vou utilizar o like porque eu quero que ele digite apenas um pedaço do título do livro e ele busque lá no
    // banco
    // Para ele ignorar o case, ou seja, ignorar se está maiúscula, minúscula, da forma que foi digitada, a gente pode
    // utilizar aqui o upper desta forma estaremos desconsiderando totalmente o case
    // Só que aqui eu preciso dizer qual vai ser o matching mode, ou seja, se eu quero comparar esse titulo no começo da
    // String, no final da String ou em qualquer lugar da String, como quero em qualquer lugar vou precisar colocar o %
    // antes e depois
    public static Specification<Livro> tituloLike(String titulo) {
        return (root, query, cb)
                -> cb.like(cb.upper(root.get("titulo")), "%" + titulo.toUpperCase() + "%");
    }

    // Detalhe que aqui no root.get() você tem que colocar o nome da propriedade e não o nome do campo no banco de dados
    // como falamos aqui é orientado a objetos então você referência os objetos da entidade, das propriedades e não do
    // nome do campo lá no banco
    public static Specification<Livro> generoEqual(GeneroLivro genero) {
        return (root, query, cb) -> cb.equal(root.get("genero"), genero);
    }

    // Aqui vai ser um pouquinho diferente porque o Livro não tem ano de publicação ele tem data de publicação, então o
    // ano está dentro da data e não diretamente no Livro, cada banco de dados tem uma forma diferente de você conseguir
    // extrair o ano, mês, dia de uma data, então aqui no Postgres podemos fazer de algumas formas: no Postgres temos a
    // função extract onde podemos colocar qual o campo que queremos extrair algo, só que pra eu usar essa função dentro
    // do API Criteria, ou seja, dentro da Specification é um pouco mais complexo mas eu consigo utilizar funções
    // simples lá dentro da Specification e uma das funções que podem resolver o nosso problema é a to_char que vai
    // transformar em char o que passarmos para ele no formato que for passado, então essa função vai receber uma data
    // ou timestamp e um parâmetro que é o formato que você quer no nosso caso como queremos só o ano podemos passar
    // 'YYYY'
    // Estou colocando toString porque essa função que eu vou chamar ela retorna uma String, então só posso comparar
    // String com String, não posso comparar String com Integer (número), por isso estamos transformando ela em String
    // Caso eu queira chamar uma função dentro da Criteria API eu chamo cb.function() passando o nome da função, o que
    // ela me retorna, só que aí ela recebe dois parâmetros: o primeiro é o campo que eu quero comparar então vou chamar
    // o root.get(), nunca coloque o nome do campo que está no banco sempre coloque o nome da propriedade na entidade,
    // para passarmos o outro campo 'YYYY' usamos o cb.literal(), ou seja, posso passar qualquer coisa que eu quiser
    // aqui
    public static Specification<Livro> anoPublicacaoEqual(Integer anoPublicacao) {
        // and to_char(data_publicacao, 'YYYY') = :anoPublicacao
        return (root, query, cb) ->
                cb.equal(cb.function(
                        "to_char",
                        String.class,
                        root.get("dataPublicacao"), cb.literal("YYYY")),
                        anoPublicacao.toString()
                );
    }

    public static Specification<Livro> nomeAutorLike(String nome) {
        return (root, query, cb) -> {
            // Com join -> o root representa a projeção (Livro), aqui temos o metodo join que possui dois parâmetros:
            // quero fazer join com qual atributo e qual o tipo de join (inner join, left join, right join), então aqui
            // a gente fez um join com Autor, agora esse joinAutor aqui é como se eu tivesse extraído um novo root então
            // aqui eu consigo navegar dentro do autor
            Join<Object, Object> joinAutor = root.join("autor", JoinType.LEFT);
            return cb.like(cb.upper(joinAutor.get("nome")), "%" + nome.toUpperCase() + "%");

            // Forma simples
            //return cb.like(cb.upper(root.get("autor").get("nome")), "%" + nome.toUpperCase() + "%");
        };
    }
}
