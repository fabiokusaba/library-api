package io.github.fabiokusaba.libraryapi.service;

import io.github.fabiokusaba.libraryapi.model.GeneroLivro;
import io.github.fabiokusaba.libraryapi.model.Livro;
import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.repository.LivroRepository;
import io.github.fabiokusaba.libraryapi.repository.specs.LivroSpecs;
import io.github.fabiokusaba.libraryapi.security.SecurityService;
import io.github.fabiokusaba.libraryapi.validator.LivroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository repository;
    private final LivroValidator validator;
    private final SecurityService securityService;

    public Livro salvar(Livro livro) {
        validator.validar(livro);
        Usuario usuario = securityService.obterUsuarioLogado();
        livro.setUsuario(usuario);
        return repository.save(livro);
    }

    public Optional<Livro> obterPorId(UUID id) {
        return repository.findById(id);
    }

    public void deletar(Livro livro) {
        repository.delete(livro);
    }

    public Page<Livro> pesquisa(
            String isbn,
            String titulo,
            String nomeAutor,
            GeneroLivro genero,
            Integer anoPublicacao,
            Integer pagina,
            Integer tamanhoPagina) {
        // Nosso repository agora tem o metodo findAll que recebe um objeto do tipo Specification e aqui vamos criar
        // esse objeto de Specification, abaixo temos um exemplo de uma Specification completa
        //Specification<Livro> specs = Specification
                //.where(LivroSpecs.isbnEqual(isbn))
                //.and(LivroSpecs.tituloLike(titulo))
                //.and(LivroSpecs.generoEqual(genero));

        // Eu sempre tenho que começar com where só que aqui pode ser que eu não tenha passado nenhum desses campos
        // então como é que eu faço para inicializar essa Specification se eu não sei quais foram os campos que passei?
        // Posso usar a estratégia do '0 = 0' ou '1 = 1' e como é que eu faço para criar essa Specification do '0 = 0'?
        // Coloco um objeto Specification aqui dentro e utilizo conjunction, a conjunction nada menos é do que um
        // critério verdadeiro então basicamente ele vai retornar '0 = 0', isso que acabamos de codificar se traduz em:
        // select * from livro where 0 = 0
        // Agora que já inicializamos a nossa specs podemos fazer as nossas verificações com os nossos ifs
        Specification<Livro> specs = Specification
                .where(((root, query, cb) -> cb.conjunction()));

        if (isbn != null) {
            // A specs é um objeto mutável, então pra adicionar um novo critério dentro dessa specs eu tenho que receber
            // ela mesma e usar o and aqui, ou seja, 'query = query and isbn = :isbn'
            specs = specs.and(LivroSpecs.isbnEqual(isbn));
        }

        if (titulo != null) {
            specs = specs.and(LivroSpecs.tituloLike(titulo));
        }

        if (genero != null) {
            specs = specs.and(LivroSpecs.generoEqual(genero));
        }

        if (anoPublicacao != null) {
            specs = specs.and(LivroSpecs.anoPublicacaoEqual(anoPublicacao));
        }

        if (nomeAutor != null) {
            specs = specs.and(LivroSpecs.nomeAutorLike(nomeAutor));
        }

        Pageable pageRequest = PageRequest.of(pagina, tamanhoPagina);

        return repository.findAll(specs, pageRequest);
    }

    public void atualizar(Livro livro) {
        // Aqui a gente vai fazer a mesma lógica que foi feita no serviço de Autor
        // Se o id do livro for nulo, ou seja, não posso chamar o atualizar se o livro não está salvo na base
        if (livro.getId() == null) {
            throw new IllegalArgumentException("Para atualizar, é necessário que o livro já esteja salvo na base.");
        }

        validator.validar(livro);

        repository.save(livro);
    }
}
