package com.lucassimao.fluxodecaixa.model;

import java.util.Objects;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import com.lucassimao.fluxodecaixa.converter.BookEntryTypeConverter;


@Entity
@Table(indexes = {
    @Index(columnList = "tenantId")
})
public class BookEntryGroup extends TenantEntity{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @Convert(converter = BookEntryTypeConverter.class)
    private BookEntryType type;
    @NotEmpty
    private String description;


    public BookEntryGroup() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookEntryType getType() {
        return this.type;
    }

    public void setType(BookEntryType type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BookEntryGroup)) {
            return false;
        }
        BookEntryGroup bookEntryGroup = (BookEntryGroup) o;
        return Objects.equals(id, bookEntryGroup.id) && Objects.equals(type, bookEntryGroup.type) && Objects.equals(description, bookEntryGroup.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, description);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
    
}