package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Aqui precisamos implementar a interface AuthenticationSuccessHandler, mas podemos utilizar uma implementação que ele
// já tem então para isso vamos extender de SavedRequestAwareAuthenticationSuccessHandler
// Essa interface AuthenticationSuccessHandler tem o metodo onAuthenticationSuccess com duas implementações, vamos
// utilizar aquele que recebe três parâmetros: request, response e authentication
// Fez o login no Google na hora que terminou de fazer login ele entra aqui na nossa aplicação denovo e vai chamar essa
// classe aqui passando a authentication
@Component // Registranto no nosso contexto
@RequiredArgsConstructor // Criando um construtor com os argumentos necessários
public class LoginSocialSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        // Fazendo casting e pegando o objeto
        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        // Pegando o principal
        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();

        // Pegando o email
        String email = oAuth2User.getAttribute("email");

        // Com o email em mãos basta injetarmos o nosso service para buscarmos o usuário pelo email
        Usuario usuario = usuarioService.obterPorEmail(email);

        // Agora vamos partir para a criação da nossa CustomAuthentication passando o usuário que vai fazer parte da
        // sessão
        CustomAuthentication customAuthentication = new CustomAuthentication(usuario);

        // Agora vamos adicionar essa nossa customAuthentication, a authentication que está no contexto do Spring
        // Security é essa aqui Authentication que ele passou e está dentro do contexto, só que eu quero mudar essa
        // authentication para essa nossa customizada, para isso basta eu vir no SecurityContextHolder e setar essa
        // nossa customAuthentication
        SecurityContextHolder.getContext().setAuthentication(customAuthentication);

        // Agora eu preciso chamar o super para continuar, ou seja, para dar continuidade a essa requisição passando
        // esses três parâmetros
        super.onAuthenticationSuccess(request, response, customAuthentication);
    }
}
