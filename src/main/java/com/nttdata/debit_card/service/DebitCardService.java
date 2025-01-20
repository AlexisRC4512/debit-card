package com.nttdata.debit_card.service;


import com.nttdata.debit_card.model.request.DebitCardRequest;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.DebitCardResponse;
import com.nttdata.debit_card.model.response.TransactionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DebitCardService {
    Mono<DebitCardResponse> createDebitCard(DebitCardRequest debitCardRequest ,String authorizationHeader);
    Flux<DebitCardResponse> getAllDebitCard();
    Mono<DebitCardResponse> getDebitCardById(String id);
    Mono<Void> deleteById(String id);
    Mono<DebitCardResponse> updateDebitCard(String id, DebitCardRequest debitCardRequest);
    Mono<TransactionResponse> withdraw(String idAccount, TransactionRequest transactionRequest ,String authorizationHeader);
    Mono<TransactionResponse> paymentByCardId(String id, TransactionRequest transactionRequest ,String authorizationHeader);
    Mono<Double> getAccountBalance(String idAccount ,String authorizationHeader);

}
