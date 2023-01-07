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

import static java.util.stream.Collectors.toList;



@RestController
@RequestMapping("/api")
public class GameController {

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


    @RequestMapping("/games")
    public Map<String, Object> getGameAll(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        if (Objects.isNull(authentication)) {
            dto.put("player", "Guest");
        } else if (Objects.nonNull(this.getPlayerAuth(authentication))) {
            dto.put("player", this.getPlayerAuth(authentication).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(toList()));
        return dto;
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "Usuario -no logueado"), HttpStatus.UNAUTHORIZED);
        }
        Game game = new Game(new Date());
        gameRepository.save(game);
        GamePlayer gameplayer = new GamePlayer(game, getPlayerAuth(authentication));
        gamePlayerRepository.save(gameplayer);
        return new ResponseEntity<>(Util.makeMap("gpid", gameplayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships,
                                                        Authentication authentication) {
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "User no logged"), HttpStatus.UNAUTHORIZED);
        }

        if (Objects.isNull(gamePlayer)) {
            return new ResponseEntity<>(Util.makeMap("error", "There is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        if (getPlayerAuth(authentication).getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(Util.makeMap("error", "The current user is not the game player the ID references"),
                    HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.getShips().stream().count() >= 5) {
            return new ResponseEntity<>(Util.makeMap("error", "Already has ships placed"),
                    HttpStatus.FORBIDDEN);
        }

        ships.stream().forEach(ship -> ship.setGamePlayer(gamePlayer));
        shipRepository.saveAll(ships);

        return new ResponseEntity<>(Util.makeMap("ships", "added ok"), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo,
                                                          Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "User no logged"), HttpStatus.UNAUTHORIZED);
        }

        if (Objects.isNull(gamePlayer)) {
            return new ResponseEntity<>(Util.makeMap("error", "There is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        if (getPlayerAuth(authentication).getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(Util.makeMap("error", "The current user is not the game player the ID references"),
                    HttpStatus.UNAUTHORIZED);
        }

        if(salvo.getSalvoLocations().size()!=5){
            return new ResponseEntity<>(Util.makeMap("error", "Wrong amount of salvos."),
                    HttpStatus.FORBIDDEN);
        }

        if (Objects.nonNull(gamePlayer.getSalvoes()
                .stream()
                .filter(salvo1 -> salvo1
                        .getTurn() == salvo.getTurn())
                .findFirst().orElse(null))) {
            return new ResponseEntity<>(Util.makeMap("error", "User already has submitted a salvo for the turn listed."),
                    HttpStatus.UNAUTHORIZED);
        }

        salvo.setTurn(gamePlayer.getSalvoes().size() + 1);
        salvo.setGamePlayer(gamePlayer);
        salvoRepository.save(salvo);
        return new ResponseEntity<>(Util.makeMap("salvo", "Added ok"), HttpStatus.CREATED);
    }

    private Player getPlayerAuth(Authentication authentication) {
        return playerRepository.findByUserName(authentication.getName());
    }
}
