package com.lucassimao.fluxodecaixa.repositories;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import javax.money.Monetary;

import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.Goal;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "goalWithBookEntries", types = Goal.class)
public interface GoalWithBookEntriesProjection  {

    public ZonedDateTime getStart();

    public ZonedDateTime getEnd();

    public BookEntryGroup getBookEntryGroup();

    public Money getMaximum();

    public LocalDateTime getDateCreated();

    public LocalDateTime getDateUpdated();

    @Value("#{@bookEntryRepository.findAllForGoal(target.start,target.end,target.bookEntryGroup)}")    
    public List<BookEntry> getBookEntries();

    public default boolean isExceeded(){

        Money total = this.getBookEntries().stream()
                          .map(BookEntry::getValue)
                          .reduce((v1,v2) ->  v1.add(v2))
                          .orElse(Money.zero(Monetary.getCurrency("BRL")));

        return total.isGreaterThan(getMaximum());
    }

}