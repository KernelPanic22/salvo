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

    private String shipType;

    @ElementCollection
    private List<String> locations = new ArrayList<>();

    public Map<String,Object> makeShipDTO(){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("type",this.getShipType());
        dto.put("locations", this.locations);
        return dto;
    }

    public Ship(){

    }

    public Ship(String shipType,List<String> locations,GamePlayer gamePlayer){
        this.shipType=shipType;
        this.locations=locations;
        this.gamePlayer=gamePlayer;
    }


    public Ship(List<String> locations){
        this.locations=locations;
    }


    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public List<String> getLocations() {
        return locations;
    }
}
