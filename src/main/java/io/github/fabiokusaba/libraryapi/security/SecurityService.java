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

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String login = userDetails.getUsername();

        return usuarioService.obterPorLogin(login);
    }
}
