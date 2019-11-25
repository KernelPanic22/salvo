package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.repository.PlayerRepository;
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
public class AppController {


   @Autowired
   PasswordEncoder passwordEncoder;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;


    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if ( email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(email) !=  null) {
            return new ResponseEntity<>("Name in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping("/games")
    public Map<String,Object> getGameAll(Authentication authentication) {
       Map<String,Object> dto = new LinkedHashMap<>();
        if(Objects.isNull(authentication)){
            dto.put("player","Guest");
        }else if(Objects.nonNull(this.getPlayerAuth(authentication))){
        dto.put("player",this.getPlayerAuth(authentication).makePlayerDTO());
       }
        dto.put("games",gameRepository.findAll()
               .stream()
               .map(game -> game.makeGameDTO())
               .collect(toList()));
        return dto;
        }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> createGame(Authentication authentication){
        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        Game game = new Game(new Date());
        gameRepository.save(game);
        GamePlayer gameplayer = new GamePlayer(game,getPlayerAuth(authentication));
        gamePlayerRepository.save(gameplayer);
        return new ResponseEntity<>(makeMap("gpid",gameplayer.getId()),HttpStatus.CREATED);
    }

    @RequestMapping("/leaderboard")
    public List<Object> getLeaderboardDTO(){
        List<Object> dto = new LinkedList<>();
                dto.add(gamePlayerRepository
                        .findAll()
                        .stream()
                        .map(gamePlayer ->   gamePlayer.getPlayer().getLeaderboardDTO())
                        .collect(toList()));
        return dto;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> getGamePlayerDTO(@PathVariable Long gamePlayerId,Authentication authentication) {
        GamePlayer gameplayer = gamePlayerRepository.findById(gamePlayerId).get();
        Game game = gameplayer.getGame();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if (playerRepository.findByUserName(authentication.getName()).getId() != gameplayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "Usuario no asignado a gameplayer"), HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> dto = game.makeGameDTO();
        dto.put("ships", gameplayer.getShipDTO());
        dto.put("salvoes", game.getGamePlayers()
                .stream()
                .flatMap(gameplayer1 -> gameplayer1.getSalvoes()
                        .stream()
                        .map(salvo -> salvo.getSalvoDTO()))
                .collect(toList()));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> joinGame(@PathVariable Long gameId, Authentication authentication){
        Game joinGame= gameRepository.findById(gameId).get();
        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        if(Objects.isNull(joinGame)){
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }

        if(joinGame.getGamePlayers().size()==2){
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }
        GamePlayer gamePlayerJoin = new GamePlayer(joinGame,getPlayerAuth(authentication));
        gamePlayerRepository.save(gamePlayerJoin);
        return  new ResponseEntity<>(makeMap("gpid",gamePlayerJoin.getId()),HttpStatus.OK);
    }

    private Player getPlayerAuth(Authentication authentication){
        return playerRepository.findByUserName(authentication.getName());
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

}


