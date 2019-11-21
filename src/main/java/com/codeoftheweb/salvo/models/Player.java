package com.codeoftheweb.salvo.models;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String userName;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<Score> scores;

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

    public Score getScore(Game game){

        return this.getScores()
                .stream()
                .filter(score -> score.getGame().getId()==game.getId())
                .findFirst().orElse(null);

    }

    public long scoreCount(List<Double> totalScores ,double x){
        return totalScores.stream()
                .filter(score -> score==x)
                .count();
    }

    public Map<String,Object> getLeaderboardDTO(){

        List<Double> allScores=this.getScores()
                            .stream()
                            .map(score -> score.getScore())
                            .collect(Collectors.toList());

        long totalWon = scoreCount(allScores,1.0);

        long totalLose = scoreCount(allScores,0.0);

        long totalTied = scoreCount(allScores,0.5);

        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("name",this.getUserName());
        dto.put("total",allScores.stream().reduce(0.0,Double::sum));
        dto.put("won",  totalWon);
        dto.put("lost",totalLose);
        dto.put("tied",totalTied);

        return dto;
    }

    public Set<Score> getScores() {
        return scores;
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
