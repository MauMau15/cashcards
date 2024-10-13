package com.example.springcourse.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/cashcards")
class CashCardController {

    private final CashCardRepository repository;

    private final CashCardPagingAndSortingRepository cashCardPagingAndSortingRepository;

    private CashCardController(
            CashCardRepository repository,
            CashCardPagingAndSortingRepository cashCardPagingAndSortingRepository
    ) {
        this.repository = repository;
        this.cashCardPagingAndSortingRepository = cashCardPagingAndSortingRepository;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findCashCard(
            @PathVariable("id") Long id,
            Principal principal
    ) {

        //Optional<CashCard> cashCardOpt = repository.findById(id);
        Optional<CashCard> cashCardOptional = Optional.ofNullable(
                repository.findByIdAndOwner(id, principal.getName())
        );

        if (cashCardOptional.isPresent())
            return ResponseEntity.ok(cashCardOptional.get());
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private ResponseEntity<Void> createCashCard(
            @RequestBody CashCard cashCard,
            UriComponentsBuilder ucb,
            Principal principal
    ) {

        //CashCard newCashCard = repository.save(cashCard);
        CashCard newCashCardWithOwner = new CashCard(null, cashCard.name(), principal.getName());
        CashCard createdCashCard = repository.save(newCashCardWithOwner);


        URI locationOfNewCashCard = ucb.path("/cashcards/{id}").buildAndExpand(createdCashCard.id()).toUri();

        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    private ResponseEntity<List<CashCard>> findAllCashCard(
            Pageable pageable,
            Principal principal
    ) {

        /*Page<CashCard> page = cashCardPagingAndSortingRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                )
        );*/

        Page<CashCard> page = repository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                )
        );

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    private ResponseEntity<List<CashCard>> findAll(
            Principal principal
    ) {
        /*List<CashCard> cashCards = StreamSupport.stream(
                repository
                        .findAll()
                        .spliterator(),
                false
        ).collect(Collectors.toList());*/
        List<CashCard> cashCards = repository.findByOwner(principal.getName());
        return ResponseEntity.ok(cashCards);
    }

    @PutMapping("/{id}")
    private ResponseEntity<Void> putCashCard(
            @PathVariable("id") Long id,
            @RequestBody CashCard cashCardUpdate,
            Principal principal
    ) {

        CashCard cashCard = repository.findByIdAndOwner(id, principal.getName());

        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.name(), principal.getName());
                repository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();

    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(
            @PathVariable("id") Long id,
            Principal principal
    ) {

        if (repository.existsByIdAndOwner(id, principal.getName())) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }


}
