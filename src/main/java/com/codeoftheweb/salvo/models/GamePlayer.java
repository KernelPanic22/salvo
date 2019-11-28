package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private Set<Ship> ships;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private Set<Salvo> salvoes;


    public Map<String,Object> makeGamePlayerDTO(){
       Map<String,Object> dto = new LinkedHashMap<>();
       dto.put("id", this.getId());
       dto.put("player", this.getPlayer().makePlayerDTO());
       return dto;
    }
    
    public List<Object> getShipDTO(){
        return this.getShips()
                        .stream()
                        .map(ship -> ship.makeShipDTO())
                        .collect(toList());
    }


    public Ship addShip(String shipType,List<String> locations){
        Ship ship=new Ship(shipType,locations,this);
        return ship;
    }

    public Score getScore(){
        return this.player.getScore(this.game);
    }

    public GamePlayer(){
        this.joinDate=new Date();
    }

    public GamePlayer(Game game,Player player){
        this.game=game;
        this.player=player;
        this.joinDate=new Date();
    }


    public GamePlayer getOpponent(){
        return this.getGame()
                .gamePlayers
                .stream()
                .filter(gamePlayer -> gamePlayer.getId()!=this.getId())
                .findFirst().orElse(new GamePlayer());
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
