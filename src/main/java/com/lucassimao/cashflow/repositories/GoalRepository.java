package com.lucassimao.cashflow.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.lucassimao.cashflow.model.Goal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@RepositoryRestResource(collectionResourceRel = "goals", path = "goals",excerptProjection = GoalWithBookEntriesProjection.class)
public interface GoalRepository extends PagingAndSortingRepository<Goal, Long>, TenantAwareRepository {

     @Query("SELECT g FROM Goal g WHERE g.id = ?1")
     Optional<Goal> findById(Long id);

     @Query("SELECT g FROM Goal g WHERE :start is not null and :end is not null and DATE(g.start) >= DATE(:start) and DATE(g.end) <=  DATE(:end)")
     Page<Goal> findByInterval(@Param("start") @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime start,
               @Param("end") @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime end, Pageable p);

}