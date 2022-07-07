/**
 * Bean Stores Account Information
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nttdata.apirestaccounts.dto.CustomerDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private AccountType accountType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate creationDate = LocalDate.now();
    private String accountNumber;
    private String currency;
    private double amount; //monto
    private double balance;//saldo
    private CustomerDto customer;
    private String state;
    private int maxLimitMovementPerMonth;
    private boolean principal;
    private List<HeadLine> headlines;
    private List<AuthorizedSigner> authorizedSigners;
}
