package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository,
                                      SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return (args) -> {


			Player player1= new Player("test1@gmail.com",passwordEncoder().encode("test1"));
            Player player2= new Player("test2@gmail.com",passwordEncoder().encode("test2"));
            Player player3= new Player("test3@gmail.com",passwordEncoder().encode("test3"));
            Player player4= new Player("test4@gmail.com",passwordEncoder().encode("test4"));
            Player player5= new Player("test5@gmail.com",passwordEncoder().encode("test5"));


            repository.save(player1);
            repository.save(player2);
            repository.save(player3);
            repository.save(player4);
            repository.save(player5);

            Date dateGame1 = new Date();
            Game game1 =new Game(dateGame1);
            Game game2 =new Game(Date.from(dateGame1.toInstant().plusSeconds(3600)));
            Game game3 =new Game(Date.from(dateGame1.toInstant().plusSeconds(7200)));

			gameRepository.save(game1);
            gameRepository.save(game2);
            gameRepository.save(game3);

            GamePlayer gameplayer1 = new GamePlayer(game1,player1);
            GamePlayer gameplayer2 = new GamePlayer(game1,player2);
            GamePlayer gameplayer3 = new GamePlayer(game2,player3);
            GamePlayer gameplayer4 = new GamePlayer(game2,player4);
            GamePlayer gameplayer5 = new GamePlayer(game3,player5);

            gamePlayerRepository.save(gameplayer1);
            gamePlayerRepository.save(gameplayer2);
            gamePlayerRepository.save(gameplayer3);
            gamePlayerRepository.save(gameplayer4);
            gamePlayerRepository.save(gameplayer5);

            List<String> locationsS1= new ArrayList<>();
            locationsS1.add("H1");
            locationsS1.add("H2");
            locationsS1.add("H3");

            List<String> locationsS2= new ArrayList<>();
            locationsS2.add("B3");
            locationsS2.add("B4");
            locationsS2.add("B5");

            List<String> locationsS3= new ArrayList<>();
            locationsS3.add("A2");
            locationsS3.add("B2");
            locationsS3.add("C2");

            shipRepository.save(gameplayer1.addShip("bomber",locationsS1));
            shipRepository.save(gameplayer1.addShip("bomber",locationsS3));
            shipRepository.save(gameplayer2.addShip("Destroyer",locationsS2));
            shipRepository.save(gameplayer3.addShip("Destroyer",locationsS3));

            List<String> locationsSalvo1= new ArrayList<>();
            locationsSalvo1.add("H1");
            locationsSalvo1.add("H2");


            List<String> locationsSalvo2= new ArrayList<>();
            locationsSalvo2.add("A2");
            locationsSalvo2.add("B2");

            List<Salvo> salvoes = new ArrayList<>();

            salvoes.add(new Salvo(gameplayer1,locationsSalvo1,1));
            salvoes.add(new Salvo(gameplayer2,locationsSalvo2,1));

            salvoRepository.saveAll(salvoes);

            scoreRepository.save(new Score(game1,player1,1));
            scoreRepository.save(new Score(game1,player2,0));
            scoreRepository.save(new Score(game2,player3,1));


		};
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName-> {
            Player player = playerRepository.findByUserName(inputName);
            if (Objects.nonNull(player)) {
                return new User(player.getUserName(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/rest/**").hasAuthority("USER")
                .antMatchers("/web/**").permitAll()
                .antMatchers("/web/img/**").permitAll()
                .antMatchers("/api/**").permitAll()
                .anyRequest().permitAll();


            http.formLogin()
                .usernameParameter("name")
                .passwordParameter("pwd")
                .loginPage("/api/login");

            http.logout()
                .logoutUrl("/api/logout");
        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}


