package com.lucassimao.cashflow.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.persistence.QueryHint;

import com.lucassimao.cashflow.model.BookEntry;
import com.lucassimao.cashflow.model.BookEntryGroup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@RepositoryRestResource(collectionResourceRel = "bookEntries", path = "bookEntries")
public interface BookEntryRepository extends PagingAndSortingRepository<BookEntry, Long>, TenantAwareRepository {

    @Query("SELECT b FROM BookEntry b WHERE b.id = ?1")
    Optional<BookEntry> findById(Long id);

    @Query("SELECT b FROM BookEntry b WHERE b.bookEntryGroup=:group AND CAST(b.date as date) between CAST(:start AS date) and CAST(:end AS date) order by b.date desc")
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    @RestResource(exported = false)
    List<BookEntry> findAllForGoal(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end,
            @Param("group") BookEntryGroup bookEntryGroup);

    @Query("SELECT b FROM BookEntry b WHERE :start is not null and :end is not null and CAST(b.date AS date) between CAST(:start AS date) and CAST(:end AS date) order by b.date desc")
    Page<BookEntry> findByInterval(@Param("start") @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime start,
            @Param("end") @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime end, Pageable p);

}