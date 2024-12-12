package io.github.fabiokusaba.libraryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		
		//var context = SpringApplication.run(Application.class, args);
		//AutorRepository repository = context.getBean(AutorRepository.class);

		//exemploSalvarRegistro(repository);
	}

//	public static void exemploSalvarRegistro(AutorRepository autorRepository) {
//		Autor autor = new Autor();
//		autor.setNome("José");
//		autor.setNacionalidade("Brasileira");
//		autor.setDataNascimento(LocalDate.of(1950, 1, 31));
//
//		var autorSalvo = autorRepository.save(autor);
//		System.out.println("Autor Salvo: " + autorSalvo);
//	}
}
