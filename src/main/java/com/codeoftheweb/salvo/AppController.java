package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class AppController {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private PlayerRepository playerRepository;


    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> getGamePlayerDTO(@PathVariable Long gamePlayerId, Authentication authentication) {
        GamePlayer gameplayer = gamePlayerRepository.findById(gamePlayerId).get();
        Game game = gameplayer.getGame();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if (playerRepository.findByUserName(authentication.getName()).getId() != gameplayer.getPlayer().getId()) {
            return new ResponseEntity<>(Util.makeMap("error", "Usuario no asignado a gameplayer"), HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> dto = game.makeGameDTO();
        dto.put("gameState", this.getState(gameplayer, gameplayer.getOpponent()));
        dto.put("ships", gameplayer.getShipDTO());
        dto.put("salvoes", game.getGamePlayers()
                .stream()
                .flatMap(gameplayer1 -> gameplayer1.getSalvoes()
                        .stream()
                        .map(salvo -> salvo.getSalvoDTO()))
                .collect(toList()));
        dto.put("hits", salvoHit(gameplayer));

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {
        Game joinGame = gameRepository.findById(gameId).get();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if (Objects.isNull(joinGame)) {
            return new ResponseEntity<>(Util.makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }

        if (joinGame.getGamePlayers().size() == 2) {
            return new ResponseEntity<>(Util.makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }
        GamePlayer gamePlayerJoin = new GamePlayer(joinGame, getPlayerAuth(authentication));
        gamePlayerRepository.save(gamePlayerJoin);
        return new ResponseEntity<>(Util.makeMap("gpid", gamePlayerJoin.getId()), HttpStatus.OK);
    }


    private Player getPlayerAuth(Authentication authentication) {
        return playerRepository.findByUserName(authentication.getName());
    }



    private String getState(GamePlayer gamePlayerSelf, GamePlayer gamePlayerOpp) {
        if (gamePlayerSelf.getShips().isEmpty()) {
            return "PLACESHIPS";
        }
        if (gamePlayerSelf.getGame().getGamePlayers().size() == 1) {
            return "WAITINGFOROPP";
        }

        if (gamePlayerSelf.getId() < gamePlayerOpp.getId()) {
            return "PLAY";
        }


        if (gamePlayerSelf.getId() > gamePlayerOpp.getId()) {
            return "WAIT";
        }
        return "LOST";
    }


    private Map<String, Object> salvoHit(GamePlayer gameplayer) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if (Objects.nonNull(gameplayer.getOpponent())) {
            dto.put("self", gameplayer.getOpponent()
                    .getSalvoes()
                    .stream()
                    .map(salvo -> gameplayer.makeHitDTO(salvo, new ArrayList<Salvo>(gameplayer
                            .getOpponent()
                            .getSalvoes())))
                    .collect(Collectors.toList()));

            dto.put("opponent", gameplayer
                    .getSalvoes()
                    .stream()
                    .map(salvo -> gameplayer.makeHitDTO(salvo, new ArrayList<Salvo>(gameplayer
                            .getSalvoes())))
                    .collect(Collectors.toList()));
        }else{
            dto.put("self",new ArrayList<>());
            dto.put("opponent",new ArrayList<>());
        }
        return dto;
    }
}


