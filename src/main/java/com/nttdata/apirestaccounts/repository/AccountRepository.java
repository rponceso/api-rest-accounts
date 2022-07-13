/**
 * Repository that stores Account information
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.repository;

import com.nttdata.apirestaccounts.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

    Mono<Account> findByAccountNumber(String accountNumber);

    Flux<Account> findByCustomer_Id(String customerId);

    Flux<Account> findByCustomer_IdAndAccountType_Code(String customerId, String codeAccountType);

    Flux<Account> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

//    Flux<Account> findByCreationDateLessThanEqualAndGreaterThanEqual(LocalDate endDate, LocalDate starDate);

    //findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual

    /*
    * LessThan/LessThanEqual
LessThan - findByEndLessThan … where x.start< ?1

LessThanEqual findByEndLessThanEqual … where x.start <= ?1

GreaterThan/GreaterThanEqual
GreaterThan - findByStartGreaterThan … where x.end> ?1

GreaterThanEqual - findByStartGreaterThanEqual … where x.end>= ?1
    *
    * */
}
