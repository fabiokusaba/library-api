package io.github.fabiokusaba.libraryapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Agora preciso dizer como ele vai acessar essa página e para isso vamos criar um controller e anotar com Controller
// Qual a diferença de Controller para RestController? O RestController é para API, para requisições REST onde vou
// tratar com JSON, vou mandar e receber objetos JSON. E o Controller é quando estou utilizando páginas web
@Controller
public class LoginViewController {

    // Então, quando estamos dentro de um Controller que não é um RestController a gente retorna Strings para indicar
    // qual é a página que ele tem que ir quando ele chamar essa requisição, desta forma a gente já vai conseguir
    // acessar essa página de login
    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }
}
