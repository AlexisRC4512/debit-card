package com.nttdata.debit_card.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String id;
    private String type;
    private double balance;
    private Date openingDate;
    private double transactionLimit;
    private double maintenanceFee;
    private String clientId;
    private List<String> holders;
    private List<String> authorizedSigners;
    private List<Transaction> transactions;
    private Integer numberAccount;
    private int transactionCount;
}
