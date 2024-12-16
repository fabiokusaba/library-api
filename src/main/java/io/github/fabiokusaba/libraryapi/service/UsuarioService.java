package io.github.fabiokusaba.libraryapi.service;

import io.github.fabiokusaba.libraryapi.model.Usuario;
import io.github.fabiokusaba.libraryapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    // Vamos injetar aqui também o PasswordEncoder porque como sabemos o service tem a lógica de negócio e antes de
    // salvar um usuário eu tenho que criptografar a senha dele
    private final PasswordEncoder encoder;

    // API para cadastrar usuário
    public void salvar(Usuario usuario) {
        // Pegando a senha do usuário que vai ser cadastrado
        var senha = usuario.getSenha();
        // Vou modificar a senha desse usuário pra senha criptografada chamando o metodo encode
        usuario.setSenha(encoder.encode(senha));
        // Depois que criptografei a senha do usuário vou chamar o repository e o metodo save
        repository.save(usuario);
    }

    // Como vimos lá no UserDetailsService, interface que a gente precisa implementar pra poder se conectar com o banco,
    // ele tem um metodo loadUserByUsername, então eu vou implementar essa interface e vou injetar o repository pra
    // gente poder utilizar esse metodo que estamos criando pra trazer os dados desse usuário
    public Usuario obterPorLogin(String login) {
        return repository.findByLogin(login);
    }

    public Usuario obterPorEmail(String email) {
        return repository.findByEmail(email);
    }
}
