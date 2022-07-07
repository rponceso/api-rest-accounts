/**
 * Implementation Interface Service Account
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.service;

import com.nttdata.apirestaccounts.dto.FilterDto;
import com.nttdata.apirestaccounts.exception.CustomerException;
import com.nttdata.apirestaccounts.model.Account;
import com.nttdata.apirestaccounts.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private APIClients apiClients;

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public Mono<Account> create(Account account) {
        return validateSaveAccount(account);
    }

    @Override
    public Mono<Account> update(Account account) {
        return repository.save(account);
    }

    @Override
    public Flux<Account> listAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Account> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Account> getByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber);
    }

    @Override
    public Flux<Account> getByCustomer_Id(String customerId) {
        return repository.findByCustomer_Id(customerId);
    }

    @Override
    public Flux<Account> findByCreationDateBetween(FilterDto filter) {
        return repository.findByCreationDateBetween(filter.getStartDate(), filter.getEndDate());
    }

    private Mono<Account> validateSaveAccount(Account account) {

        Mono<Account> monoAccountCustomer = repository.findByCustomer_IdAndAccountType_Code(account.getCustomer().getId(), account.getAccountType().getCode());

        Mono<Account> monoAccount = Mono.just(account);

        return monoAccount
                .flatMap(acc -> apiClients.expiredDebtCreditsByCustomer(acc.getCustomer().getId())
                        .flatMap(expiredCredits -> apiClients.expiredDebtCreditCardsByCustomer(acc.getCustomer().getId())
                                .flatMap(expiredCreditCards -> {
                                    if (expiredCredits || expiredCreditCards) {
                                        log.info("There are overdue debts will not be inserted");
                                        return Mono.just(acc);
                                    } else {
                                        log.info("I can insert because there are no overdue debts");
                                        account.setBalance(account.getAmount());
                                        if (acc.getCustomer().getCustomerType().equalsIgnoreCase("P")) {
                                            log.info("The customer is personal");
                                            return monoAccountCustomer
                                                    .switchIfEmpty(repository.save(account)).flatMap(accSaved -> {
                                                        return apiClients.findDebitCardByCustomer(accSaved.getCustomer().getId())
                                                                .flatMap(debitCard -> {
                                                                    List<Account> accounts = new ArrayList<>();
                                                                    accounts = debitCard.getAccounts();
                                                                    accounts.add(accSaved);
                                                                    debitCard.setAccounts(accounts);
                                                                    return apiClients.updateDebitCard(debitCard.getId(), debitCard);
                                                                }).flatMap(udc -> Mono.just(accSaved));
                                                    });
                                        } else if (acc.getCustomer().getCustomerType().equalsIgnoreCase("B")) {
                                            log.info("The customer is Business");

                                            if (account.getAccountType().getCode().equalsIgnoreCase("CC")) {
                                                log.info("The account type is: " + account.getAccountType().getCode());
                                                log.info("This type of account is allowed for the type of customer Business");
                                                return repository.save(account).flatMap(accSaved -> {
                                                    return apiClients.findDebitCardByCustomer(accSaved.getCustomer().getId())
                                                            .flatMap(debitCard -> {
                                                                List<Account> accounts = new ArrayList<>();
                                                                accounts = debitCard.getAccounts();
                                                                accounts.add(accSaved);
                                                                debitCard.setAccounts(accounts);
                                                                return apiClients.updateDebitCard(debitCard.getId(), debitCard);
                                                            }).flatMap(udc -> Mono.just(accSaved));
                                                });
                                            } else {
                                                log.info("The account type is: " + account.getAccountType().getCode());
                                                log.info("This type of account not is allowed for the type of customer Business");
                                            }
                                        } else {
                                            return Mono.error(new CustomerException("The type of Customer is incorrect"));
                                        }


                                    }
                                    return Mono.just(account);
                                })));

    }


}
