package com.nttdata.debit_card.controller;

import com.nttdata.debit_card.api.ApiApi;
import com.nttdata.debit_card.model.request.DebitCardRequest;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.DebitCardResponse;
import com.nttdata.debit_card.model.response.TransactionResponse;
import com.nttdata.debit_card.service.DebitCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/debitcard")
public class DebitCardController implements ApiApi {
    private final DebitCardService debitCardService;

    @PostMapping
    public Mono<DebitCardResponse> createDebitCard(@RequestBody DebitCardRequest debitCardRequest ,@RequestHeader("Authorization") String authorizationHeader) {
        return debitCardService.createDebitCard(debitCardRequest,authorizationHeader);
    }

    @GetMapping
    public Flux<DebitCardResponse> getAllDebitCard() {
        return debitCardService.getAllDebitCard();
    }

    @GetMapping("/{id}")
    public Mono<DebitCardResponse> getDebitCardById(@PathVariable String id) {
        return debitCardService.getDebitCardById(id);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteById(@PathVariable String id) {
        return debitCardService.deleteById(id);
    }

    @PutMapping("/{id}")
    public Mono<DebitCardResponse> updateDebitCard(@PathVariable String id, @RequestBody DebitCardRequest debitCardRequest) {
        return debitCardService.updateDebitCard(id, debitCardRequest);
    }

    @PostMapping("/{id}/withdraw")
    public Mono<TransactionResponse> withdraw(@PathVariable String id, @RequestBody TransactionRequest transactionRequest
            , @RequestHeader("Authorization") String authorizationHeader) {
        return debitCardService.withdraw(id, transactionRequest ,authorizationHeader);
    }

    @PostMapping("/{id}/payment")
    public Mono<TransactionResponse> paymentByCardId(@PathVariable String id, @RequestBody TransactionRequest transactionRequest
            , @RequestHeader("Authorization") String authorizationHeader) {
        return debitCardService.paymentByCardId(id, transactionRequest ,authorizationHeader);
    }

    @GetMapping("/{idAccount}/balance")
    public Mono<Double> getAccountBalance(@PathVariable String idAccount , @RequestHeader("Authorization") String authorizationHeader) {
        return debitCardService.getAccountBalance(idAccount,authorizationHeader);
    }

}
