package com.nttdata.debit_card.util;


import com.nttdata.debit_card.model.entity.DebitCard;
import com.nttdata.debit_card.model.request.DebitCardRequest;
import com.nttdata.debit_card.model.response.DebitCardResponse;

import java.util.UUID;

public class DebitCardMapper {

    public static DebitCard debitCardRequestToDebitCard(DebitCardRequest debitCardRequest) {
        DebitCard debitCard = new DebitCard();
        debitCard.setId(UUID.randomUUID().toString());
        debitCard.setNumberDebitCard(debitCardRequest.getNumberDebitCard());
        debitCard.setAccounts(debitCardRequest.getAccounts());
        debitCard.setPrincipalAccountId(debitCardRequest.getPrincipalAccountId());
        return debitCard;
    }
    public static DebitCardResponse debitCardToDebitCardResponse(DebitCard debitCard) {
        DebitCardResponse debitCardResponse = new DebitCardResponse();
        debitCardResponse.setId(debitCard.getId());
        debitCardResponse.setNumberDebitCard(debitCard.getNumberDebitCard());
        debitCardResponse.setAccounts(debitCard.getAccounts());
        debitCardResponse.setPrincipalAccountId(debitCard.getPrincipalAccountId());
        return debitCardResponse;
    }

}
