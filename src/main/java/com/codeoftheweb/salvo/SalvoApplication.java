package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Ship;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import com.codeoftheweb.salvo.repository.ShipRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository) {
		return (args) -> {


			Player player1= new Player("test1@gmail.com");
            Player player2= new Player("test2@gmail.com");
            Player player3= new Player("test3@gmail.com");
            Player player4= new Player("test4@gmail.com");
            Player player5= new Player("test5@gmail.com");


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


		};




	}
}



