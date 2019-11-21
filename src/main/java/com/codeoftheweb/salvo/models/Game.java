package com.codeoftheweb.salvo.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date creationDate;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    Set<Score> scores;


    public Map<String, Object> makeGameDTO() {
        Map<String,Object> dto = new LinkedHashMap<>();


        dto.put("id" , this.getId());
        dto.put("created", this.creationDate);
        dto.put("gamePlayers", this.getGamePlayers()
                .stream()
                .map(gamePlayer-> gamePlayer.makeGamePlayerDTO())
                .collect(Collectors.toList()));
        dto.put("scores",this.getScores()
                        .stream()
                        .map(score -> score.makeScoreDTO())
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

    public Set<Score> getScores() {
        return scores;
    }
}
