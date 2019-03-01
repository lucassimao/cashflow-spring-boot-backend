package com.lucassimao.fluxodecaixa.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.lucassimao.fluxodecaixa.model.BookEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@RepositoryRestResource(collectionResourceRel = "bookEntries", path = "bookEntries")
public interface BookEntryRepository extends PagingAndSortingRepository<BookEntry,Long>, TenantAwareRepository{

    @Query("SELECT b FROM BookEntry b WHERE b.id = ?1")
    Optional<BookEntry> findById(Long id);

     @Query("SELECT b FROM BookEntry b WHERE :start is not null and :end is not null and DATE(b.date) between DATE(:start) and DATE(:end) order by b.date desc")
     Page<BookEntry> findByInterval(@Param("start") @DateTimeFormat(iso=ISO.DATE_TIME) ZonedDateTime start, 
                                @Param("end") @DateTimeFormat(iso=ISO.DATE_TIME) ZonedDateTime end, Pageable p);   
                                
}