package io.github.fabiokusaba.libraryapi.controller;

import io.github.fabiokusaba.libraryapi.controller.dto.AutorDTO;
import io.github.fabiokusaba.libraryapi.controller.mappers.AutorMapper;
import io.github.fabiokusaba.libraryapi.model.Autor;
import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.service.AutorService;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Quando queremos transformar uma classe Spring em um controlador Rest a gente usa a annotation RestController e também vamos
// colocar a annotation RequestMapping aqui nós vamos mapear qual a URL que esse controlador vai escutar.
@RestController
@RequestMapping("/autores") // http://localhost:8080/autores
@RequiredArgsConstructor
@Tag(name = "Autores")
@Slf4j
public class AutorController implements GenericController {

    // Vamos fazer a injeção do nosso serviço no nosso controlador
    private final AutorService service;
    private final AutorMapper mapper;
    // Injetando o meu UsuarioService
    //private final UsuarioService usuarioService;

    // Para falarmos que esse metodo vai ser um POST nós precisamos anotá-lo com PostMapping, porém temos duas formas de fazer o
    // mapeamento: uma delas é utilizando o PostMapping e a outra é utilizando o RequestMapping passando alguns parâmetros
    // No contrato da nossa API para cadastrarmos um novo Autor vamos ter como input(entrada) o nome, dataNascimento e nacionalidade
    // só que no Autor temos mais propriedades do que isso, então para resolvermos esse problema vamos fazer o uso do padrão DTO
    // então ao invés de receber a entidade vou receber um objeto que vai ser da minha camada representacional
    // Quando estou no controlador estou na camada onde é a entrada de dados do meu sistema e eu não posso permitir que a entrada de
    // dados receba dados que não estejam dentro do contrato
    // Ao invés de utilizar a entidade que faz parte da nossa camada de persistência vamos utilizar o AutorDTO que faz parte da
    // camada de representação (contrato da API) e que tem as informações necessárias para criar um novo Autor
    // Vamos utilizar a annotation RequestBody para indicar que esse objeto autor vai vir no body da requisição
    // Vamos ter como retorno uma classe ResponseEntity que você pode parametrizar o tipo de retorno, é uma classe que serve para
    // representar uma resposta, então ele representa todos os dados que você pode retornar em uma resposta
    // O tipo parametrizado que passamos no ResponseEntity se refere ao body da nossa resposta, no nosso caso o cadastrar um novo
    // Autor não possui body de resposta então para isso podemos utilizar Object que significa que eu posso retornar qualquer coisa
    // ou Void que quer dizer que não há retorno
    // Para conseguirmos aplicar a validação nesse campo autor além da anotação RequestBody nós vamos colocar a annotation Valid, então
    // quando eu coloco essa annotation ele vai validar já na entrada, ele vai fazer a leitura das annotations do nosso AutorDTO e se o
    // campo está atendendo ao nosso requisito
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Salvar", description = "Cadastrar novo autor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastrado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Erro de validação"),
            @ApiResponse(responseCode = "409", description = "Autor já cadastrado")
    })
    public ResponseEntity<Void> salvar(@RequestBody @Valid AutorDTO dto, Authentication authentication) {

        log.info("Cadastrando novo autor: {}", dto.nome());

        // A ideia é que quando eu vou cadastrar um autor, por exemplo, a minha solicitação quando chegar no backend, na
        // API eu quero pegar quem é esse usuário que fez esse cadastro e salvar lá na base de dados como uma forma de
        // auditoria
        // A pergunta é como é que eu faço para descobrir qual foi esse usuário que enviou a requisição? Como vimos nos
        // estudos do Spring Security nós temos aqui no meio de tudo um objeto chamado Authentication que é produzido
        // pelo Spring Security pra identificar quem é que está autenticado naquele momento, quais são as credenciais de
        // quem está acessando a API naquele momento
        // Então, basta eu injetar ele na requisição, o Spring Security permite que em qualquer requisição você possa
        // injetar aqui um objeto do tipo Authentication, com isso eu tenho acesso a quem é que está fazendo a request
        // Esse objeto Authentication tem as authorities, credenciais, detalhes, objeto Principal que é o objeto que
        // representa o usuário, se ele está autenticado e setAuthenticated(), o metodo que a gente vai utilizar para
        // descobrir quem é esse usuário é o getPrincipal() que vai retornar quem é que está acessando, qual é a
        // identificação daquele usuário dentro da Authentication
        // Como Authentication é bem genérico, ele retorna Object, a gente precisa saber que instância é essa de
        // Authentication e percebemos que ele é um UserDetails.User porque a gente está utilizando UserDetailsService
        // então esse objeto foi encapsulado dentro da Authentication dentro do campo principal, agora já sabemos que
        // através desse objeto User eu consigo pegar o login do usuário
        // Como getPrincipal() retorna um Object a gente vai fazer um casting aqui para UserDetails
        //UserDetails usuarioLogado = (UserDetails) authentication.getPrincipal();

        // Pegando qual é o usuário
        //Usuario usuario = usuarioService.obterPorLogin(usuarioLogado.getUsername());

        // Tratativa do erro utilizando try-catch
        // Transformando um DTO em Autor
        var autor = mapper.toEntity(dto);

        // Com o usuário em mãos basta eu vir aqui e chamar o setIdUsuario() passando o seu id, então agora já sei qual
        // foi o usuário que cadastrou esse autor
        //autor.setIdUsuario(usuario.getId());

        // Chamando o service para salvar o autorEntidade, a partir desse momento ela já possui um id
        service.salvar(autor);

        // Builder de componentes URI, ele vai criar uma URI, vou utilizar o metodo fromCurrentRequest, ou seja, a partir da
        // requisição atual ele vai pegar os dados para construir uma nova URL e eu quero os dados da requisição atual porque
        // ela vai ter o domínio e o path para essa API, vou preencher o path que vai com o id e utilizo buildAndExpand para
        // passar os argumentos, ao final chamo toUri para transformar no objeto do tipo URI
        // http://localhost:8080/autores/{id}
        URI location = gerarHeaderLocation(autor.getId());

        // Vamos fazer uma lógica que se der erro ao salvar o autor, nós temos uma regra de negócio que não permite
        // cadastrar um autor com mesmo nome, data de nascimento e nacionalidade, então vou fazer uma validação que se
        // der erro no meu metodo salvar eu vou retornar o nosso ErroResposta

        // O ResponseEntity já tem aqui alguns metodos estáticos, vou utilizar o created e ele recebe como parâmetro a URI e
        // ao final eu chamo o build
        return ResponseEntity.created(location).build();
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    @Operation(summary = "Obter Detalhes", description = "Retorna os dados do autor pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor encontrado"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado")
    })
    public ResponseEntity<AutorDTO> obterDetalhes(@PathVariable("id") String id) {
        // Transformando o id do autor que estou recebendo pela URL em UUID
        var idAutor = UUID.fromString(id);

        // Podemos eliminar toda essa lógica e complexidade do código abaixo da seguinte forma:
        return service
                .obterPorId(idAutor)
                .map(autor -> {
                    AutorDTO dto = mapper.toDTO(autor);
                    return ResponseEntity.ok(dto);
                }).orElseGet(() -> ResponseEntity.notFound().build());

        // Chamando o service para obter o autor por id, aqui ele vai me retornar um objeto Optional do tipo Autor
        //Optional<Autor> autorOptional = service.obterPorId(idAutor);

        // Aqui faço a verificação se existe um autor para aquele id
        //if (autorOptional.isPresent()) {
        // Aqui eu utilizo o metodo get desse meu objeto Optional para que ele me retorne a entidade que está lá dentro
        //Autor autor = autorOptional.get();

        // Aqui vou criar um AutorDTO e quando eu construo um DTO como ele é um record ele já pede no construtor todos os
        // campos
        //AutorDTO dto = mapper.toDTO(autor);

        // Aqui retorno um ResponseEntity.ok e aqui como parâmetro eu tenho o body onde vou passar o dto
        //return ResponseEntity.ok(dto);
        //}

        // Caso contrário, ou seja, não existe um autor com aquele id retorno um Not Found
        //return ResponseEntity.notFound().build();
    }

    // Aqui eu tenho duas vertentes: ou retorno Not Found ou ignoro e retorno No Content, no caso de retornar para ambos os casos
    // de sucesso ou não o status No Content teria aqui o que chamamos de idempotente, ou seja, toda vez que eu fizer uma requisição
    // com os mesmos parâmetros ele sempre vai me retornar a mesma resposta, então independente de ter id ou não ele iria me dar
    // sucesso
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Deletar", description = "Deleta um autor existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado"),
            @ApiResponse(responseCode = "400", description = "Autor possui livro cadastrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable("id") String id) {

        log.info("Deletando autor de ID: {}", id);

        // Vamos transformar o id que recebemos em UUID
        var autorId = UUID.fromString(id);

        // Através do service vamos buscar o autor pelo id
        Optional<Autor> autorOptional = service.obterPorId(autorId);

        // Verificamos se o autor existe ou não, caso não exista retornamos um Not Found
        if (autorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Caso contrário, podemos deletar esse autor
        // Lembrando que o get retorna o autor que está dentro do Optional
        service.deletar(autorOptional.get());

        // E aqui em baixo retorno um No Content para simbolizar que a operação foi bem sucedida
        return ResponseEntity.noContent().build();
    }

    // No contrato da nossa API temos um array que é representado aqui por uma lista, uma colection, mas usaremos a
    // lista porque ela é ordenada, do tipo AutorDTO, então sempre na entrada e na saída vamos estar utilizando DTO
    // porque ele faz parte da camada representacional
    // Sabemos que o nome e a nacionalidade são Query Params, ou seja, não fazem parte da URL/da identificação do
    // recurso, e para mapearmos Query Params a gente usa em cada parâmetro a annotation RequestParam que significa
    // parâmetro da requisição e aqui eu coloco o nome que vai vir na URL, como são parâmetros opcionais vou colocar
    // o required como false
    // Então, independente de passarem o nome ou a nacionalidade ou nenhum ele vai entrar na pesquisa porque ela vai
    // funcionar como um filtro, ou seja, se eu passar o nome ele vai considerar o nome na pesquisa, se eu passar o
    // nome e a nacionalidade vai considerar os dois, se eu passar só a nacionalidade ele vai considerar só a
    // nacionalidade
    // Ou seja, os parâmetros não são obrigatórios e posso passar um ou outro ou os dois, e por padrão o RequestParam
    // é required true, obrigatório, por isso precisamos falar que o required será false para dizer que não é
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    @Operation(summary = "Pesquisar", description = "Realiza pesquisa de autores por parâmetros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso")
    })
    public ResponseEntity<List<AutorDTO>> pesquisar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "nacionalidade", required = false) String nacionalidade) {

        // Adicionando logs
        log.trace("Pesquisa autores");
        log.debug("Pesquisa autores");
        log.info("Pesquisa autores");
        log.warn("Pesquisa autores");
        log.error("Pesquisa autores");

        // Chamando o metodo pesquisa do nosso service passando os parâmetros fornecidos na requisição
        List<Autor> resultado = service.pesquisaByExample(nome, nacionalidade);

        // Transformando a lista de Autor em uma lista do tipo AutorDTO
        List<AutorDTO> lista = resultado
                .stream()
                // Aqui estamos utilizando method reference, então é a referência do metodo toDTO ele vai aplicar na
                // entrada que é autor
                // Quando o parâmetro que temos aqui é igual ao parâmetro do metodo que você quer chamar aqui dentro
                // então você pode utilizar essa sintaxe
                .map(mapper::toDTO)
                .collect(Collectors.toList());

        // Ao final, retorno a lista de DTO
        return ResponseEntity.ok(lista);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Atualizar", description = "Atualiza um autor existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado"),
            @ApiResponse(responseCode = "409", description = "Autor já cadastrado")
    })
    public ResponseEntity<Void> atualizar(@PathVariable("id") String id, @RequestBody @Valid AutorDTO dto) {
        // Preciso verificar se existe esse autor com o id que foi informado
        // Pegamos o id informado e transformamos em UUID
        var idAutor = UUID.fromString(id);

        // Com o nosso service pesquisamos por esse autor pelo id
        Optional<Autor> autorOptional = service.obterPorId(idAutor);

        // Se estiver vazio, ou seja, não existe autor para esse id, retornamos um Not Found
        if (autorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Aqui vamos implementar o que vai acontecer se o autor estiver presente, ou seja, atualizar os seus dados
        // Obtendo a entidade autor que veio do banco através do metodo get
        var autor = autorOptional.get();

        // Agora preciso atualizar os dados desse autor pelos dados vindos do DTO
        autor.setNome(dto.nome());
        autor.setNacionalidade(dto.nacionalidade());
        autor.setDataNascimento(dto.dataNascimento());

        // Finalmente chamamos o service para atualizar os dados
        service.atualizar(autor);

        // E aqui preciso retornar um ResponseEntity com o status No Content
        return ResponseEntity.noContent().build();
    }
}
