package io.github.fabiokusaba.libraryapi.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String login;

    @Column
    private String senha;

    // Aqui nesse campo temos um detalhe importante, uma configuração a se fazer, lá no banco de dados esse tipo de dado
    // é um Array, no Java ele é uma lista de Strings, será que o JPA vai saber traduzir essa lista de Strings para um
    // Array no banco de dados? A resposta é não, mas tem uma biblioteca que vamos utilizar aqui que vai permitir que a
    // gente faça isso, ou seja, ela vai traduzir uma lista de Strings ou de qualquer outro tipo para um Array do banco
    // de dados (varchar[]) e a biblioteca é a hypersistence-utils
    // E aqui vou colocar um Type do hibernate, ele é como se fosse um tradutor, ou seja, ele vai traduzir esse campo
    // como tipo que eu passar aqui, no nosso caso é um ListArrayType.class que vem da biblioteca que instalamos
    @Type(ListArrayType.class)
    // Em columnDefinition eu vou dizer qual é o tipo dessa coluna lá no banco
    @Column(name = "roles", columnDefinition = "varchar[]")
    private List<String> roles;
}
