package com.nttdata.debit_card.service;

import com.nttdata.debit_card.model.domain.Account;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final WebClient webClient;

    public Mono<Account> getAccountById(String accountId) {
        return webClient.get()
                .uri("/api/v1/account/{id_account}", accountId)
                .retrieve()
                .bodyToMono(Account.class);
    }
    public Mono<TransactionResponse> withdrawAccount(String accountId, TransactionRequest transactionRequest) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/account/{id_account}/withdraw")
                        .build(accountId))
                .body(Mono.just(transactionRequest), TransactionRequest.class)
                .retrieve()
                .bodyToMono(TransactionResponse.class);
    }
}
