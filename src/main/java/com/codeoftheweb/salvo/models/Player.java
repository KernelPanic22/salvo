package com.codeoftheweb.salvo.models;


import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.GenerationType;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;

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

    @Override
    public String toString() {
        return "Player{" +
                "userName='" + userName + '\'' +
                "id="+ id+
                '}';
    }
}
