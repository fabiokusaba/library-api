package io.github.fabiokusaba.libraryapi.validator;

import io.github.fabiokusaba.libraryapi.exceptions.CampoInvalidoException;
import io.github.fabiokusaba.libraryapi.exceptions.RegistroDuplicadoException;
import io.github.fabiokusaba.libraryapi.model.Livro;
import io.github.fabiokusaba.libraryapi.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LivroValidator {

    private static final int ANO_EXIGENCIA_PRECO = 2020;

    private final LivroRepository repository;

    public void validar(Livro livro) {
        if (existeLivroComIsbn(livro)) {
            throw new RegistroDuplicadoException("ISBN já cadastrado");
        }

        if (isPrecoObrigatorioNulo(livro)) {
            throw new CampoInvalidoException("preco",
                    "Para livros com ano de publicação a partir de 2020, o preço é obrigatório.");
        }
    }

    // Esse metodo aqui é pra dizer se o preço é obrigatório e está nulo
    private boolean isPrecoObrigatorioNulo(Livro livro) {
        return livro.getPreco() == null && livro.getDataPublicacao().getYear() >= ANO_EXIGENCIA_PRECO;
    }

    // Esse metodo aqui vai verificar se existe um livro com o isbn do livro que estou cadastrando ou atualizando
    // Desta forma, vai existir um livro em duas condições: a primeira é quando estou salvando a primeira vez esse livro
    // e existe um livro presente/salvo no banco de dados com esse isbn, e o segundo é se ele encontrou um livro mas o
    // livroEncontrado tem o id diferente desse livro que estou cadastrando
    private boolean existeLivroComIsbn(Livro livro) {
        // Basicamente vamos pegar o repository e buscar um livro pelo isbn, aqui no metodo vou passar o isbn do livro
        // que estou cadastrando ou atualizando
        Optional<Livro> livroEncontrado = repository.findByIsbn(livro.getIsbn());

        // E aqui vou fazer os testes
        // Primeiro teste é se estou cadastrando um livro quer dizer que o id dele é nulo, então se estou cadastrando
        // esse livro e já existe um livroEncontrado com esse isbn quer dizer que já existe um livro com esse isbn
        if (livro.getId() == null) {
            return livroEncontrado.isPresent();
        }

        // Agora vou fazer a lógica caso esteja atualizando esse livro, qual é a lógica? Se esse livroEncontrado tiver
        // um id diferente desse livro que estou tentando atualizar vou retornar verdadeiro também dizendo que já existe
        // um livro cadastrado com esse isbn
        // Ou seja, quero encontrar desse Optional, quero filtrar o item que não possua esse mesmo id aqui desse livro
        // ele vai verificar se o livroEncontrado não possui esse mesmo id de livro aí o anyMatch retorna verdadeiro ou
        // falso
        return livroEncontrado
                .map(Livro::getId) // capturando o id
                .stream()
                .anyMatch(id -> !id.equals(livro.getId()));
    }
}
