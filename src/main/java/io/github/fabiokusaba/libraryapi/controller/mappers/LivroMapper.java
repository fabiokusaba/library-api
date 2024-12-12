package io.github.fabiokusaba.libraryapi.controller.mappers;

import io.github.fabiokusaba.libraryapi.controller.dto.CadastroLivroDTO;
import io.github.fabiokusaba.libraryapi.controller.dto.ResultadoPesquisaLivroDTO;
import io.github.fabiokusaba.libraryapi.model.Livro;
import io.github.fabiokusaba.libraryapi.repository.AutorRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

// O MapStruct permite que a gente utilize classes abstratas também para poder fazer os nossos mappers, então ao invés
// de utilizar interface vamos utilizar uma classe abstrata
@Mapper(componentModel = "spring", uses = AutorMapper.class)
public abstract class LivroMapper {

    @Autowired
    AutorRepository autorRepository;

    // Vamos olhar as diferenças no Livro ele vai mapear o isbn, titulo, dataPublicacao, genero, preco, nossos campos
    // lógicos que a gente vai gerar dataCadastro, dataAtualizacao, mas precisamos mapear o autor também, indo para o
    // nosso DTO temos isbn, titulo, dataPublicacao, genero, preco e aqui ao invés de ter o autor nós temos o idAutor
    // então vai complicar um pouco as coisas porque eu preciso pegar esse idAutor e transformar na entidade Autor e
    // como a gente faz isso? A gente faz isso indo ao banco de dados e recuperando o autor que possui aquele id para
    // finalmente setar no Livro
    // Para resolvermos esse problema existe uma estratégia no MapStruct pra injetar, por exemplo um repository ou então
    // um outro componente, pra fazer isso a gente utiliza classes abstratas
    // E agora como eu faço para pegar esse autorRepository buscar o autor que tem esse idAutor e jogar dentro de autor?
    // Aqui ao invés de usarmos source (mapear campos simples) vamos utilizar expression onde podemos passar uma
    // expressão e aqui eu coloco qual o tipo de expressão que eu quero colocar e a expressão em si
    @Mapping(target = "autor", expression = "java(autorRepository.findById(dto.idAutor()).orElse(null))")
    public abstract Livro toEntity(CadastroLivroDTO dto);

    // Aqui temos um pequeno probleminha o nosso DTO precisa desses campos que o compõe e aqui temos o AutorDTO e se a
    // gente vier na entidade Livro não temos o AutorDTO nós temos um objeto do tipo Autor, então aqui dentro do Mapper
    // temos um parâmetro que é o uses e aqui eu quero utilizar um outro mapper quando tiver algum objeto aqui a ser
    // mapeado com outro mapper, por exemplo aqui dentro do Livro eu tenho o Autor e temos um mapper que mapeia o Autor
    // então eu quero que ele use o AutorMapper pra utilizar esse metodo toDTO para fazer o mapeamento e é possível
    // fazer isso com MapStruct através dessa propriedade uses
    public abstract ResultadoPesquisaLivroDTO toDTO(Livro livro);
}
