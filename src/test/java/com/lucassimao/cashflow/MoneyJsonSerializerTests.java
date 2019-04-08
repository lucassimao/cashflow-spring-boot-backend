package com.lucassimao.cashflow;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@JsonTest
public class MoneyJsonSerializerTests {


	@Autowired
	private JacksonTester<Money> jacksonTester;

 
    @Test
    public void testSerialization() throws IOException {
        CurrencyUnit currencyUnit = Monetary.getCurrency("BRL");
        Money _10Reais = Money.of(10.56, currencyUnit);
        JsonContent<Money> jsonContent = jacksonTester.write(_10Reais);

		assertThat(jsonContent).extractingJsonPathStringValue("@.currency").isEqualTo("BRL");
        assertThat(jsonContent).extractingJsonPathNumberValue("@.value").isEqualTo(10.56);
    }

}

