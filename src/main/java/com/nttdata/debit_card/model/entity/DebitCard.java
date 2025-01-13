package com.nttdata.debit_card.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "debitCard")
public class DebitCard {
    @Id
    private String id;
    private String numberDebitCard;
    private String principalAccountId;
    private Set<String>accounts;
}
