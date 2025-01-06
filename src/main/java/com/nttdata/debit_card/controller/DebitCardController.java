package com.nttdata.debit_card.controller;

import com.nttdata.debit_card.model.request.DebitCardRequest;
import com.nttdata.debit_card.model.request.TransactionRequest;
import com.nttdata.debit_card.model.response.DebitCardResponse;
import com.nttdata.debit_card.model.response.TransactionResponse;
import com.nttdata.debit_card.service.DebitCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/debitcard")
public class DebitCardController {
    @Autowired
    private DebitCardService debitCardService;

    @PostMapping
    public Mono<DebitCardResponse> createDebitCard(@RequestBody DebitCardRequest debitCardRequest) {
        return debitCardService.createDebitCard(debitCardRequest);
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
    public Mono<TransactionResponse> withdraw(@PathVariable String id, @RequestBody TransactionRequest transactionRequest) {
        return debitCardService.withdraw(id, transactionRequest);
    }

    @PostMapping("/{id}/payment")
    public Mono<TransactionResponse> paymentByCardId(@PathVariable String id, @RequestBody TransactionRequest transactionRequest) {
        return debitCardService.paymentByCardId(id, transactionRequest);
    }

    @GetMapping("/{idAccount}/balance")
    public Mono<Double> getAccountBalance(@PathVariable String idAccount) {
        return debitCardService.getAccountBalance(idAccount);
    }

}
