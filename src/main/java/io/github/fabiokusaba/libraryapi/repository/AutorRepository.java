package io.github.fabiokusaba.libraryapi.repository;

import io.github.fabiokusaba.libraryapi.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Para a criação dos nossos repositories vamos utilizar o sufixo Repository, então para cada camada aqui vamos ter um
// AutorController, AutorService, AutorRepository que é a camada de dados e quando a gente vai criar um repository a
// gente precisa definir ele como interface
// Quem vai prover a implementação dessa interface não é a gente, é o Spring Data JPA, então você precisa extender de
// outra interface, quando a gente vai implementar uma interface a gente utiliza implements, mas aqui trata-se de uma
// interface e não vamos estar implementando por isso dizemos que uma interface extende outra interface, e a interface
// que vamos estar trabalhando é a JpaRepository
// Aqui precisamos passar dois parâmetros, então a gente vai passar a entidade e o tipo do id dessa nossa entidade
// Então, aqui apenas declarei um repository extendendo JpaRepository e a partir daqui já posso injetar ele, já tenho
// ele registrado no container de injeção de dependência, não precisamos colocar nenhuma annotation como Component,
// Repository que é o tipo dele, fica opcional utilizar essas anotações
// Podemos dizer que o JpaRepository é um agregado de várias outras interfaces que cada um tem os métodos com as
// operações no banco
// O objeto Example é exatamente isso que ele quer dizer um exemplo, então eu tenho um objeto Autor vou preencher ele
// com algumas informações e ele vai pegar os dados que estão preenchidos e vai utilizar na query, então se eu preencher
// só o nome daquele Autor ele vai fazer uma query considerando apenas o nome do Autor na busca, se eu preencher esse
// Autor com nome e nacionalidade ele vai considerar os dois na busca, se eu preencher além desses dois mais outro campo
// por exemplo a data de nascimento, ele também vai considerar na busca, então ele vai utilizar o objeto preenchido como
// exemplo pra criar uma query dinâmica baseada nas propriedades que foram preenchidas
public interface AutorRepository extends JpaRepository<Autor, UUID> {

    List<Autor> findByNome(String nome);
    List<Autor> findByNacionalidade(String nacionalidade);
    List<Autor> findByNomeAndNacionalidade(String nome, String nacionalidade);

    Optional<Autor> findByNomeAndDataNascimentoAndNacionalidade(
            String nome, LocalDate dataNascimento, String nacionalidade
    );
}
