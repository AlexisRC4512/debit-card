package com.nttdata.debit_card.model.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventState {
    private String state;
    private String idPay;
    private List<String> debitCardNumbers;
    private List<String> listTransactionId;

}
