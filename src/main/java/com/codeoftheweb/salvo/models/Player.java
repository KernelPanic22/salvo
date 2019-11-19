package com.codeoftheweb.salvo.models;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String userName;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    public Map<String,Object> makePlayerDTO() {
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }

    public Player(){
    }

    public Player(String username) {
        this.userName  = username;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public long getId() {
        return id;
    }
}
