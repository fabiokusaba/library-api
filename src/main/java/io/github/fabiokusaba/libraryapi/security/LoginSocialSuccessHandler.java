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
import java.util.List;

// Aqui precisamos implementar a interface AuthenticationSuccessHandler, mas podemos utilizar uma implementação que ele
// já tem então para isso vamos extender de SavedRequestAwareAuthenticationSuccessHandler
// Essa interface AuthenticationSuccessHandler tem o metodo onAuthenticationSuccess com duas implementações, vamos
// utilizar aquele que recebe três parâmetros: request, response e authentication
// Fez o login no Google na hora que terminou de fazer login ele entra aqui na nossa aplicação denovo e vai chamar essa
// classe aqui passando a authentication
@Component // Registranto no nosso contexto
@RequiredArgsConstructor // Criando um construtor com os argumentos necessários
public class LoginSocialSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String SENHA_PADRAO = "321";

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

        // Dessa forma que fizemos a implementação o usuário já precisa estar cadastrado no banco de dados porque quando
        // a gente faz o login social ele vai pegar o usuário pelo email e vai criar a CustomAuthentication e depois
        // setar essa authentication no SecurityContextHolder, mas o que acontece se eu fizer o login com o Google e
        // esse usuário não existir? A authentication vai estar nula e qual estratégia podemos utilizar para resolver
        // esse problema? Quando ele se autenticar com o Google nós podemos cadastrar ele, então podemos fazer uma
        // verificação se o usuário é nulo, ou seja, ele não achou um usuário para esse email é a primeira vez que ele
        // está logando com o Google e com esse email, ele não está previamente cadastrado
        if (usuario == null) {
            usuario = cadastrarUsuarioNaBase(email);
        }

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

    private Usuario cadastrarUsuarioNaBase(String email) {
        Usuario usuario;
        // Instânciando um novo usuário
        usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setLogin(obterLoginApartirDoEmail(email));
        usuario.setSenha(SENHA_PADRAO);
        usuario.setRoles(List.of("OPERADOR"));

        // Chamando o service para salvar esse usuário
        usuarioService.salvar(usuario);
        return usuario;
    }

    private String obterLoginApartirDoEmail(String email) {
        // Nota: da posição "0" até o índice do "@", ou seja, se o email for pessoa@email.com ele vai pegar da posição
        // "0" da String, início da palavra pessoa, até a letra "a" que vem antes do "@"
        // No metodo substring o segundo index é exclusivo
        return email.substring(0, email.indexOf("@"));
    }
}
