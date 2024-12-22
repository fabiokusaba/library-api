package io.github.fabiokusaba.libraryapi.config;

import io.github.fabiokusaba.libraryapi.security.CustomUserDetailsService;
import io.github.fabiokusaba.libraryapi.security.LoginSocialSuccessHandler;
import io.github.fabiokusaba.libraryapi.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// Classe de configurações de segurança da aplicação, por ser uma classe de configuração anotamos com Configuration e
// quando é uma configuração de segurança precisamos de mais uma annotation EnableWebSecurity
// Em versões mais antigas do Spring era necessário extender uma classe, mas nas versões mais atuais não é mais preciso
// o que precisamos fazer é declarar um Bean do tipo SecurityFilterChain e a gente vai injetar um objeto HttpSecurity e
// vou lançar uma exceção com throws Exception porque essa configuração do Spring Security ela pode lançar uma Exception
// Esse objeto HttpSecurity é injetado de dentro do contexto de segurança do Spring, esse objeto faz parte do contexto
// do Spring Security então não precisamos configurar ele em nenhum lugar pois ele já vem pré-configurado pra gente
// A gente vai utilizar ele pra habilitar o que a gente precisar e pra configurar o que a gente precisar aqui dentro de
// segurança
// Esse objeto http pra eu criar um SecurityFilterChain a partir dele eu preciso chamar o metodo build() dele que ele
// vai retornar pra mim um SecurityFilterChain, e aqui antes de chamar o build() eu tenho várias configurações que eu
// posso estar chamando aqui
// A partir do momento que eu declaro o meu SecurityFilterChain aqui ele vai sobrescrever o que estava por padrão, ou
// seja, aquele que habilitou o formulário de login e aquele que habilitou a autenticação Basic então ele vai passar a
// atender as configurações que eu passar aqui
// Aqui vamos habilitar uma annotation que é o EnableMethodSecurity e aqui eu preciso passar duas propriedades para
// habilitar com que a gente faça isso lá nos controllers, a primeira propriedade é o securedEnabled e o jsr250Enabled
// com isso daqui a gente habilita para que a gente consiga fazer isso nos controllers
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginSocialSuccessHandler successHandler) throws Exception {
        // Habilitando as configurações padrão
        return http
                // Configuração que utilizamos quando estavamos trabalhando com aplicações web, proteção de páginas web
                // ou seja, para que a aplicação consiga fazer as requisições de forma autenticada ela tem que enviar um
                // token csrf para o backend e ele vai garantir que aquela página que enviou a requisição foi a página
                // dessa própria aplicação isso vai impedir que outras páginas de outros sistemas consigam fazer uma
                // requisição pra essa aplicação, então vou desabilitar isso aqui porque eu quero permitir que outras
                // aplicações consigam fazer uma requisição
                .csrf(AbstractHttpConfigurer::disable)
                // Vou habilitar o formulário padrão do Spring Security
                // Para demonstrar o login pelo Google vamos utilizar o default do formLogin porque ele vai adicionar
                // automaticamente o botão para logarmos pelo Google
                //.formLogin(Customizer.withDefaults())
                // Customizando o formulário de login
                .formLogin(configurer -> {
                    // O permitAll vai fazer com que todos consigam acessar a página de login, ou seja, ela não vai
                    // estar protegida com autenticação
                    configurer.loginPage("/login");
                })
                // Configurando o httpBasic
                .httpBasic(Customizer.withDefaults())
                // Por último vou autorizar requisições http, ou seja, estabelecer as regras de acesso
                .authorizeHttpRequests(authorize -> {
                    // Permitindo que tanto login quanto o cadastro de usuário sejam abertos
                    authorize.requestMatchers("/login/**").permitAll();
                    authorize.requestMatchers(HttpMethod.POST,"/usuarios/**").permitAll();
                    // Aqui dentro fazemos o controle de requisições pela role ou pelas authorities do usuário
                    // Então, aqui estou controlando as requisições que chegam para "/autores" e utilizando "/**" quer
                    // dizer que não me importo com o quem vem depois, ou seja, id do autor, parâmetros de pesquisa, e
                    // passando o hasRole como "ADMIN" estou dizendo que somente usuários que tenham essa role podem
                    // fazer operações
                    //authorize.requestMatchers("/autores/**").hasRole("ADMIN");
                    // Vamos imaginar que para salvar um Autor eu tenho que ser administrador (ADMIN) e como eu me digo
                    // que é só no cadastro? Aqui eu coloco o HttpMethod e digo qual metodo que ele vai fazer a request
                    // e aqui posso ir incrementando e criando essas regras com os demais verbos Http DELETE, PUT,...
                    //authorize.requestMatchers(HttpMethod.POST, "/autores/**").hasRole("ADMIN");
                    //authorize.requestMatchers(HttpMethod.DELETE, "/autores/**").hasRole("ADMIN");
                    //authorize.requestMatchers(HttpMethod.PUT, "/autores/**").hasRole("ADMIN");
                    //authorize.requestMatchers(HttpMethod.GET, "/autores/**").hasAnyRole("ADMIN", "USER");
                    // Com o hasAnyRole posso passar mais de uma role para permitir que usuários de distintas roles
                    // realizem operações
                    //authorize.requestMatchers("/livros/**").hasAnyRole("USER", "ADMIN");
                    // Estou dizendo que qualquer requisição que eu fizer pra essa API ele vai ter que estar autenticado
                    // E o mais importante esse anyRequest vai anular qualquer coisa que vir abaixo dele, então qualquer
                    // outra regra que eu colocar aqui abaixo ele não vai atender, então o anyRequest tem que ficar por
                    // último nessas declarações
                    authorize.anyRequest().authenticated();
                })
                // Adicionando o OAuth2Login
                .oauth2Login(oauth2 -> {
                    // Configurando manualmente
                    // Quando ele fizer autenticação com sucesso ele vai chamar essa classe que vou passar aqui, então
                    // ele precisa de uma instância de AuthenticationSuccessHandler, basicamente ele vai se autenticar
                    // via Google ou via OAuth2 e depois que ele logar com sucesso ele vai chamar essa classe passando
                    // essa authentication, então a gente vai receber nessa classe authentication e vai transformar na
                    // nossa CustomAuthentication, vamos criar a implementação dessa classe para fazer essa lógica
                    // Para passarmos a nossa classe LoginSocialSuccessHandler para cá precisamos injetá-la no security
                    // FilterChain
                    oauth2
                            // Dizendo ao oauth2 que ele vai utilizar o mesmo formulário, ou seja, tanto oauth2 quanto
                            // formLogin vão apontar para a mesma página
                            .loginPage("/login")
                            .successHandler(successHandler);
                })
                .build();
    }

    // Declarando um Bean que vai ser uma instância de PasswordEncoder, essa interface possui metodos para criptografar
    // uma senha e um metodo que retorna um booleano que vai pegar uma senha digitada e vai verificar se ela bate com
    // a senha criptografada
    // Como declarei o PasswordEncoder o Spring Boot na hora que eu digitar o login e a senha ele vai cair aqui dentro
    // desse UserDetailsService e vai buscar pelo usuário
    // A gente sempre utiliza a interface porque a gente tem apenas uma implementação aqui no projeto, só existe um
    // PasswordEncoder registrado aqui no nosso container que é esse aqui do BCrypt e se algum dia eu quiser mudar a
    // forma de criptografia basta mudar aqui e o resto do sistema vai continuar funcionando porque estamos utilizando
    // a interface
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        // Uma vez que a gente codifica essa senha, a gente criptografa ela em BCrypt, não é possível ela voltar atrás
//        // ou seja, se você pegar o hash BCrypt você não consegue decodificar ele pra descobrir o que ele significa
//        // Ele consegue através de, por exemplo, comparar esse número "123" com o hash gerado por esse número se é
//        // compatível, se foi gerado aquele hash a partir daquela senha digitada, então é assim que ele diz se bate ou
//        // não as senhas, ele não criptografa e compara os dois ele apenas verifica se aquela String foi utilizada para
//        // gerar aquele hash
//        // E aqui você pode botar uma força para dizer quantas vezes ele vai passar em cima daquele BCrypt
//        return new BCryptPasswordEncoder(10);
//    }

    // Agora a gente conseguiu conectar a nossa base de usuários, nossa tabela de usuarios, através de UsuarioService,
    // UsuarioRepository com a UserDetails que vai retornar uma instância de UserDetails já com username, password e com
    // as roles para o Spring saber quais são as permissões daquele usuário
    // O UserDetailsService é o que atualmente provê a authentication através do CustomUserDetailsService, entao eu vou
    // desabilitar ele comentando a anotação Bean para que ele não seja mais registrado e agora o que o Spring vai
    // registrar vai ser o nosso provider customizado
    //@Bean
//    public UserDetailsService userDetailsService(PasswordEncoder encoder, UsuarioService usuarioService) {
//        // Esse CustomUserDetailsService precisa do UsuarioService pra funcionar, então vamos vir aqui e injetar o
//        // UsuarioService
//        return new CustomUserDetailsService(usuarioService);
//
//        // Criando usuários em memória
//        // Essa classe User é um builder para construir UserDetails
//        //UserDetails user1 = User.builder()
//                //.username("usuario")
//                // Aqui na senha a gente tem um detalhe bem importante que é: o Spring Security precisa saber como ele
//                // vai comparar a senha digitada com essa senha que está salva em memória ou no banco de dados
//                // O Spring Security obriga a gente a disponibilizar um codificador e decodificador de senhas para que
//                // ele consiga comparar as senhas e também para que a nossa aplicação fique mais segura porque não faz
//                // sentido salvar a senha hard coded, ou seja, do jeito que o usuário digitou a senha eu salvar no banco
//                // Eu tenho que criptografar aquela senha para que ninguém consiga acessar do contrário não faz sentido
//                // eu ter essa segurança na aplicação
//                // Como já declarei o PasswordEncoder basta eu injetar aqui passando como parâmetro e agora basta eu
//                // chamar o metodo encode passando a senha que quero criptografar
//                //.password(encoder.encode("123"))
//                // Geralmente as roles colocamos em caixa alta
//                //.roles("USER")
//                //.build();
//
//        //UserDetails user2 = User.builder()
//                //.username("admin")
//                //.password(encoder.encode("321"))
//                //.roles("ADMIN")
//                //.build();
//
//        //return new InMemoryUserDetailsManager(user1, user2);
//    }

    // A configuração é bem simples basta retornarmos a instância dele e no construtor a gente passa qual prefixo que
    // a gente quer, passando nada ele vai simplesmente ignorar esse prefixo ROLE ou podemos customizar com um prefixo
    // de nossa escolha
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
