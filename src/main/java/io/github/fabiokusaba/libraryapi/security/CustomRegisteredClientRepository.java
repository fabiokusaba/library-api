package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

// Como declaramos essa classe como Component não precisamos registrá-la em nenhum outro local, se ele achar essa
// implementação de RegisteredClientRepository o Authorization Server automaticamente já vai pegar essa implementação
// e vai buscar os clients por aqui.
@Component
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientService clientService;
    private final TokenSettings tokenSettings;
    private final ClientSettings clientSettings;

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
                // Os grant types dizem qual é a forma que a gente está utilizando para se autenticar, qual é o fluxo de
                // autenticação, então authorization code é quando temos usuário (login e senha), client credentials é
                // de aplicação para aplicação, o refresh token é um pouco diferente porque ele só vai servir quando eu
                // já estou autenticado então primeiramente ele não funciona com client credentials ele vai fazer
                // sentido com authorization code ou com algum outro grant type que nós temos usuário, e ele só faz
                // sentido nessas condições porque ele não é um token de transação, o access token é de transação, o
                // refresh token vai servir pra gente renovar o access token, então imagine que você tem uma sessão de
                // usuário em que você vai se autenticar com login e senha e você terá uma sessão de 30, 60 minutos, o
                // usuário vai poder fazer as operações durante esse tempo e o que acontece quando passar 60 minutos?
                // Aquele access token vai estar expirado e o usuário vai ter que logar novamente, vamos supor que a
                // na nossa aplicação queremos dar uma experiência melhor para o nosso usuário então se ele está
                // trabalhando durante aquela hora toda e ainda está fazendo algum trabalho a gente pode extender o
                // tempo do token então a gente pode renovar aquele token para que ele continue trabalhando, utilizamos
                // o refresh token justamente pra isso, é um grant type diferente mas que também vai servir para obter
                // um novo token, de maneira geral todos os grant types servem para a gente obter um token só que a
                // diferença é que no refresh token você já tem um token e agora você precisa renovar ele
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .tokenSettings(tokenSettings)
                .clientSettings(clientSettings)
                .build();
    }
}
