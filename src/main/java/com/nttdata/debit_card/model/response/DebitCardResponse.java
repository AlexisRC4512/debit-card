package com.nttdata.debit_card.model.response;

import lombok.*;

import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebitCardResponse {
    private String id;
    private Integer numberDebitCard;
    private String principalAccountId;
    private Set<String> accounts;
}
