package com.lucassimao.fluxodecaixa.model;

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

import com.lucassimao.fluxodecaixa.converter.MoneyConverter;

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

    

}