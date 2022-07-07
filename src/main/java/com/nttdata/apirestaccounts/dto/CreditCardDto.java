/**
 * Bean Stores CreditCard Information
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
public class CreditCardDto {
    private String id;

    private String nameCard;

    private String pan; //Personal Account Number

    private String cardType;

    private String cvv;

    private String monthYearExp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateOperation = LocalDateTime.now();

    private String cardBrand;

    private double balanceAmount;

    private double creditLimit;

    private double totalConsumption;

    private boolean active;

    private String chargeDay;
}
