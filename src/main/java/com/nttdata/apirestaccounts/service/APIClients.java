package com.nttdata.apirestaccounts.service;

import com.nttdata.apirestaccounts.dto.CreditDto;
import com.nttdata.apirestaccounts.dto.CustomerDto;
import com.nttdata.apirestaccounts.dto.DebitCardDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface APIClients {
    Mono<CustomerDto> findByCustomer(String customerId);
    Mono<DebitCardDto> updateDebitCard(String idDebitCard, DebitCardDto debitCard);
    Mono<DebitCardDto> findDebitCardByCustomer(String customerId);
    Flux<CreditDto> findCreditByCustomer(String customerId);
    Mono<Boolean> expiredDebtCreditsByCustomer(String customerId);
    Mono<Boolean> expiredDebtCreditCardsByCustomer(String customerId);

}
