package com.nttdata.apirestaccounts.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDto {

    private LocalDate startDate;
    private LocalDate endDate;
}
