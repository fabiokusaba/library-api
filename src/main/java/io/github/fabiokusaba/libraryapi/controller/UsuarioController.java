package io.github.fabiokusaba.libraryapi.controller;

import io.github.fabiokusaba.libraryapi.controller.dto.UsuarioDTO;
import io.github.fabiokusaba.libraryapi.controller.mappers.UsuarioMapper;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void salvar(@RequestBody UsuarioDTO dto) {
        // Fazendo o mapeamento DTO - Entity
        var usuario = mapper.toEntity(dto);
        // Chamando o service para salvar o usuário
        service.salvar(usuario);
    }
}
