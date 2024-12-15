package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

// Classe customizada de Authentication onde vamos implementar a interface Authentication do Spring Security
// Agora toda a nossa aplicação independente do tipo de autenticação que vamos estar utilizando se é basic, form login,
// Google, no final a gente vai produzir uma instância de CustomAuthentication porque ela vai carregar o usuário que
// está logado
// Agora eu preciso dizer para o Spring que ele vai usar essa authentication, até o momento temos a CustomUserDetails
// Service que é a nossa fonte de usuários em que ele retorna UserDetails e o próprio Spring cria a authentication então
// não vamos mais usar esse metodo aqui, então a gente precisa criar um AuthenticationProvider, ou seja, um provedor de
// authentication e dizer para o Spring que ele tem que usar aquela authentication
@RequiredArgsConstructor // Cria um construtor para usuario
@Getter // Para quando quisermos, dentro dessa authentication, pegar o usuário logado
public class CustomAuthentication implements Authentication {

    // Quando a gente for criar essa authentication a gente vai passar pra ela o usuário que está autenticado
    private final Usuario usuario;

    // Dentro de Usuario a gente tem as roles, vamos pegar essas roles e transformar em authorities
    // Isso aqui é importante porque é através daqui que a gente vai autorizar o usuário
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Aqui temos uma lista de Strings que vamos mapear para uma lista de GrantedAuthority, interface que retorna
        // qual é a authority, por exemplo: operador, gerente, etc
        // SimpleGrantedAuthority é uma implementação simples de GrantedAuthority que recebe uma role e adiciona
        return this.usuario.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    // Aqui é a senha, mas não vamos retornar a senha desse usuário porque aqui ele já vai estar autenticado, ele não
    // vai validar a senha ainda
    @Override
    public Object getCredentials() {
        return null;
    }

    // Aqui é para obter os detalhes, então você pode retornar alguns detalhes do usuário, por exemplo: o departamento
    // que ele está lotado dentro da empresa, qual o CPF dele, aqui vamos retornar o usuário por si só
    @Override
    public Object getDetails() {
        return usuario;
    }

    // Aqui em principal é o próprio usuário
    @Override
    public Object getPrincipal() {
        return usuario;
    }

    // Aqui colocamos true porque se deixarmos como false nunca vamos conseguir logar
    @Override
    public boolean isAuthenticated() {
        return true;
    }

    // Aqui não precisamos fazer nada
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    // Aqui no name vamos colocar o login do usuário
    @Override
    public String getName() {
        return usuario.getLogin();
    }
}
