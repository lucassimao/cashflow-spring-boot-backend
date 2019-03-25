package com.lucassimao.cashflow.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.lucassimao.cashflow.converter.MoneyConverter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.javamoney.moneta.Money;

@Entity
@Table(indexes = { @Index(columnList = "tenantId") })
public class Goal extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private ZonedDateTime start;

    @NotNull
    private ZonedDateTime end;

    @ManyToOne
    @NotNull
    private BookEntryGroup bookEntryGroup;

    @Convert(converter = MoneyConverter.class)
    @NotNull
    private Money maximum;

    @CreationTimestamp
    private LocalDateTime dateCreated;
    @UpdateTimestamp
    private LocalDateTime dateUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public BookEntryGroup getBookEntryGroup() {
        return bookEntryGroup;
    }

    public void setBookEntryGroup(BookEntryGroup bookEntryGroup) {
        this.bookEntryGroup = bookEntryGroup;
    }

    public Money getMaximum() {
        return maximum;
    }

    public void setMaximum(Money maximum) {
        this.maximum = maximum;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    

}