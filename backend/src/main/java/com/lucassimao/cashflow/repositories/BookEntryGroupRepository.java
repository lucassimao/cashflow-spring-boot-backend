package com.lucassimao.cashflow.repositories;

import java.util.List;
import java.util.Optional;

import com.lucassimao.cashflow.model.BookEntryGroup;
import com.lucassimao.cashflow.model.BookEntryType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "bookEntryGroups", path = "bookEntryGroups")
public interface BookEntryGroupRepository
          extends PagingAndSortingRepository<BookEntryGroup, Long>, TenantAwareRepository {

     List<BookEntryGroup> findByType(@Param("type") BookEntryType type);

     @Query("SELECT b FROM BookEntryGroup b WHERE b.id = ?1")
     Optional<BookEntryGroup> findById(Long id);

}