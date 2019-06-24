package com.lucassimao.cashflow.converter;

import javax.persistence.AttributeConverter;

import com.lucassimao.cashflow.model.BookEntryType;

public class BookEntryTypeConverter implements AttributeConverter<BookEntryType, String> {

    @Override
    public String convertToDatabaseColumn(BookEntryType attribute) {
        switch (attribute) {
        case Income:
            return "I";
        case Expense:
            return "E";
        default:
            throw new IllegalArgumentException("Unknown " + attribute);
        }
    }

    @Override
    public BookEntryType convertToEntityAttribute(String dbData) {
        switch (dbData) {
        case "I":
            return BookEntryType.Income;
        case "E":
            return BookEntryType.Expense;
        default:
            throw new IllegalArgumentException("Unknown " + dbData);

        }
    }

}