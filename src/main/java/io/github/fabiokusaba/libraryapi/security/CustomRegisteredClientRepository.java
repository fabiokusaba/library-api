package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

// Como declaramos essa classe como Component não precisamos registrá-la em nenhum outro local, se ele achar essa
// implementação de RegisteredClientRepository o Authorization Server automaticamente já vai pegar essa implementação
// e vai buscar os clients por aqui.
@Component
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientService clientService;

    @Override
    public void save(RegisteredClient registeredClient) {}

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        // Obtendo o client cadastrado no banco
        var client = clientService.obterPorClientId(clientId);

        // Fazendo a verificação do nosso client, caso seja nulo significa que não existe um client e portanto ele não
        // vai ficar autenticado
        if (client == null) {
            return null;
        }

        // Preenchendo as informações do RegisteredClient e retornando
        return RegisteredClient
                .withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .redirectUri(client.getRedirectUri())
                .scope(client.getScope())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }
}
