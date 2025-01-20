package com.nttdata.debit_card.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentEvent {
    private String idPay;
    private String eventType;
    private double amount;
    private List<String> listTransactionId;
    private List<String> debitCardNumbers;
    private String authorizationHeader;

}
