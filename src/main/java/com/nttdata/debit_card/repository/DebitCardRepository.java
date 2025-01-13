package com.nttdata.debit_card.repository;

import com.nttdata.debit_card.model.entity.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard,String> {
    Mono<DebitCard> findByNumberDebitCard(String id);
}
