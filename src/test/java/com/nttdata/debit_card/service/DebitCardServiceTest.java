package com.nttdata.debit_card.service;


import com.nttdata.debit_card.model.entity.DebitCard;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.TransactionResponse;
import com.nttdata.debit_card.repository.DebitCardRepository;
import com.nttdata.debit_card.service.impl.DebitCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class DebitCardServiceTest {

    @Mock
    private DebitCardRepository debitCardRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private DebitCardServiceImpl debitCardService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testHandleTransactionSuccess() {
        String idDebiCard = "debitCard123";
        TransactionRequest transactionRequest = new TransactionRequest();
        DebitCard debitCard = new DebitCard();
        debitCard.setPrincipalAccountId("account123");

        when(debitCardRepository.findById(idDebiCard)).thenReturn(Mono.just(debitCard));
        when(accountService.withdrawAccount(anyString(), any(TransactionRequest.class)))
                .thenReturn(Mono.just(new TransactionResponse()));

        Mono<TransactionResponse> result = debitCardService.withdraw(idDebiCard, transactionRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response != null)
                .verifyComplete();
    }

    @Test
    public void testHandleTransactionFailure() {
        String idDebiCard = "debitCard123";
        TransactionRequest transactionRequest = new TransactionRequest();
        DebitCard debitCard = new DebitCard();
        debitCard.setPrincipalAccountId("account123");

        when(debitCardRepository.findById(idDebiCard)).thenReturn(Mono.just(debitCard));
        when(accountService.withdrawAccount(anyString(), any(TransactionRequest.class)))
                .thenReturn(Mono.error(new Exception("Withdrawal failed")));

        Mono<TransactionResponse> result = debitCardService.withdraw(idDebiCard, transactionRequest);

        StepVerifier.create(result)
                .expectError(Exception.class)
                .verify();
    }
}