
package com.lucassimao.fluxodecaixa.config;

import java.util.Collections;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.modelmapper.ModelMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

// https://auth0.com/blog/automatically-mapping-dto-to-entity-on-spring-boot-apis/
public class GoalDTOModelMapper extends RequestResponseBodyMethodProcessor {

    private static final ModelMapper modelMapper = new ModelMapper();

    private EntityManager entityManager;

    public GoalDTOModelMapper(ObjectMapper objectMapper, EntityManager entityManager) {
        super(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
        this.entityManager = entityManager;
    }


}