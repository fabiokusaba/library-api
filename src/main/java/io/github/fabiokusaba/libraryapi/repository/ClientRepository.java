package io.github.fabiokusaba.libraryapi.repository;

import io.github.fabiokusaba.libraryapi.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Client findByClientId(String clientId);
}
