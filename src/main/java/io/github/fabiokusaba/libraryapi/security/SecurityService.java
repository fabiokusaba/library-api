package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// Agora temos uma classe que toda vez que eu precisar do usuário logado é só utilizar o seu metodo obterUsuarioLogado
@Component
@RequiredArgsConstructor
public class SecurityService {
    // Aqui eu vou injetar o UsuarioService
    private final UsuarioService usuarioService;

    // Vou criar um metodo aqui que vai retornar um Usuario
    public Usuario obterUsuarioLogado() {
        // Uma outra forma da gente conseguir esse objeto Authentication é através do SecurityContextHolder, então de
        // dentro do contexto do Spring Security eu consigo pegar esse objeto Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vamos verificar se a authentication é uma instância de CustomAuthentication
        if (authentication instanceof CustomAuthentication customAuth) {
            // Se sim, então vou retornar o usuário que está contido no meu customAuth, o processo ficou até mais fácil
            // porque não precisamos ir ao banco o CustomAuthentication já tem esse usuário
            // Desta forma, a CustomAuthentication é a única forma de eu conseguir o usuário da aplicação então a partir
            // de agora alteramos o SecurityService e vamos conseguir descobrir qual é o usuário que está autenticado e
            // quando for fazer uma requisição para salvar um livro ou então salvar um autor ele vai popular aquela
            // informação
            return customAuth.getUsuario();
        }

        // Caso contrário, retornaremos um nulo
        return null;

        // Não vamos precisar mais do trecho de código abaixo porque não estamos utilizando mais o UserDetails
        //UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //String login = userDetails.getUsername();
        //return usuarioService.obterPorLogin(login);
    }
}
