/**
 * Interface Service Account
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.service;

import com.nttdata.apirestaccounts.dto.FilterDto;
import com.nttdata.apirestaccounts.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<Account> create(Account account);

    Mono<Account> update(Account account);

    Flux<Account> listAll();

    Mono<Account> getById(String id);

    Mono<Account> getByAccountNumber(String accountNumber);

    Flux<Account> getByCustomer_Id(String customerId);

    Flux<Account> findByCreationDateBetween(FilterDto filtroDto);

}
