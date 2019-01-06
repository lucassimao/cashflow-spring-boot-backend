package com.lucassimao.fluxodecaixa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@JsonTest
public class MoneyJsonDeserializerTests {


    @Autowired
    private ObjectMapper objectMapper;
 
    @Test
    public void testDeserialization() throws IOException {

        String json = "{\"currency\":\"BRL\",\"value\":100.09}";
        Money _100Reais = objectMapper.readValue(json, Money.class);

        assertEquals(100.09, _100Reais.getNumber().doubleValue(),0.001);
        assertEquals("BRL", _100Reais.getCurrency().getCurrencyCode());
    }

}

