package com.lucassimao.fluxodecaixa.repositories;

import java.util.List;

import com.lucassimao.fluxodecaixa.model.BookEntryGroup;
import com.lucassimao.fluxodecaixa.model.BookEntryType;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "bookEntryGroups", path = "bookEntryGroups")
public interface BookEntryGroupRepository extends PagingAndSortingRepository<BookEntryGroup,Long>{

    List<BookEntryGroup> findByType(@Param("type") BookEntryType type);
}