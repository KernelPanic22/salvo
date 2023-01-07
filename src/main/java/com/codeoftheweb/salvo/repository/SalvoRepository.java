package com.codeoftheweb.salvo.repository;


import com.codeoftheweb.salvo.models.Salvo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

@RestResource
public interface SalvoRepository extends JpaRepository<Salvo,Long> {

}
