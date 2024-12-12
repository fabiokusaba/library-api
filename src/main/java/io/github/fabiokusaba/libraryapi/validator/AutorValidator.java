package io.github.fabiokusaba.libraryapi.validator;

import io.github.fabiokusaba.libraryapi.exceptions.RegistroDuplicadoException;
import io.github.fabiokusaba.libraryapi.model.Autor;
import io.github.fabiokusaba.libraryapi.repository.AutorRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Aqui vai ser a nossa classe responsável pela lógica de validação do nosso Autor, vamos anotá-la com Component para
// que ela possa ser gerenciada pelo Spring e para que eu possa conseguir injetá-la quando for preciso
@Component
public class AutorValidator {

    // Injetando o nosso repository porque vou precisar ir no banco de dados para verificar se já está cadastrado o
    // autor
    private AutorRepository repository;

    public AutorValidator(AutorRepository repository) {
        this.repository = repository;
    }

    // Agora vamos criar um metodo validar que vai receber o autor que queremos validar e aqui dentro ele vai ter a
    // lógica de validação
    public void validar(Autor autor) {
        // A gente já sabe que eu vou receber um autor que vou cadastrar ou atualizar, vou pegar os dados dele como nome
        // data nascimento e nacionalidade e antes de efetuar o cadastro vou verificar, ou seja, vou ir lá no repository
        // vou criar um metodo para ele buscar no banco se existe um autor cadastrado com essas informações, se existir
        // lanço o RegistroDuplicadoException
        if (existeAutorCadastrado(autor)) {
            throw new RegistroDuplicadoException("Autor já cadastrado!");
        }
    }

    // Metodo auxiliar que vai verificar se existe um autor cadastrado no banco
    private boolean existeAutorCadastrado(Autor autor) {
        // Fui ao banco e busquei um possível autor que pode existir ou não com essas três informações
        Optional<Autor> autorEncontrado = repository.findByNomeAndDataNascimentoAndNacionalidade(
                autor.getNome(), autor.getDataNascimento(), autor.getNacionalidade()
        );

        // Lógica para quando estou cadastrando esse autor pela primeira vez
        // Se o id desse autor for nulo, isso quer dizer que eu vou cadastrar esse autor agora, ou seja, não estou
        // fazendo uma atualização de autor, estou fazendo um cadastro, é um novo autor
        // Se estou cadastrando, ou seja, basta verificar se existe um autor com esses dados, o autor que estou
        // validando não tem id eu vou cadastrar ele, então se já existir um autor lá no banco, ou seja, isPresent ele
        // vai retornar true indicando que existe um autor cadastrado e vai lançar o erro
        if (autor.getId() == null) {
            return autorEncontrado.isPresent();
        }

        // Lógica para quando estou atualizando
        // Porque vamos supor que eu estou atualizando esse autor e com certeza ele vai estar salvo lá no banco com
        // esses três campos, então esse autor que eu vou estar atualizando eu já atualizei os dados dele aqui na API
        // só que lá no banco ele ainda está com os dados antigos, então eu tenho que verificar se o autor que eu
        // encontrei não é o mesmo autor que estou tentando atualizar
        // Ou seja, se existe um autor cadastrado é quando o autor não possui o mesmo id, tenho que retornar true quando
        // o autor não tem o mesmo id do autor que estou atualizando e autorEncontrado está presente
        return !autor.getId().equals(autorEncontrado.get().getId()) && autorEncontrado.isPresent();
    }
}
