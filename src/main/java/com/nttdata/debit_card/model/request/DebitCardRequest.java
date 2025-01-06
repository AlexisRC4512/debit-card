package com.nttdata.debit_card.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class DebitCardRequest {
    private Integer numberDebitCard;
    private String principalAccountId;
    private Set<String> accounts;

    public DebitCardRequest(Integer numberDebitCard, String principalAccountId, Set<String> accounts) {
        setNumberDebitCard(numberDebitCard);
        setPrincipalAccountId(principalAccountId);
        setAccounts(accounts);
    }

    public void setNumberDebitCard(Integer numberDebitCard) {
        if ( numberDebitCard.toString().length() < 8) {
            throw new IllegalArgumentException("The debit card number must have at least 9 characters.");
        }
        this.numberDebitCard = numberDebitCard;
    }

    public void setPrincipalAccountId(String principalAccountId) {
        if (principalAccountId.isEmpty() || principalAccountId.isBlank()) {
            throw new IllegalArgumentException("principalAccountId is required");
        }
        this.principalAccountId = principalAccountId;
    }

    public void setAccounts(Set<String> accounts) {
        if (accounts == null) {
            throw new IllegalArgumentException("accounts is null");
        }
        this.accounts = accounts;
    }
}
