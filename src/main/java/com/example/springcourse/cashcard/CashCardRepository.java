package com.example.springcourse.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CashCardRepository extends CrudRepository<CashCard, Long> {

    CashCard findByIdAndOwner(Long id, String owner);

    Page<CashCard> findByOwner(String owner, PageRequest pageRequest);

    List<CashCard> findByOwner(String owner);

    boolean existsByIdAndOwner(Long id, String owner);

}
