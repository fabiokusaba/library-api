package io.github.fabiokusaba.libraryapi.controller.mappers;

import io.github.fabiokusaba.libraryapi.controller.dto.UsuarioDTO;
import io.github.fabiokusaba.libraryapi.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    Usuario toEntity(UsuarioDTO dto);
    UsuarioDTO toDTO(Usuario entity);
}
