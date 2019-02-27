package com.lucassimao.fluxodecaixa.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
                ZoneId utc = ZoneId.of("UTC");
				jackson2ObjectMapperBuilder.serializers(new ZonedDateTimeSerializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(utc)));
            }

        };
    }

}