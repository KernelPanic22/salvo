package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    GamePlayer gamePlayer;

    private String type;

    @ElementCollection
    @Column(name = "shipLocations")
    private List<String> shipLocations = new ArrayList<>();

    public Map<String,Object> makeShipDTO(){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("type",this.getType());
        dto.put("locations", this.shipLocations);
        return dto;
    }

    public Ship(){

    }
    public Ship(String type, List<String> shipLocations, GamePlayer gamePlayer){
        this.type = type;
        this.shipLocations = shipLocations;
        this.gamePlayer=gamePlayer;
    }

    public Ship shipIsHit(List<String> loc){
        if(!Collections.disjoint(this.getShipLocations(),loc)){
            return this;
        }else {
            return null;
        }
    }

    public Ship(List<String> shipLocations){
        this.shipLocations = shipLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }
}
