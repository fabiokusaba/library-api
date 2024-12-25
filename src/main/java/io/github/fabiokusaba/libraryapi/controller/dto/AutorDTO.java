package io.github.fabiokusaba.libraryapi.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

// Para a sua criação você pode utilizar tanto classe quanto record só que record é um objeto simples e o Spring Boot trabalha com
// records também.
// O que é que significa DTO? Significa Data Transfer Object, é um objeto de transferência de dados, ou seja, eu quero só receber
// esse objeto que é da minha camada de representação (modelo representacional), faz parte do meu contrato, mas não faz parte das
// outras camadas
// O record é uma classe imutável, ou seja, depois que você constrói você não muda os valores, mas basicamente nós queremos receber
// esses dados preenchidos pra passar para a entidade e criar um Autor a partir dessas informações, então não precisamos dos set
// apenas dos get
// Aqui vamos colocar os dados que são exigidos no contrato da nossa API
// A diferença das anotações do pacote Validation NotBlank e NotNull são que o NotBlank ele é igual ao NotNull mais específico para 
// Strings ele quer dizer que eu quero que essa String não venha nula e também não venha vazia porque ele pode passar uma String
// vazia então a String vazia ela não é nula mas ela está vazia por isso que a gente utiliza o NotBlank no lugar do NotNull para 
// em Strings, o NotNull é para campos que podem vir nulos então eu não tenho como preencher só um pedaço da data de nascimento eu
// tenho que preencher ela completa para ter um objeto Date
// Então, essas annotations vão barrar agora assim que a gente fizer a chamada da API para que esses campos não venham em branco ou
// nulos, aqui temos a opção também de colocar uma mensagem então abrimos o parâmetro message e passamos a mensagem que queremos
// Um detalhe importante é que fazemos essas validações sempre na entrada do sistema, ou seja, o input do nosso sistema é na API
// por isso que a gente coloca essas validações no DTO para que a gente já receba o feedback antes mesmo dele tentar salvar no
// banco de dados essa informação, então quando chega na camada de domínio que é a nossa classe Autor ele já chega validado com todos
// os campos para serem salvos na base de dados
// Mais um motivo para você utilizar o padrão DTO é esse, você vai utilizar ele pra fazer essas validações não deixando com que a camada
// de persistência já chegue quebrada e também você não precisa deixar sujar, deixar muita sujeira nas suas entidades
@Schema(name = "Autor")
public record AutorDTO(
    UUID id,
    @NotBlank(message = "Campo obrigatório")
    @Size(min = 3, max = 100, message = "Campo fora do tamanho padrão")
    @Schema(name = "nome")
    String nome, 
    @NotNull(message = "Campo obrigatório")
    @Past(message = "Não pode ser uma data futura")
    @Schema(name = "dataNascimento")
    LocalDate dataNascimento, 
    @NotBlank(message = "Campo obrigatório")
    @Size(min = 2, max = 50, message = "Campo fora do tamanho padrão")
    @Schema(name = "nacionalidade")
    String nacionalidade) {

    // Podemos aqui dentro do corpo do nosso DTO criar um metodo para fazer o mapeamento para Autor, então eu vou transformar esse
    // meu AutorDTO em Autor, apesar dele ser um record ele é uma classe normal em que você pode criar metodos normalmente
    //public Autor mapearParaAutor() {
        //Autor autor = new Autor();
        //autor.setNome(this.nome);
        //autor.setDataNascimento(this.dataNascimento);
        //autor.setNacionalidade(this.nacionalidade);
        //return autor;
    //}
}
