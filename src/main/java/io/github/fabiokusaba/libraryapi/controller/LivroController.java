package io.github.fabiokusaba.libraryapi.controller;

import io.github.fabiokusaba.libraryapi.controller.dto.CadastroLivroDTO;
import io.github.fabiokusaba.libraryapi.controller.dto.ResultadoPesquisaLivroDTO;
import io.github.fabiokusaba.libraryapi.controller.mappers.LivroMapper;
import io.github.fabiokusaba.libraryapi.model.GeneroLivro;
import io.github.fabiokusaba.libraryapi.model.Livro;
import io.github.fabiokusaba.libraryapi.service.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("livros")
// Utilizando aqui na classe ele vai aplicar essa regra para todos os metodos
//@PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
@RequiredArgsConstructor
public class LivroController implements GenericController {

    private final LivroService service;
    private final LivroMapper mapper;

    // Quem é que pode salvar livros? Tanto o Operador quanto o Gerente e como eu coloco essa regra? Eu tenho essa
    // annotation que é o PreAuthorize e aqui dentro a gente coloca a regra de acesso pra ele poder salvar, e a regra
    // que colocamos aqui é a mesma hasRole, hasAnyRole e passamos esses valores como String mesmo, então aqui ele vai
    // ler essa regra e vai permitir que eu possa salvar se eu tiver com qualquer uma dessas roles aqui
    // Perceba que fica bem mais interessante colocar essas regras de acesso nos endpoints porque se eu precisar
    // visualizar uma regra de acesso no endpoint basta eu ir até o Controller encontrar o endpoint e ver quem é que
    // pode acessar
    // Então, temos a opção de fazer em cada endpoint ou aqui em cima da classe e se eu colocar aqui ele vai aplicar
    // essa regra para todos os metodos
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Void> salvar(@RequestBody @Valid CadastroLivroDTO dto) {
        // Primeiro passo: mapear DTO para entidade
        Livro livro = mapper.toEntity(dto);

        // Enviar a entidade para o service validar e salvar na base
        service.salvar(livro);

        // Criar URL para acesso dos dados do livro
        var url = gerarHeaderLocation(livro.getId());

        // Retornar código created com header location
        return ResponseEntity.created(url).build();
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<ResultadoPesquisaLivroDTO> obterDetalhes(@PathVariable("id") String id) {
        return service.obterPorId(UUID.fromString(id))
                .map(livro -> {
                    ResultadoPesquisaLivroDTO dto = mapper.toDTO(livro);
                    return ResponseEntity.ok(dto);
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Object> deletar(@PathVariable("id") String id) {
        return service.obterPorId(UUID.fromString(id))
                .map(livro -> {
                    service.deletar(livro);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Page<ResultadoPesquisaLivroDTO>> pesquisa(
            // Como são opcionais vamos colocar required como sendo false
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "nome-autor", required = false) String nomeAutor,
            @RequestParam(value = "genero", required = false) GeneroLivro genero,
            @RequestParam(value = "ano-publicacao", required = false) Integer anoPublicacao,
            @RequestParam(value = "pagina", defaultValue = "0") Integer pagina,
            @RequestParam(value = "tamanho-pagina", defaultValue = "10") Integer tamanhoPagina
    ) {
        var paginaResultado = service.pesquisa(isbn, titulo, nomeAutor, genero, anoPublicacao, pagina, tamanhoPagina);

        Page<ResultadoPesquisaLivroDTO> resultado = paginaResultado.map(mapper::toDTO);

        return ResponseEntity.ok(resultado);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Object> atualizar(@PathVariable("id") String id, @RequestBody @Valid CadastroLivroDTO dto) {
        // Primeira coisa que a gente vai fazer -> ele vai obterPorId pegando esse id que passamos, transformamos o id
        // em UUID, então primeiro ele buscou o livro na base de dados, caso ele não encontre retorne um Not Found
        return service.obterPorId(UUID.fromString(id))
                .map(livro -> {
                    // Aqui vamos fazer a lógica para ele atualizar esse livro que veio do banco com os dados novos que
                    // vieram do DTO, só que aqui tem um detalhe
                    // Primeiramente vamos chamar o mapper para transformar esse dto em entidade, então esse livro já
                    // tem os dados que eu quero atualizar e o que vamos fazer aqui? Para não ter aquele problema dele
                    // atualizar esses campos (dataCadastro, dataAtualizacao, idUsuario) para valores nulos a gente vai
                    // só preencher esse livro aqui com os dados atualizados e por que não fiz diretamente já chamando
                    // os setters? Porque o dto só tem o id do autor, ele não tem o autor que foi selecionado e no
                    // mapper a gente tem aquela implementação onde ele busca através do id do autor o autor então ele
                    // já preenche o livro do jeito que precisamos, por isso chamei o mapper.toEntity() e transformei
                    // ele em entidade
                    Livro entidadeAuxiliar = mapper.toEntity(dto);

                    // Atualizando o livro
                    livro.setDataPublicacao(entidadeAuxiliar.getDataPublicacao());
                    livro.setIsbn(entidadeAuxiliar.getIsbn());
                    livro.setPreco(entidadeAuxiliar.getPreco());
                    livro.setGenero(entidadeAuxiliar.getGenero());
                    livro.setTitulo(entidadeAuxiliar.getTitulo());
                    livro.setAutor(entidadeAuxiliar.getAutor());

                    // Aqui chamamos o service
                    service.atualizar(livro);

                    // Retornamos um No Content indicando que a operação foi bem sucedida
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
