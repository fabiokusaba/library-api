package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// Aqui vamos estar criando a nossa classe customizada para a interface UserDetailsService fazendo a sua implementação
// Eu poderia anotar aqui com Service, mas não vou fazer isso porque esse Bean userDetailsService que declaramos na
// classe SecurityConfiguration quero que fique dentro da configuração do Spring Security, então quem for ver a nossa
// classe vai ver que a fonte de dados de usuários, a fonte de usuários que vai servir para autenticar usuários no
// sistema vai estar nesse UserDetailsService
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    // Vou injetar o nosso service aqui
    private final UsuarioService service;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // E aqui vou fazer a lógica para pegar o nosso usuário através desse username, na nossa aplicação chamamos de
        // login
        Usuario usuario = service.obterPorLogin(login);
        // E aqui o que vou retornar?
        // Se o usuario for nulo, ou seja, não existe um usuario com esse login, vou lançar uma exceção
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado!");
        }
        // No caso dele encontrar o que eu preciso fazer? Aqui eu preciso retornar um UserDetails, tenho a interface
        // UserDetailsService que ela tem que prover uma instância de UserDetails, e se a gente abrir a UserDetails ela
        // tem que prover uma lista de authorities/roles, ou seja, tanto as permissões quanto os grupos de usuário, o
        // password e o username que é o login
        // Porque aqui quando eu retornar esses dados pro Spring dentro de UserDetailsService a primeira coisa que ele
        // vai fazer é bater a senha, ou seja, ele vai ver se a senha que a pessoa digitou lá no formulário de login é
        // a mesma que está sendo retornada do usuario
        // E como é que eu posso criar um UserDetails de forma rápida? Eu tenho a classe User que tem um builder que me
        // ajuda a construir um UserDetails
        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenha())
                // Esse usuario tem uma lista de Strings só que eu preciso transformar em Array, pra isso eu chamo o
                // metodo toArray que retorna um Array a partir dos elementos dessa lista, e aqui dentro eu passo um
                // new String[] com a quantidade de elementos que tem
                .roles(usuario.getRoles().toArray(new String[usuario.getRoles().size()]))
                .build();
    }
}
