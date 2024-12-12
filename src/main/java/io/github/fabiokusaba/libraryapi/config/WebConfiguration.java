package io.github.fabiokusaba.libraryapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Estou dizendo que essa classe é uma classe de configuração e também estou habilitando o MVC dentro da minha aplicação
// ou seja, Model-View-Controller, então agora consigo utilizar páginas web dentro da minha aplicação
// Essa classe precisa implementar a classe WebMvcConfigurer e vou sobrescrever o metodo addViewControllers
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // E aqui vou registrar essa página web, então estou dizendo que quando acessar "/login" ele vai acessar a View
        // "login" que é a página que criamos em templates
        registry.addViewController("/login").setViewName("login");
        // Vou ordenar esse registro colocando essa View como a mais alta precedência na minha aplicação
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
