package com.nttdata.debit_card.service;


import com.nttdata.debit_card.model.entity.DebitCard;
import com.nttdata.debit_card.model.exception.DebitCardNotFoundException;
import com.nttdata.debit_card.model.request.DebitCardRequest;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.DebitCardResponse;
import com.nttdata.debit_card.model.response.TransactionResponse;
import com.nttdata.debit_card.repository.DebitCardRepository;
import com.nttdata.debit_card.service.impl.DebitCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class DebitCardServiceTest {

    private DebitCardRequest debitCardRequest;
    private DebitCard debitCard;
    @Mock
    private DebitCardRepository debitCardRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private DebitCardServiceImpl debitCardService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        debitCardRequest = new DebitCardRequest();
        debitCardRequest.setNumberDebitCard("123456789");

        debitCard = new DebitCard();
        debitCard.setNumberDebitCard("123456789");
    }
    @Test
     void createDebitCardShouldCreateNewDebitCardWhenDebitCardDoesNotExist() {
        debitCard.setNumberDebitCard("123456");

        when(debitCardRepository.findByNumberDebitCard(debitCardRequest.getNumberDebitCard()))
                .thenReturn(Mono.empty());
        when(debitCardRepository.save(any(DebitCard.class)))
                .thenReturn(Mono.just(debitCard));

        Mono<DebitCardResponse> result = debitCardService.createDebitCard(debitCardRequest, "auth-header");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getNumberDebitCard().equals("123456"))
                .verifyComplete();
    }



    @Test
     void getAllDebitCardShouldReturnAllDebitCards() {
        debitCard.setNumberDebitCard("123456");

        when(debitCardRepository.findAll())
                .thenReturn(Flux.just(debitCard));

        Flux<DebitCardResponse> result = debitCardService.getAllDebitCard();

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getNumberDebitCard().equals("123456"))
                .verifyComplete();
    }

    @Test
     void getDebitCardByIdShouldReturnDebitCardWhenIdExists() {
        debitCard.setId("1");
        debitCard.setNumberDebitCard("123456");

        when(debitCardRepository.findById("1"))
                .thenReturn(Mono.just(debitCard));

        Mono<DebitCardResponse> result = debitCardService.getDebitCardById("1");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getNumberDebitCard().equals("123456"))
                .verifyComplete();
    }


    @Test
     void deleteByIdShouldDeleteDebitCardWhenIdExists() {
        when(debitCardRepository.findById("1"))
                .thenReturn(Mono.just(debitCard));
        when(debitCardRepository.delete(debitCard))
                .thenReturn(Mono.empty());

        Mono<Void> result = debitCardService.deleteById("1");

        StepVerifier.create(result)
                .verifyComplete();
    }


    @Test
     void updateDebitCardShouldUpdateDebitCardWhenIdExists() {
        debitCard.setId("1");
        debitCard.setNumberDebitCard("123456");

        when(debitCardRepository.findById("1"))
                .thenReturn(Mono.just(debitCard));
        when(debitCardRepository.save(any(DebitCard.class)))
                .thenReturn(Mono.just(debitCard));

        Mono<DebitCardResponse> result = debitCardService.updateDebitCard("1", debitCardRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getNumberDebitCard().equals("123456"))
                .verifyComplete();
    }

    @Test
     void updateDebitCardShouldThrowExceptionWhenIdDoesNotExist() {
        when(debitCardRepository.findById("1"))
                .thenReturn(Mono.empty());

        Mono<DebitCardResponse> result = debitCardService.updateDebitCard("1", debitCardRequest);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof DebitCardNotFoundException &&
                        throwable.getMessage().equals("Debit card not found with id: 1"))
                .verify();
    }

    @Test
     void testHandleTransactionSuccess() {
        String idDebiCard = "debitCard123";
        String authorizationHeader = "Bearer your_jwt_token";
        TransactionRequest transactionRequest = new TransactionRequest();
        debitCard.setPrincipalAccountId("account123");

        when(debitCardRepository.findById(idDebiCard)).thenReturn(Mono.just(debitCard));
        when(accountService.withdrawAccount(anyString(), any(TransactionRequest.class), eq(authorizationHeader)))
                .thenReturn(Mono.just(new TransactionResponse()));

        Mono<TransactionResponse> result = debitCardService.withdraw(idDebiCard, transactionRequest, authorizationHeader);

        StepVerifier.create(result)
                .expectNextMatches(response -> response != null)
                .verifyComplete();
    }

    @Test
     void testHandleTransactionFailure() {
        String idDebiCard = "debitCard123";
        String authorizationHeader = "Bearer your_jwt_token";
        TransactionRequest transactionRequest = new TransactionRequest();
        debitCard.setPrincipalAccountId("account123");

        when(debitCardRepository.findById(idDebiCard)).thenReturn(Mono.just(debitCard));
        when(accountService.withdrawAccount(anyString(), any(TransactionRequest.class), eq(authorizationHeader)))
                .thenReturn(Mono.error(new Exception("Withdrawal failed")));

        Mono<TransactionResponse> result = debitCardService.withdraw(idDebiCard, transactionRequest, authorizationHeader);

        StepVerifier.create(result)
                .expectError(Exception.class)
                .verify();
    }
}