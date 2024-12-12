package io.github.fabiokusaba.libraryapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.fabiokusaba.libraryapi.exceptions.OperacaoNaoPermitidaException;
import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.repository.LivroRepository;
import io.github.fabiokusaba.libraryapi.security.SecurityService;
import io.github.fabiokusaba.libraryapi.validator.AutorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import io.github.fabiokusaba.libraryapi.model.Autor;
import io.github.fabiokusaba.libraryapi.repository.AutorRepository;

// Camade de negócio do domínio de Autores, vamos anotar ele com Service e fazer a injeção do nosso AutorRepository
// Aqui na camada de serviço ele trata da camada de domínio da aplicação, o domínio da aplicação tem a entidade Autor
// o AutorDTO faz parte de outra camada que é a camada de representação (camada da API)
@Service
// O Lombok tem uma annotation que é o RequiredArgsConstructor, essa annotation cria um construtor com os campos
// obrigatórios, campos obrigatórios são os que tem a palavra final então toda vez que eu quiser adicionar uma nova
// dependência utilizo private final e a dependência que eu quero adicionar
@RequiredArgsConstructor
public class AutorService {

    // Já sabemos que toda vez que precisamos adicionar uma nova propriedade aqui que deve ser injetada nesse Component
    // eu preciso adicionar no construtor tanto que já estudamos em arquitetura que a forma adequada que indica que a
    // dependência é obrigatória é quando injeto ela via construtor
    private final AutorRepository repository;
    private final AutorValidator validator;
    private final LivroRepository livroRepository;
    // Injetando o SecurityService
    private final SecurityService securityService;

    // Como explicado nas aulas de arquitetura se você colocar um Bean gerenciado aqui no construtor o Spring vai injetar
    // automaticamente pra você porque essa nossa classe também é um Bean gerenciado
//    public AutorService(AutorRepository repository, AutorValidator validator, LivroRepository livroRepository) {
//        this.repository = repository;
//        this.validator = validator;
//        this.livroRepository = livroRepository;
//    }

    // O service é onde tem a lógica de negócio, então não colocamos validações no controller a gente tem que ter essa
    // lógica de validação aqui no service e como já explicado anteriormente a gente pode criar um objeto que vai ser
    // um validador, posso criar uma classe só para ter a lógica de validação utilizando o princípio da responsabilidade
    // única, então assim eu diminuo a quantidade de código aqui dentro do AutorService
    public Autor salvar(Autor autor) {
        // Validando o autor
        validator.validar(autor);
        // Depois que eu validar o autor, eu vou obter o usuário logado e setar o id no autor, então onde eu quiser
        // pegar o usuário que está fazendo a requisição eu chamo o SecurityService, injeto, e eu chamo esse metodo
        // obterUsuarioLogado() e ele vai me retornar quem é que está logado
        Usuario usuario = securityService.obterUsuarioLogado();
        autor.setUsuario(usuario);
        return repository.save(autor);
    }

    public void atualizar(Autor autor) {
        // Vamos fazer uma verificação, quando vou atualizar o autor ele já tem que estar cadastrado no banco então se
        // ele está cadastrado ele tem um id, caso o id seja igual a nulo vamos lançar uma Exception
        if (autor.getId() == null) {
            throw new IllegalArgumentException("Para atualizar, é necessário que o autor já esteja salvo na base.");
        }

        // Validando o nosso autor
        validator.validar(autor);

        // Caso contrário, vamos atualizar os dados desse autor
        repository.save(autor);
    }

    // Aqui vamos retornar como Optinal porque o Autor pode existir ou não
    public Optional<Autor> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public void deletar(Autor autor) {
        // Se o autor possui livros lanço essa Exception
        if (possuiLivro(autor)) {
            throw new OperacaoNaoPermitidaException("Não é permitido excluir um Autor que possui livros cadastrados!");
        }

        // Caso contrário, faço a sua deleção
        repository.delete(autor);
    }

    public List<Autor> pesquisa(String nome, String nacionalidade) {
        // Podemos melhorar essa nossa pesquisa de autores, aqui fizemos uma lógica que nada mais é do que uma pesquisa
        // dinâmica porque dependendo dos parâmetros ela vai se comportar de uma forma diferente e toda vez que a gente
        // for trabalhar com pesquisa dinâmica nós poderemos utilizar o recurso examples
        // Se o nome não for nulo e a nacionalidade não for nulo
        if (nome != null && nacionalidade != null) {
            return repository.findByNomeAndNacionalidade(nome, nacionalidade);
        }

        // Se o nome não for nulo
        if (nome != null) {
            return repository.findByNome(nome);
        }

        // Se a nacionalidade não for nulo
        if (nacionalidade != null) {
            return repository.findByNacionalidade(nacionalidade);
        }

        // Se ele não passou nem o nome e nem a nacionalidade
        return repository.findAll();
    }

    public List<Autor> pesquisaByExample(String nome, String nacionalidade) {
        // Primeiramente vou criar um objeto do tipo Autor aqui porque o Example precisa de um objeto, ele não trabalha
        // só com os parâmetros, ele trabalha com um objeto de exemplo
        var autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);

        // Vamos utilizar a classe Example para criar um Example do autor, e isso aqui vai me retornar um objeto do tipo
        // Example, mas aqui vou precisar passar para ele um objeto do tipo ExampleMatcher para passar algumas configs
        // que eu quero para a minha pesquisa, por exemplo ignorar letras maiúsculas ou minúsculas, passar parte de uma
        // palavra, etc
        ExampleMatcher matcher = ExampleMatcher
                .matching()
                // Ignora os campos que eu colocar aqui, então nessa pesquisa só quero levar em consideração o nome e a
                // nacionalidade, ele vai desconsiderar totalmente mesmo que esteja dentro do objeto de exemplo
                .withIgnorePaths("id", "dataNascimento", "dataCadastro")
                // Ignora os valores nulos, ou seja, se eu não preencher nenhuma outra informação no caso do autor se
                // eu preencher somente essas duas (nome e nacionalidade) ele vai considerar apenas elas ignorando
                // qualquer outro valor nulo
                .withIgnoreNullValues()
                // Ignorar letras maiúsculas e minúsculas na hora da busca
                .withIgnoreCase()
                // Vamos adicionar um matcher de String, ou seja, qual é a forma que ele vai utilizar para bater as
                // Strings, aqui temos três estratégias que é: que começa com aquela String, que termina com aquela
                // String ou que qualquer parte do texto contenha aquela String
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Autor> autorExample = Example.of(autor, matcher);

        // Aqui a gente vai chamar o repository com o metodo findAll e ele tem a sobrecarga que recebe um Example
        return repository.findAll(autorExample);
    }

    // Metodo para verificar se o autor possui algum livro
    public boolean possuiLivro(Autor autor) {
        return livroRepository.existsByAutor(autor);
    }
}
