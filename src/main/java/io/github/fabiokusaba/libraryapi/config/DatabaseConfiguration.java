package io.github.fabiokusaba.libraryapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

// Classe para configurar um DataSource e um Pool de conexões
// Já sabemos que quando vamos definir alguns Beans dentro de uma classe eu preciso colocar a anotação Configuration
@Configuration
public class DatabaseConfiguration {

    @Value("${spring.datasource.url}")
    String url;
    @Value("${spring.datasource.username}")
    String username;
    @Value("${spring.datasource.password}")
    String password;
    @Value("${spring.datasource.driver-class-name}")
    String driver;

    // O DataSource nada mais é do que um Bean
    //@Bean
    public DataSource dataSource() {
        // Prover uma implementação de DataSource
        // Primeiramente preciso ler as propriedades de data source que estão no meu arquivo application.yml, já vimos
        // que conseguimos ler essas propriedades através do Value
        // O DataSource mais básico do Spring Boot é o DriverManagerDataSource, ele é uma implementação de DataSource
        DriverManagerDataSource ds = new DriverManagerDataSource();

        // Configurando as propriedades no DataSource
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driver);

        // Retornando o DataSource
        return ds;

        // Então, aqui eu já criei o meu DataSource, então basicamente a partir de agora eu também consigo me conectar
        // criar JpaRepository e fazer as operações na base de dados, porém esse é um DataSource simples, uma
        // implementação básica provida pelo Spring Data JPA e não é recomendável utilizá-la em produção
    }

    // O DataSource padrão que a gente utiliza com Spring Boot é o HikariDataSource, e ele permite que a gente crie um
    // Pool de conexões, ou seja, quando eu tiver muitos usuários a aplicação pode liberar mais conexões fazendo um
    // gerenciamento delas para atender a todos os usuários
    @Bean
    public DataSource hikariDataSource() {
        // Primeiramente vou instanciar um HikariConfig, um detalhe importante é que o Hikari já vem por padrão com o
        // Spring Data JPA, então você não precisa adicionar essa dependência
        HikariConfig config = new HikariConfig();

        // Passando as configurações para o Hikari
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);

        // Configurando o Pool de conexões
        config.setMaximumPoolSize(10); // máximo de conexões liberadas
        config.setMinimumIdle(1); // tamanho inicial do pool
        config.setPoolName("library-db-pool");
        config.setMaxLifetime(600000); // 600 mil ms -> 10 min (duração máxima da conexão)
        config.setConnectionTimeout(100000); // tempo gasto para tentar obter uma conexão
        config.setConnectionTestQuery("select 1"); // query simples para verificar se o banco está funcionando

        // Retornando um HikariDataSource passando a nossa configuração
        return new HikariDataSource(config);
    }
}
