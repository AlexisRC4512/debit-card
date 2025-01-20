package com.nttdata.debit_card.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.debit_card.model.domain.Account;
import com.nttdata.debit_card.model.entity.DebitCard;
import com.nttdata.debit_card.model.events.EventState;
import com.nttdata.debit_card.model.events.PaymentEvent;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
@Log4j2
@RequiredArgsConstructor
public class DebitCardServiceImpl implements DebitCardService {
    private final DebitCardRepository debitCardRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AccountService accountService;

    @Override
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackCreateDebitCard")
    @TimeLimiter(name = "debit-card")
    public Mono<DebitCardResponse> createDebitCard(DebitCardRequest debitCardRequest ,String authorizationHeader) {
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
    public Mono<TransactionResponse> withdraw(String idDebiCard, TransactionRequest transactionRequest ,String authorizationHeader) {
        return handleTransaction(idDebiCard, transactionRequest, "withdrawing",authorizationHeader);
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackPaymentByCardId")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<TransactionResponse> paymentByCardId(String idDebiCard, TransactionRequest transactionRequest,String authorizationHeader) {
        return handleTransaction(idDebiCard, transactionRequest, "payment",authorizationHeader);
    }
    @CircuitBreaker(name = "debit-card", fallbackMethod = "fallbackGetAccountBalance")
    @TimeLimiter(name = "debit-card")
    @Override
    public Mono<Double> getAccountBalance(String idAccount ,String authorizationHeader) {
        return accountService.getAccountById(idAccount,authorizationHeader)
                .map(Account::getBalance)
                .doOnError(e -> log.error("Error fetching account balance: {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new Exception("Failed to fetch account balance", e)));
    }


    private Mono<TransactionResponse> handleTransaction(String idDebiCard, TransactionRequest transactionRequest, String action,String authorizationHeader) {
        return debitCardRepository.findById(idDebiCard)
                .flatMap(debitCard -> accountService.withdrawAccount(debitCard.getPrincipalAccountId(), transactionRequest,authorizationHeader)
                        .onErrorResume(ex -> {
                            log.error("Error {} from principal account {}: {}", action, debitCard.getPrincipalAccountId(), ex.getMessage());
                            return Flux.fromIterable(debitCard.getAccounts())
                                    .concatMap(accountId -> accountService.withdrawAccount(accountId, transactionRequest,authorizationHeader)
                                            .onErrorResume(innerEx -> {
                                                log.error("Error {} from account {}: {}", action, accountId, innerEx.getMessage());
                                                return Mono.empty();
                                            }))
                                    .next()
                                    .switchIfEmpty(Mono.error(new Exception("Failed to " + action + " from all associated accounts")));
                        }));
    }
    @KafkaListener(id = "myConsumer2", topics = "debit-card-topic-create", groupId = "springboot-group-1", autoStartup = "true")
    public void listen(String message) {
        log.info("Message received from Kafka: {}", message);
        JSONObject jsonObject = new JSONObject(message);
        String cardNumber = jsonObject.getString("cardNumber");
        String authorizationHeader = jsonObject.getString("authorization");
        validateAndSendMessage(cardNumber,authorizationHeader);
    }
    @KafkaListener(id = "myConsumer", topics = "debit-card-topic-pay-write", groupId = "springboot-group-1", autoStartup = "true")
    public void listenMessagePay(String message) {
         ObjectMapper objectMapper = new ObjectMapper();

        try {
            log.info("Message received from Kafka: {}", message);
            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            processPaymentEvent(paymentEvent);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
    private void processPaymentEvent(PaymentEvent paymentEvent) {
        paymentEvent.getDebitCardNumbers().stream()
                .distinct()
                .forEach(cardNumber -> validateAndSendMessageW(cardNumber, paymentEvent.getAmount(), paymentEvent.getIdPay(),paymentEvent.getDebitCardNumbers(),paymentEvent.getListTransactionId(),paymentEvent.getAuthorizationHeader()));
    }
    private void validateAndSendMessageW(String cardNumber, double amount, String idPay, List<String>listOfDebitCardNumber, List<String> listTransactionId , String authorizationHeader) {
        debitCardRepository.findByNumberDebitCard(cardNumber)
                .flatMap(debitCard -> {
                    String principalAccountId = debitCard.getPrincipalAccountId();
                    return accountService.withdrawAccount(principalAccountId, new TransactionRequest(amount),authorizationHeader)
                            .flatMap(account -> {
                                EventState messageComplete = new EventState();
                                messageComplete.setState("Complete");
                                messageComplete.setIdPay(idPay);
                                messageComplete.setDebitCardNumbers(listOfDebitCardNumber);
                                messageComplete.setListTransactionId(listTransactionId);
                                JSONObject jsonCompleteMessage = new JSONObject(messageComplete);
                                log.info("Sending message to Kafka: {}", jsonCompleteMessage);
                                kafkaTemplate.send("debit-card-topic-pay-read", jsonCompleteMessage.toString());
                                log.info("Message sent to Kafka: {}", jsonCompleteMessage);
                                return Mono.empty();
                            })
                            .onErrorResume(e -> {
                                EventState messageError = new EventState();
                                messageError.setState("Error");
                                messageError.setIdPay(idPay);
                                messageError.setDebitCardNumbers(listOfDebitCardNumber);
                                JSONObject jsonErrorMessage = new JSONObject(messageError);
                                log.info("Sending error message to Kafka: {}", jsonErrorMessage);
                                kafkaTemplate.send("debit-card-topic-pay-read", jsonErrorMessage.toString());
                                log.info("Error message sent to Kafka: {}", jsonErrorMessage);
                                return Mono.error(e);
                            });
                })
                .doOnError(e -> log.error("Error validating card number: {}", cardNumber, e))
                .subscribe();
    }

    private void validateAndSendMessage(String cardNumber ,String authorizationHeader) {
        debitCardRepository.findByNumberDebitCard(cardNumber)
                .flatMap(debitCard -> {
                    String principalAccountId = debitCard.getPrincipalAccountId();
                    return accountService.getAccountById(principalAccountId ,authorizationHeader)
                            .flatMap(account -> {
                                String responseMessage = createResponseMessage(cardNumber, account.getBalance());
                                log.info("Sending message to Kafka: {}", responseMessage);
                                kafkaTemplate.send("purse-balance-topic", responseMessage);
                                log.info("Message sent to Kafka: {}", responseMessage); // Log para verificar el envío
                                return Mono.empty();
                            })
                            .onErrorResume(e -> {
                                String errorMessage = createErrorMessage(cardNumber);
                                log.info("Sending error message to Kafka: {}", errorMessage);
                                kafkaTemplate.send("purse-balance-topic", errorMessage);
                                log.info("Error message sent to Kafka: {}", errorMessage); // Log para verificar el envío
                                return Mono.empty();
                            });
                })
                .doOnError(e -> log.error("Error validating card number: {}", cardNumber, e))
                .subscribe();
    }
    private String createResponseMessage(String cardNumber, double balance) {
        return "{\"cardNumber\": \"" + cardNumber + "\", \"balance\": " + balance + "}";
    }

    private String createErrorMessage(String cardNumber) {
        return "{\"cardNumber\": \"" + cardNumber + "\", \"error\": \"Card not found or account balance retrieval failed\"}";
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