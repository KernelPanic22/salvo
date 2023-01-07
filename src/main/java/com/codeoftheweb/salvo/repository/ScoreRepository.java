package com.codeoftheweb.salvo.repository;


import com.codeoftheweb.salvo.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

@RestResource
public interface ScoreRepository extends JpaRepository<Score,Long> {


}
