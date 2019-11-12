package com.codeoftheweb.salvo;


import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    GamePlayerRepository gamePlayerRepository;

    @RequestMapping("/games")
    public List<Object> getGameAll() {

        return gameRepository.findAll()
                .stream()
                .map(game-> game.makeGameDTO())
                .collect(toList());

        }
    }

