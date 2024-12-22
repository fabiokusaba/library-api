package io.github.fabiokusaba.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;

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
                .accessTokenTimeToLive(Duration.ofMinutes(60))
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
}
