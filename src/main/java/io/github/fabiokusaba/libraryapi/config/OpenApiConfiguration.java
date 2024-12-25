package io.github.fabiokusaba.libraryapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

// Customizando a documentação da nossa API através de uma classe de configuração
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Library API",
                version = "v1",
                contact = @Contact(
                        name = "Fabio Kusaba",
                        email = "fabinhokb44@gmail.com",
                        url = "library-api.com"
                )
        )
)
public class OpenApiConfiguration {


}
