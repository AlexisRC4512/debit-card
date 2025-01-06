package com.nttdata.debit_card.model.response;

import com.nttdata.debit_card.model.enums.TypeTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private String clientId;
    private TypeTransaction type;
    private double amount;
    private Date date;
    private String description;
}
