package com.nttdata.debit_card.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDebitCardDataException extends RuntimeException {
    public InvalidDebitCardDataException(String message) {
        super(message);
    }

}
