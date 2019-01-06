package com.lucassimao.fluxodecaixa.repositories;


import com.lucassimao.fluxodecaixa.model.BookEntry;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "bookEntries", path = "bookEntries")
public interface BookEntryRepository extends PagingAndSortingRepository<BookEntry,Long>{

}