package io.github.fabiokusaba.libraryapi.controller.mappers;

import io.github.fabiokusaba.libraryapi.controller.dto.AutorDTO;
import io.github.fabiokusaba.libraryapi.model.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Todos os mappers do MapStruct são interfaces
// Vamos colocar a primeira annotation que é do MapStruct que é o Mapper e vamos transformar ele em um componente do
// Spring e para isso temos a propriedade componentModel e ele vai transformar isso aqui em um componente na hora da
// compilação e a gente vai poder injetar ele aonde a gente precisar
// Criamos uma camada de mapeamento (mappers) então essa classe aqui é responsável por mapear os objetos pra gente, nós
// tiramos a responsabilidade de dentro do DTO de mapear para a entidade e trouxemos pra cá
@Mapper(componentModel = "spring")
public interface AutorMapper {

    // Declarando mappings
    // Aqui temos o detalhe que, vamos supor que no DTO tem um campo cujo nome é diferente da entidade, então aqui se eu
    // quiser fazer esse mapeamento posso utilizar a annotation Mapping e coloco source (origem) que no caso seria o DTO
    // e o target (destino) que no caso seria a entidade, agora quando as propriedades possuem o mesmo nome isso se
    // torna opcional, não necessário
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "dataNascimento", target = "dataNascimento")
    @Mapping(source = "nacionalidade", target = "nacionalidade")
    Autor toEntity(AutorDTO dto);

    AutorDTO toDTO(Autor autor);
}
