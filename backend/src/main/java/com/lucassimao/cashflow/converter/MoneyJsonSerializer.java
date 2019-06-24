package com.lucassimao.cashflow.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.javamoney.moneta.Money;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class MoneyJsonSerializer extends JsonSerializer<Money> {

        @Override
        public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeStringField("currency", value.getCurrency().getCurrencyCode());
                gen.writeNumberField("value", value.getNumber().doubleValue());
                gen.writeEndObject();
        }

}