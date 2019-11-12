package com.codeoftheweb.salvo.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date creationDate;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;


    public Map<String, Object> makeGameDTO() {
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id" , this.getId());
        dto.put("created", this.creationDate);
        dto.put("gamePlayers", this.getGamePlayers()
                .stream()
                .map(gamePlayer-> gamePlayer.makeGamePlayerDTO())
                .collect(Collectors.toList()));
        return dto;
    }

    public Game(){
        this.creationDate=new Date();
    }

    public Game(Date creationDate){
        this.creationDate=creationDate;
    }


    public Date getDate() {
        return creationDate;
    }

    public void setDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public long getId() {
        return id;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
