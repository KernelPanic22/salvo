package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name = "location")
    private List<String> salvoLocations = new ArrayList<>();

    private long turn;

    public Map<String,Object> getSalvoDTO() {
        Map<String,Object> dto = new LinkedHashMap<>();

        dto.put("turn", this.getTurn());
        dto.put("player", this.getGamePlayer().getPlayer().getId());
        dto.put("locations",this.getSalvoLocations());
        return dto;
    }

    public Salvo() {

    }

    public Salvo(GamePlayer gamePlayer, List<String> salvoLocations, long turn){
        this.gamePlayer=gamePlayer;
        this.salvoLocations = salvoLocations;
        this.turn=turn;
    }

    public long getHits(Ship ship){
        return this.getSalvoLocations()
                .stream()
                .filter(salvoLocation -> ship.getShipLocations().contains(salvoLocation))
                .count();
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public long getTurn() {
        return turn;
    }

    public void setTurn(long turn) {
        this.turn = turn;
    }
}

