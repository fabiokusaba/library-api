package io.github.fabiokusaba.libraryapi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// Agora preciso dizer como ele vai acessar essa página e para isso vamos criar um controller e anotar com Controller
// Qual a diferença de Controller para RestController? O RestController é para API, para requisições REST onde vou
// tratar com JSON, vou mandar e receber objetos JSON. E o Controller é quando estou utilizando páginas web
// Quando utilizamos a anotação Controller ele espera que essa String que estou retornando seja uma página, ou seja, é
// como se eu estivesse retornango a página 'login.html'
// Então, como é que eu faço para dizer que eu quero retornar String? Utilizo a anotação ResponseBody, desta forma ele
// vai pegar o retorno e colocar no corpo da resposta e não vai esperar uma página
@Controller
public class LoginViewController {

    // Então, quando estamos dentro de um Controller que não é um RestController a gente retorna Strings para indicar
    // qual é a página que ele tem que ir quando ele chamar essa requisição, desta forma a gente já vai conseguir
    // acessar essa página de login
    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    @GetMapping("/")
    @ResponseBody
    public String paginaHome(Authentication authentication) {
        return "Olá " + authentication.getName();
    }
}
