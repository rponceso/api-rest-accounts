package com.nttdata.apirestaccounts.exception;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerException extends RuntimeException {

    private String message;

}
