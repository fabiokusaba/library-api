package io.github.fabiokusaba.libraryapi.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.fabiokusaba.libraryapi.security.CustomAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfiguration {

    // A annotation Order vai definir qual a ordem que esse SecurityFilterChain vai ficar no nosso caso ele vai ser a
    // ordem 1, dentro da cadeia de filtros do Spring Security ele vai ficar como sendo o primeiro
    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // Com essa linha eu já habilitei o Authorization Server aqui nessa aplicação
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Como eu apliquei essa configuração agora eu tenho esse configurer aqui dentro do meu contexto
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // Open ID Connect -> plugin do OAuth2 que permite com que com o token em mãos eu consiga, por exemplo
                // obter as informações daquele token
                .oidc(Customizer.withDefaults());

        // Agora preciso configurar esse Authorization Server para ele validar os tokens dos Resources Servers
        // Estou dizendo aqui que vou utilizar o token JWT e esse Resource Server vai validar os tokens, inclusive gerá-
        // los por outras aplicações, na verdade gerado por ela e sendo utilizado por outras aplicações
        http.oauth2ResourceServer(oauth2Rs -> oauth2Rs.jwt(Customizer.withDefaults()));

        // Agora vou habilitar como é que ele vai se autenticar nesse Authorization Server, vimos que lá no Google a
        // gente redirecionava para o Google e lá tem o formulário do Google que o usuário digita o email e a senha e
        // se autentica, então aqui vou dizer como ele vai se autenticar e aqui vamos usar o formulário de login que a
        // gente criou
        http.formLogin(configurer -> configurer.loginPage("/login"));

        return http.build();
    }

    // O PasswordEncoder faz parte do Authorization Server porque é ele quem vai codificar as senhas e vai validar as
    // senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                // Aqui estamos dizendo que o access token dura 60 minutos e é o token utilizado nas requisições
                .accessTokenTimeToLive(Duration.ofMinutes(60))
                // Agora vou dizer a duração do refresh token, vamos supor que a sessão do usuário é de 60 minutos
                // extendível até 90 minutos, então o refresh token sempre vai ter uma duração maior que o access token
                // e é o token para renovar o access token
                .refreshTokenTimeToLive(Duration.ofMinutes(90))
                .build();
    }

    // requireAuthorizationConsent -> quando a gente vai se autenticar com o Google ele tem uma telinha em que eu tenho
    // que autorizar "Você permite que essa aplicação autorizada acesse as suas informações" e você dá o consentimento,
    // então não vamos trabalhar com essa telinha de consentimento por isso colocamos false.
    @Bean
    public ClientSettings clientSettings() {
        return ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build();
    }

    // Isso aqui é para gerar o token JWK, que significa JSON Web Key, que é uma representação em JSON de uma chave
    // criptográfica que pode ser usada em processos de autenticação e assinatura digital especialmente quando estamos
    // trabalhando com JWT, então a gente está trabalhando com JWT e a gente precisa de uma chave, um token JWK que é
    // para ele assinar o token
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        // Aqui a gente vai precisar de uma chave e vamos utilizar uma RSAKey que vamos passar no nosso jwkSet
        // Uma chave RSA é um tipo de chave criptográfica usada em criptografia assimétrica, então é um metodo de
        // criptografia onde a gente tem duas chaves diferentes: uma chave pública e uma privada, um dos algoritmos mais
        // utilizados para troca segura de informações e assinatura digital
        // Os componentes de uma chave RSA são: nós temos a chave pública que serve para criptografar os dados e ela
        // pode ser compartilhada publicamente sem comprometer a segurança. E ela tem uma chave privada que é usada para
        // descriptografar dados que foram criptografados com a chave pública, essa chave precisa ser mantida em segredo
        // nesse caso só quem sabe da chave privada é o servidor (AuthorizationServer) que vai utilizar para validar o
        // token e descriptografar ele
        RSAKey rsaKey = gerarChaveRSA();

        // Para conseguirmos uma instância de JWKSource temos aqui um objeto que é o JWKSet, então precisamos criar uma
        // instância de JWKSet e ele precisa de uma JWK key
        JWKSet jwkSet = new JWKSet(rsaKey);

        // E aqui a gente retorna um ImmutableJWKSet passando o jwkSet como parâmetro
        return new ImmutableJWKSet<>(jwkSet);
    }

    private RSAKey gerarChaveRSA() throws Exception {
        // Primeiramente vou precisar de um par de chaves e para isso vamos utilizar o KeyPairGenerator passando o
        // algoritmo, no nosso caso o RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

        // Agora que tenho o keyPairGenerator vou inicializar ele com 2048 bits
        keyPairGenerator.initialize(2048);

        // Agora através desse keyPairGenerator vou criar um par de chaves com KeyPair, uma chave pública e uma chave
        // privada
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // E o que vou precisar para gerar a minha RSA? Como esse par de chaves que gerei é uma RSA então vou pegar
        // primeiro a chave pública, como é uma classe genérica aqui vou precisar fazer o casting
        RSAPublicKey chavePublica = (RSAPublicKey) keyPair.getPublic();

        // E aqui também vou pegar a chave privada
        RSAPrivateKey chavePrivada = (RSAPrivateKey) keyPair.getPrivate();

        // Agora vou precisar do builder de RSAKey, no primeiro parâmetro vou passar a chavePublica, chamo o metodo
        // privateKey para passar a chavePrivada, preciso gerar o id para essa chave com o keyID
        return new RSAKey
                .Builder(chavePublica)
                .privateKey(chavePrivada)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    // Decoder -> como decodifica o token JWT
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // Customização de URLs -> quais são os endpoints que você pode utilizar para se autenticar, obter um token, revogar
    // o token, etc
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                // Obter token independente do grant type
                .tokenEndpoint("/oauth2/token")
                // Obter informações do token, por exemplo ver até quando aquele token vale, etc
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                // Revogação do token para quando você quer revogar o token antes de acabar o seu tempo de duração
                .tokenRevocationEndpoint("/oauth2/revoke")
                // Authorization Code -> encaminha para o formulário de login
                .authorizationEndpoint("/oauth2/authorize")
                // Obter informações do usuário Open ID Connect
                .oidcUserInfoEndpoint("/oauth2/userinfo")
                // Obter a chave pública para verificar a assinatura do token
                .jwkSetEndpoint("/oauth2/jwks")
                // Logout
                .oidcLogoutEndpoint("/oauth2/logout")
                .build();
    }

    // Customizando token JWT -> por ser um Bean ele vai ser registrado dentro do AuthorizationServer, ele vai ver que
    // tem um tokenCustomizer registrado e aí quando ele for customizar o token ele vai chamá-lo passando esse contexto
    // para executar a nossa lógica
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        // Vamos construir um objeto que vai receber esse context e quem é que passa esse context? É o Authorization
        // Server
        return context -> {
            // Esse principal é a authentication que vai vir e como a gente sabe a nossa aplicação só trabalha com
            // authentication que é a CustomAuthentication
            var principal = context.getPrincipal();

            // Para garantir vamos verificar se esse principal é instância de CustomAuthentication
            if (principal instanceof CustomAuthentication authentication) {
                // Vou verificar qual o tipo de token que temos aqui
                OAuth2TokenType tipoToken = context.getTokenType();

                // Vou verificar se esse tipoToken é um Access Token, token que mandamos na requisição, e o Refresh
                // Token é um grant type para renovarmos o nosso Access Token
                if (OAuth2TokenType.ACCESS_TOKEN.equals(tipoToken)) {
                    // Pegando as authorities
                    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                    // Mapeando as authorities
                    List<String> authoritiesList = authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();

                    // Quero pegar esse objeto do contexto com o metodo getClaims e aqui ele tem um metodo claim onde
                    // você coloca uma parâmetro e um valor
                    context.getClaims()
                            .claim("authorities", authoritiesList)
                            .claim("email", authentication.getUsuario().getEmail());
                }
            }
        };
    }
}
