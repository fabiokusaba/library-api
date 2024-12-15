package io.github.fabiokusaba.libraryapi.security;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Classe customizada que vamos implementar a AuthenticationProvider do pacote Spring Security
// Ele tem dois metodos que precisamos implementar: authenticate e supports
@Component // Anotamos com Component para que ela seja um Bean gerenciado
@RequiredArgsConstructor // Para injeção de dependências necessárias
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UsuarioService usuarioService; // Para verificar qual o login de usuário
    private final PasswordEncoder encoder; // Para verificação de senha

    // Esse metodo aqui é o que vai autenticar, então aqui no CustomUserDetailsService a gente só provê pra ele o user
    // que ele vai verificar se a senha está correta, então aqui a gente só carrega os dados do usuário pra ele depois
    // verificar se a senha está correta e criar a authentication
    // Aqui dentro do authenticate nós mesmos vamos bater a senha, então aqui a gente vai fazer o que o Spring faz por
    // de baixo dos panos e ao final temos que prover um objeto do tipo Authentication
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Obtendo o login através do objeto authentication
        String login = authentication.getName();
        // o getCredentials retorna um Object porque ele não necessariamente é uma senha digitada podendo ser uma:
        // digital, autenticação facial (Face ID), então ele pode retornar qualquer um desses tipos de autenticação por
        // isso utilizamos o toString para transformarmos em String e pegar a senha digitada
        String senhaDigitada = authentication.getCredentials().toString();

        // Agora que temos o login e a senha digitada vamos localizar esse usuário através do usuarioService
        Usuario usuarioEncontrado = usuarioService.obterPorLogin(login);

        // Vamos fazer verificações com esse usuário
        if (usuarioEncontrado == null) {
            // Nesse caso aqui ele não achou o usuário, digitou o login errado
            // Importante! Não devemos dizer se é o usuário ou a senha que estão incorretos, sempre devemos colocar uma
            // mensagem padrão como essa para dificultar invasões
            throw getErroUsuarioNaoEncontrado();
        }

        // Agora eu localizei o usuário então eu preciso pegar a senha que está criptografada, ou seja, a senha desse
        // usuário que está salva no banco de dados
        String senhaCriptografada = usuarioEncontrado.getSenha();

        // Agora vou utilizar o encoder para verificar se essa senhaDigitada bate com a senhaCriptografada
        // O metodo matches recebe dois parâmetros: o primeiro é o rawPassword, ou seja, a senha digitada crua sem estar
        // criptografada, o segundo parâmetro é a senha codificada encodedPassword, se atente a esse detalhe porque se
        // você passar o contrário não vai funcionar
        boolean senhaValida = encoder.matches(senhaDigitada, senhaCriptografada);

        // Aqui vou fazer a verificação se as senhas baterem, se sim vou precisar prover esse objeto Authentication
        if (senhaValida) {
            // Aqui estou provendo a minha própria authentication pro contexto do Spring e agora sim estou customizando
            // de fato a minha authentication
            return new CustomAuthentication(usuarioEncontrado);
        }

        // Se falhar, vou lançar uma exceção
        throw getErroUsuarioNaoEncontrado();
    }

    private static UsernameNotFoundException getErroUsuarioNaoEncontrado() {
        return new UsernameNotFoundException("Usuário e/ou senha incorretos!");
    }

    // Aqui ele vai dizer quais tipos de authentication que suporta desse authentication provider
    // Temos um tipo específico para quando o usuário digita login e senha que é o isAssignableFrom
    // Esse metodo isAssignableFrom vai pegar a classe de authentication que foi passada pra cá, o Spring Security vai
    // verificar o login e a senha, ele vai criar um objeto a partir daquele login e senha, então ele vai passar esse
    // objeto para o provider perguntando se ele suporta esse tipo de autenticação e aqui vamos dizer qual é a classe
    // que ele aceita
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
