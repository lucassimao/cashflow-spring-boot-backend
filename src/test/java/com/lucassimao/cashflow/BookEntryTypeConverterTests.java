package com.lucassimao.cashflow;

import static org.junit.Assert.assertNotNull;

import com.lucassimao.cashflow.converter.BookEntryTypeConverter;
import com.lucassimao.cashflow.model.BookEntryType;

import org.junit.Before;
import org.junit.Test;

public class BookEntryTypeConverterTests{

    private BookEntryTypeConverter bookEntryTypeConverter;

    @Before
    public void setup(){
        this.bookEntryTypeConverter = new BookEntryTypeConverter();
    }

    @Test
    public void testAllTypesAreConvertibleToDatabaseColumns(){
        BookEntryType[] types = BookEntryType.values();
        for(BookEntryType type: types)
            assertNotNull(bookEntryTypeConverter.convertToDatabaseColumn(type));
    }

}