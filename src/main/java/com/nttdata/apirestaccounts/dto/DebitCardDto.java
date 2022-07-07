/**
 * Bean Stores CreditCard Information
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nttdata.apirestaccounts.model.Account;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "debitCard")
public class DebitCardDto {
    @Id
    private String id;

    private String nameCard;

    private String pan; //Personal Account Number

    private String cardType;

    private String cvv;

    private String monthYearExp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate creationDate = LocalDate.now();

    private String cardBrand; //marca

    private boolean active;

    private CustomerDto customer;

    private List<Account> accounts;
}
