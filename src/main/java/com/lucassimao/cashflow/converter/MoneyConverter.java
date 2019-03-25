package com.lucassimao.cashflow.converter;

import javax.persistence.AttributeConverter;

import org.javamoney.moneta.Money;

public class MoneyConverter implements AttributeConverter<Money, String> {

    @Override
    public String convertToDatabaseColumn(Money attribute) {
        if (attribute == null)
            throw new IllegalArgumentException("invalid null value");
        return attribute.toString();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        return Money.parse(dbData);
    }

}