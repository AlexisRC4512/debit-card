package com.nttdata.debit_card.service.impl;

import com.nttdata.debit_card.model.domain.Account;
import com.nttdata.debit_card.model.entity.DebitCard;
import com.nttdata.debit_card.model.exception.DebitCardNotFoundException;
import com.nttdata.debit_card.model.request.DebitCardRequest;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.DebitCardResponse;
import com.nttdata.debit_card.model.response.TransactionResponse;
import com.nttdata.debit_card.repository.DebitCardRepository;
import com.nttdata.debit_card.service.AccountService;
import com.nttdata.debit_card.service.DebitCardService;
import com.nttdata.debit_card.util.DebitCardMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@Log4j2
public class DebitCardServiceImpl implements DebitCardService {
    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private AccountService accountService;

    @Override
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackCreateDebitCard")
    @TimeLimiter(name = "debit-card")
    public Mono<DebitCardResponse> createDebitCard(DebitCardRequest debitCardRequest) {
        return debitCardRepository.findByNumberDebitCard(debitCardRequest.getNumberDebitCard())
                .flatMap(existingDebitCard -> Mono.<DebitCardResponse>error(new DebitCardNotFoundException("That account number already exists")))
                .switchIfEmpty(
                        debitCardRepository.save(DebitCardMapper.debitCardRequestToDebitCard(debitCardRequest))
                                .map(DebitCardMapper::debitCardToDebitCardResponse)
                                .doOnError(e -> log.error("Error creating account", e))
                                .onErrorMap(e -> new Exception("Error creating account", e))
                );
    }


    @Override
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackGetAllDebitCard")
    @TimeLimiter(name = "debit-card")
    public Flux<DebitCardResponse> getAllDebitCard() {
        return debitCardRepository.findAll()
                .map(DebitCardMapper::debitCardToDebitCardResponse)
                .doOnError(e -> log.error("Error finding DebitCards: {}", e.getMessage()))
                .onErrorResume(e -> Flux.error(new Exception("Failed to find DebitCards", e)));
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackGetDebitCardById")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<DebitCardResponse> getDebitCardById(String id) {
        return debitCardRepository.findById(id)
                .map(DebitCardMapper::debitCardToDebitCardResponse)
                .switchIfEmpty(Mono.error(new DebitCardNotFoundException("Debit card not found with id: " + id)))
                .onErrorMap(e -> new Exception("Error fetching DebitCard by id", e));
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackDeleteById")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<Void> deleteById(String id) {
        return debitCardRepository.findById(id)
                .switchIfEmpty(Mono.error(new DebitCardNotFoundException("Debit card not found with id: " + id)))
                .flatMap(debitCard -> debitCardRepository.delete(debitCard))
                .onErrorMap(e -> new Exception("Error deleting DebitCard by id", e));
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackUpdateDebitCard")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<DebitCardResponse> updateDebitCard(String id, DebitCardRequest debitCardRequest) {
        return debitCardRepository.findById(id)
                .switchIfEmpty(Mono.error(new DebitCardNotFoundException("Debit card not found with id: " + id)))
                .flatMap(existingDebitCard -> {
                    DebitCard updatedCard = DebitCardMapper.debitCardRequestToDebitCard(debitCardRequest);
                    updatedCard.setId(existingDebitCard.getId());
                    return debitCardRepository.save(updatedCard)
                            .map(DebitCardMapper::debitCardToDebitCardResponse)
                            .onErrorMap(e -> new Exception("Error updating DebitCard by id", e));
                });
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackWithdraw")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<TransactionResponse> withdraw(String idDebiCard, TransactionRequest transactionRequest) {
        return handleTransaction(idDebiCard, transactionRequest, "withdrawing");
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackPaymentByCardId")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<TransactionResponse> paymentByCardId(String idDebiCard, TransactionRequest transactionRequest) {
        return handleTransaction(idDebiCard, transactionRequest, "payment");
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackGetAccountBalance")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<Double> getAccountBalance(String idAccount) {
        return accountService.getAccountById(idAccount)
                .map(Account::getBalance)
                .doOnError(e -> log.error("Error fetching account balance: {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new Exception("Failed to fetch account balance", e)));
    }


    private Mono<TransactionResponse> handleTransaction(String idDebiCard, TransactionRequest transactionRequest, String action) {
        return debitCardRepository.findById(idDebiCard)
                .flatMap(debitCard -> accountService.withdrawAccount(debitCard.getPrincipalAccountId(), transactionRequest)
                        .onErrorResume(ex -> {
                            log.error("Error {} from principal account {}: {}", action, debitCard.getPrincipalAccountId(), ex.getMessage());
                            return Flux.fromIterable(debitCard.getAccounts())
                                    .concatMap(accountId -> accountService.withdrawAccount(accountId, transactionRequest)
                                            .onErrorResume(innerEx -> {
                                                log.error("Error {} from account {}: {}", action, accountId, innerEx.getMessage());
                                                return Mono.empty();
                                            }))
                                    .next()
                                    .switchIfEmpty(Mono.error(new Exception("Failed to " + action + " from all associated accounts")));
                        }));
    }
    public Mono<DebitCardResponse> fallbackCreateDebitCard(Exception exception) {
        log.error("Fallback method for createDebitCard", exception);
        return Mono.error(new Exception("Fallback method for createDebitCard"));
    }

    public Flux<DebitCardResponse> fallbackGetAllDebitCard(Exception exception) {
        log.error("Fallback method for getAllDebitCard", exception);
        return Flux.error(new Exception("Fallback method for getAllDebitCard"));
    }

    public Mono<DebitCardResponse> fallbackGetDebitCardById(Exception exception) {
        log.error("Fallback method for getDebitCardById", exception);
        return Mono.error(new Exception("Fallback method for getDebitCardById"));
    }

    public Mono<Void> fallbackDeleteById(Exception exception) {
        log.error("Fallback method for deleteById", exception);
        return Mono.error(new Exception("Fallback method for deleteById"));
    }

    public Mono<DebitCardResponse> fallbackUpdateDebitCard(Exception exception) {
        log.error("Fallback method for updateDebitCard", exception);
        return Mono.error(new Exception("Fallback method for updateDebitCard"));
    }

    public Mono<TransactionResponse> fallbackWithdraw(Exception exception) {
        log.error("Fallback method for withdraw", exception);
        return Mono.error(new Exception("Fallback method for withdraw"));
    }

    public Mono<TransactionResponse> fallbackPaymentByCardId(Exception exception) {
        log.error("Fallback method for paymentByCardId", exception);
        return Mono.error(new Exception("Fallback method for paymentByCardId"));
    }

    public Mono<Double> fallbackGetAccountBalance(Exception exception) {
        log.error("Fallback method for getAccountBalance", exception);
        return Mono.error(new Exception("Fallback method for getAccountBalance"));
    }

    public Flux<DebitCardResponse> fallbackGetDebitCardByClientId(Exception exception) {
        log.error("Fallback method for getDebitCardByClientId", exception);
        return Flux.error(new Exception("Fallback method for getDebitCardByClientId"));
    }

}