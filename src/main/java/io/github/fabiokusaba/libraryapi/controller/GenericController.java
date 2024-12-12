package io.github.fabiokusaba.libraryapi.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

// Vai ser uma interface que vai ter alguns metodos que a gente vai poder utilizar aqui e os nossos controllers vão
// implementar essa interface e vão herdar os metodos que a gente vai colocar aqui
public interface GenericController {

    // Criamos um metodo default, então pra você criar um metodo com corpo dentro de uma interface você precisa declarar
    // como default
    default URI gerarHeaderLocation(UUID id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
