package com.lucassimao.fluxodecaixa.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lucassimao.fluxodecaixa.converter.MoneyConverter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.javamoney.moneta.Money;

@Entity
@Table(indexes = {
    @Index(columnList = "tenantId")
})
public class BookEntry extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private BookEntryGroup bookEntryGroup;
    private String description;

    private ZonedDateTime date;
    @Convert(converter = MoneyConverter.class)
    private Money value;

    @CreationTimestamp
    private LocalDateTime dateCreated;
    @UpdateTimestamp
    private LocalDateTime dateUpdated;

    public BookEntry() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookEntryGroup getBookEntryGroup() {
        return this.bookEntryGroup;
    }

    public void setBookEntryGroup(BookEntryGroup bookEntryGroup) {
        this.bookEntryGroup = bookEntryGroup;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public Money getValue() {
        return this.value;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    public LocalDateTime getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return this.dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BookEntry)) {
            return false;
        }
        BookEntry bookEntry = (BookEntry) o;
        return Objects.equals(id, bookEntry.id) && Objects.equals(bookEntryGroup, bookEntry.bookEntryGroup)
                && Objects.equals(description, bookEntry.description) && Objects.equals(date, bookEntry.date)
                && Objects.equals(value, bookEntry.value) && Objects.equals(dateCreated, bookEntry.dateCreated)
                && Objects.equals(dateUpdated, bookEntry.dateUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bookEntryGroup, description, date, value, dateCreated, dateUpdated);
    }

    @Override
    public String toString() {
        return "{" + " id='" + getId() + "'" + ", bookEntryGroup='" + getBookEntryGroup() + "'" + ", description='"
                + getDescription() + "'" + ", date='" + getDate() + "'" + ", value='" + getValue() + "'"
                + ", dateCreated='" + getDateCreated() + "'" + ", dateUpdated='" + getDateUpdated() + "'" + "}";
    }

}