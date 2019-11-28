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
    private ShipRepository  shipRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;


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
            return new ResponseEntity<>(makeMap("error", "Usuario -no logueado"), HttpStatus.UNAUTHORIZED);
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

        Map<String,Object> auxdto= new LinkedHashMap<>();
        Map<String, Object> dto = game.makeGameDTO();
        dto.put("gameState", this.getState(gameplayer,gameplayer.getOpponent()));
        List<Object> auxlist = new ArrayList<>();
        dto.put("ships", gameplayer.getShipDTO());
        dto.put("salvoes", game.getGamePlayers()
                .stream()
                .flatMap(gameplayer1 -> gameplayer1.getSalvoes()
                        .stream()
                        .map(salvo -> salvo.getSalvoDTO()))
                .collect(toList()));
        auxdto.put("self", auxlist);
        auxdto.put("opponent",auxlist);
        dto.put("hits",auxdto);
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

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> addShips(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships,
                                                         Authentication authentication){
        GamePlayer gamePlayer=gamePlayerRepository.findById(gamePlayerId).get();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(makeMap("error", "User no logged"), HttpStatus.UNAUTHORIZED);
        }

        if(Objects.isNull(gamePlayer)){
            return new ResponseEntity<>(makeMap("error", "There is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        if(getPlayerAuth(authentication).getId()!=gamePlayer.getPlayer().getId()){
            return new ResponseEntity<>(makeMap("error","The current user is not the game player the ID references"),
                    HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer.getShips().stream().count()>=5){
            return new ResponseEntity<>(makeMap("error","Already has ships placed"),
                    HttpStatus.FORBIDDEN);
        }

        ships.stream().forEach(ship -> ship.setGamePlayer(gamePlayer));
        shipRepository.saveAll(ships);

        return  new ResponseEntity<>(makeMap("ships","added ok"),HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> addSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo,
                                                         Authentication authentication){

        GamePlayer gamePlayer=gamePlayerRepository.findById(gamePlayerId).get();

        if (Objects.isNull(authentication)) {
            return new ResponseEntity<>(makeMap("error", "User no logged"), HttpStatus.UNAUTHORIZED);
        }

        if(Objects.isNull(gamePlayer)){
            return new ResponseEntity<>(makeMap("error", "There is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        if(getPlayerAuth(authentication).getId()!=gamePlayer.getPlayer().getId()){
            return new ResponseEntity<>(makeMap("error","The current user is not the game player the ID references"),
                    HttpStatus.UNAUTHORIZED);
        }

        if(Objects.nonNull(gamePlayer.getSalvoes()
                .stream()
                .filter(salvo1 -> salvo1
                        .getTurn()==salvo.getTurn())
                .findFirst().orElse(null))){
            return new ResponseEntity<>(makeMap("error","User already has submitted a salvo for the turn listed"),
                    HttpStatus.UNAUTHORIZED);
        }

        salvo.setTurn(gamePlayer.getSalvoes().size()+1);
        salvo.setGamePlayer(gamePlayer);
        salvoRepository.save(salvo);
        return  new ResponseEntity<>(makeMap("salvo","Added ok"),HttpStatus.CREATED);
    }



    private Player getPlayerAuth(Authentication authentication){
        return playerRepository.findByUserName(authentication.getName());
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private String getState(GamePlayer gamePlayerSelf, GamePlayer gamePlayerOpp){
        if(gamePlayerSelf.getShips().isEmpty()){
            return "PLACESHIPS";
        }
        if(gamePlayerSelf.getGame().getGamePlayers().size()==1){
            return "WAITINGFOROPP";
        }

        if(gamePlayerSelf.getId()< gamePlayerOpp.getId()){
            return "PLAY";
        }


        if(gamePlayerSelf.getId()> gamePlayerOpp.getId()){
            return "WAIT";
        }

        return "LOST";
    }

/*    private List<Object> salvoHit(GamePlayer gameplayer1,GamePlayer gameplayer2){
        Map<String,Object> dto = new LinkedHashMap<>();

        List<String> locationsHit= gameplayer1.getSalvoes().stream().flatMap(
                salvo -> gameplayer2
                        .getShips()
                        .stream()
                        .flatMap(ship -> { return ship
                                .getShipLocations()
                                .stream()
                                .flatMap(s -> salvo.getShipLocations()
                                        .stream()
                                        .filter(s1 -> s1.equals(s)));}
                                )).collect(Collectors.toList());




         return gameplayer2
                .getShips()
                .stream()
                .anyMatch(ship -> Objects.nonNull(ship.shipIsHit(locationsHit)));
    }*/
}


