package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvoes;


    public Map<String, Object> makeGamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDTO());
        return dto;
    }

    public List<Object> getShipDTO() {
        return this.getShips()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(toList());
    }


    public Ship addShip(String shipType, List<String> locations) {
        Ship ship = new Ship(shipType, locations, this);
        return ship;
    }

    public Score getScore() {
        return this.player.getScore(this.game);
    }

    public GamePlayer() {
        this.joinDate = new Date();
    }

    public GamePlayer(Game game, Player player) {
        this.game = game;
        this.player = player;
        this.joinDate = new Date();
    }


    public Ship getShipByType(String type) {
        return this.getShips()
                .stream()
                .filter(ship -> ship.getType().equals(type))
                .findFirst().orElse(new Ship());
    }


    public List<String> salvoHitLocations(Salvo salvo) {
        return this.getShips().stream()
                .flatMap(ship -> ship.getShipLocations()
                        .stream().flatMap(shipLocations -> salvo.getSalvoLocations()
                                .stream().filter(salvoLoc -> shipLocations.contains(salvoLoc))))
                .collect(Collectors.toList());
    }

    public Long salvoMissed(List<String> salvoHited) {
        long salvosLocationsPerTurn = 5;
        return salvosLocationsPerTurn - salvoHited.size();
    }


    public Map<String, Object> makeHitDTO(Salvo salvo, List<Salvo> oppSalvo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("hitLocations", this.salvoHitLocations(salvo));
        dto.put("damages", Stream.concat(this.DTOHitsbyTurn(salvo).entrySet().stream(),
                this.getHitsDTO(oppSalvo, salvo).entrySet().stream())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        dto.put("missed", salvoMissed(this.salvoHitLocations(salvo)));
        return dto;
    }

    public Map<String, Object> DTOHitsbyTurn(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("carrierHits", salvo.getHits(getShipByType("carrier")));
        dto.put("battleshipHits", salvo.getHits(getShipByType("battleship")));
        dto.put("submarineHits", salvo.getHits(getShipByType("submarine")));
        dto.put("destroyerHits", salvo.getHits(getShipByType("destroyer")));
        dto.put("patrolboatHits", salvo.getHits(getShipByType("patrolboat")));
        return dto;
    }

    public Map<String, Object> getHitsDTO(List<Salvo> oppSalvo, Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("carrier", oppSalvo.stream().filter(salvo1 -> salvo1.getTurn() <= salvo.getTurn()).map(salvoOp -> salvoOp.getHits(getShipByType("carrier"))).reduce(Long::sum).get());
        dto.put("battleship", oppSalvo.stream().filter(salvo1 -> salvo1.getTurn() <= salvo.getTurn()).map(salvoOp -> salvoOp.getHits(getShipByType("battleship"))).reduce(Long::sum).get());
        dto.put("submarine", oppSalvo.stream().filter(salvo1 -> salvo1.getTurn() <= salvo.getTurn()).map(salvoOp -> salvoOp.getHits(getShipByType("submarine"))).reduce(Long::sum).get());
        dto.put("destroyer", oppSalvo.stream().filter(salvo1 -> salvo1.getTurn() <= salvo.getTurn()).map(salvoOp -> salvoOp.getHits(getShipByType("destroyer"))).reduce(Long::sum).get());
        dto.put("patrolboat", oppSalvo.stream().filter(salvo1 -> salvo1.getTurn() <= salvo.getTurn()).map(salvoOp -> salvoOp.getHits(getShipByType("patrolboat"))).reduce(Long::sum).get());
        return dto;
    }


    public GamePlayer getOpponent() {
        return this.getGame()
                .gamePlayers
                .stream()
                .filter(gamePlayer -> gamePlayer.getId() != this.getId())
                .findFirst().orElse(null);
    }

    public long getTurn(List<Salvo> oppSalvo) {

        return oppSalvo.size();

    }

    public long getId() {
        return id;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }


}
