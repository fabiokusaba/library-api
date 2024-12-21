package io.github.fabiokusaba.libraryapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

// Client que vai ficar registrado no nosso Authorization Server para que ele consiga obter tokens e autenticar usuários
// Essa nossa classe contém as informações necessárias para cadastrarmos no banco
@Entity
@Table
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "redirect_uri")
    private String redirectUri;

    @Column(name = "scope")
    private String scope;
}
