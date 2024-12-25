package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Precisamos converter agora essa JwtAuthenticationToken na nossa CustomAuthentication para isso vamos criar um filtro
// que vai fazer o seguinte: quando o resource server receber esse token a gente vai ver dentro da cadeia de filtros do
// Spring Security o momento em que ele pega esse token, ele termina de se autenticar, recebe esse token para pegar
// dessa authentication o usuário e obter ele pelo login, então conseguimos obter uma CustomAuthentication pelo login
// Precisamos registrar esse filtro para dizer em que parte do processo de autenticação ele vai entrar, queremos
// adicionar esse filtro depois que ele se autenticou com JWT e gerou essa authentication, sabemos que o Spring Security
// quando autentica o usuário, loga o usuário na aplicação, ele gera uma authentication, vamos no nosso resource server
// que é a nossa classe SecurityConfiguration através do metodo addFilterAfter vamos adicionar o filtro depois do filtro
// do OAuth2
// Por que eu tenho que fazer essa configuração no resource server? Porque o resource server é que recebe o token e
// autentica o usuário, obtém as informações daquele token que está recebendo
@Component
@RequiredArgsConstructor
public class JwtCustomAuthenticationFilter extends OncePerRequestFilter {

    private final UsuarioService usuarioService;

    // Esse metodo recebe três parâmetros: a request, a response e o filter chain. Esse filter chain é a cadeia de
    // filtros
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Eu quero aqui pegar a Authentication e como faço para pegá-la? Vou criar o meu objeto Authentication, vou
        // receber o SecurityContextHolder e assim consigo pegar o Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Tenho que verificar o seguinte: nossa aplicação eu consigo me autenticar com authorization basic, com o
        // formulário de login e com a authentication do Google, fora OAuth2, então aqui eu tenho que entender que eu
        // só vou fazer essa customização se for uma autenticação do OAuth2, pois as outras já estão customizadas
        // Então, se a authentication for uma instância de JwtAuthenticationToken eu preciso customizar
        if (deveConverter(authentication)) {
            // Aqui é onde de fato vamos fazer o intercept
            // Primeiramente vou pegar o login do usuário que se autenticou
            String login = authentication.getName();

            // Vamos pegar o usuário no nosso sistema
            Usuario usuario = usuarioService.obterPorLogin(login);

            // Verificamos se o usuário não é nulo, ou seja, existe um usuário com esse login
            if (usuario != null) {
                // E aqui vou dizer que essa authentication vai receber uma CustomAuthentication passando o usuário aqui
                // para dentro
                authentication = new CustomAuthentication(usuario);

                // Chamamos o SecurityContextHolder novamente para setar a nossa authentication, então eu sobrescrevi a
                // authentication do OAuth2 que veio para a nossa authentication e a partir de agora ela vai estar
                // customizada, ou seja, ela vai ser agora a CustomAuthentication
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Vou pegar esse filterChain com o metodo doFilter para ele passar para o próximo filtro, para ele continuar a
        // requisição aí você passa a request e o response como parâmetros
        // Então, a primeira coisa que a gente faz: toda vez que você implementar um filtro é ter uma forma de passar a
        // requisição para frente, a gente só vai interceptar aqui no meio, mas depois vai seguir a requisição
        filterChain.doFilter(request, response);
    }

    // Criando um metodo para verificar se devemos converter essa Authentication
    private boolean deveConverter(Authentication authentication) {
        return authentication != null && authentication instanceof JwtAuthenticationToken;
    }

}
