package com.lucassimao.fluxodecaixa.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import com.lucassimao.fluxodecaixa.model.BookEntry;
import com.lucassimao.fluxodecaixa.model.BookEntryGroup;

import org.javamoney.moneta.Money;

public class GoalDTO {

    private Long id;

    private ZonedDateTime start, end;
    private BookEntryGroup bookEntryGroup;
    private Money maximum;

    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private List<BookEntry> bookEntries;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the start
     */
    public ZonedDateTime getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public ZonedDateTime getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    /**
     * @return the bookEntryGroup
     */
    public BookEntryGroup getBookEntryGroup() {
        return bookEntryGroup;
    }

    /**
     * @param bookEntryGroup the bookEntryGroup to set
     */
    public void setBookEntryGroup(BookEntryGroup bookEntryGroup) {
        this.bookEntryGroup = bookEntryGroup;
    }

    /**
     * @return the maximum
     */
    public Money getMaximum() {
        return maximum;
    }

    /**
     * @param maximum the maximum to set
     */
    public void setMaximum(Money maximum) {
        this.maximum = maximum;
    }

    /**
     * @return the dateCreated
     */
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the dateUpdated
     */
    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    /**
     * @return the bookEntries
     */
    public List<BookEntry> getBookEntries() {
        return bookEntries;
    }

    /**
     * @param bookEntries the bookEntries to set
     */
    public void setBookEntries(List<BookEntry> bookEntries) {
        this.bookEntries = bookEntries;
    }

    


}