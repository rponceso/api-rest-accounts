package com.nttdata.apirestaccounts.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerException extends RuntimeException {

    private String message;

}
