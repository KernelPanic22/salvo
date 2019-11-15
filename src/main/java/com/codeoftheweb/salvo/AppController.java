package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/games")
    public List<Object> getGameAll() {
        return gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(toList());
        }

    @RequestMapping("/game_view/{gamePlayerId}")
    public Map<String,Object> getGamePlayerDTO(@PathVariable Long gamePlayerId){
        GamePlayer gameplayer = gamePlayerRepository.findById(gamePlayerId).get();
        Game game = gameplayer.getGame();

        Map<String,Object> dto = game.makeGameDTO();
        dto.put("ships",gameplayer.getShipDTO());
        dto.put("salvoes", game.getGamePlayers()
                            .stream()
                            .flatMap(gameplayer1 -> gameplayer1.getSalvoes()
                                                    .stream()
                                                    .map(salvo -> salvo.getSalvoDTO()))
                            .collect(toList()));
        return dto;
        }
    }


