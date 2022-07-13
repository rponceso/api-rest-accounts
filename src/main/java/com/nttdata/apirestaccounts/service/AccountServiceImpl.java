/**
 * Implementation Interface Service Account
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.service;

import com.nttdata.apirestaccounts.dto.CustomerDto;
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
    public Flux<Account> findByCustomer_IdAndAccountType_Code(String customerId, String codeAccountType) {
        return repository.findByCustomer_IdAndAccountType_Code(customerId, codeAccountType);
    }

    @Override
    public Flux<Account> findByCreationDateBetween(FilterDto filter) {
        return repository.findByCreationDateBetween(filter.getStartDate(), filter.getEndDate());
    }

    private Mono<Account> validateSaveAccount(Account account) {

        Mono<CustomerDto> customerDtoMono = apiClients.findByCustomer(account.getCustomer().getId());

        Flux<Account> fluxAccount = getByCustomer_Id(account.getCustomer().getId());

        return customerDtoMono
                .flatMap(cust -> {
                    if (cust != null) {
                        return apiClients.expiredDebtCreditsByCustomer(cust.getId())
                                .flatMap(expiredCredits -> apiClients.expiredDebtCreditCardsByCustomer(cust.getId())
                                        .flatMap(expiredCreditCards -> {
                                            if (expiredCredits || expiredCreditCards) {
                                                log.info("There are overdue debts will not be inserted");
                                                throw new RuntimeException("There are overdue debts will not be inserted");
                                            } else {
                                                log.info("I can insert because there are no overdue debts");
                                                log.info("We will search if the customer has registered accounts");
                                                return fluxAccount
                                                        .collectList()
                                                        .flatMap(lstAccounts -> {
                                                            if (lstAccounts.isEmpty()) {
                                                                log.info("There is no customer for this bank account");
                                                                if (account.getCustomer().getClientType().equalsIgnoreCase("P")) {
                                                                    if (account.getAccountType().getCode().equalsIgnoreCase("CAH")
                                                                            || account.getAccountType().getCode().equalsIgnoreCase("CC")
                                                                            || account.getAccountType().getCode().equalsIgnoreCase("CPF")) {
                                                                        return repository.save(account);
                                                                    }
                                                                } else if (account.getCustomer().getClientType().equalsIgnoreCase("B")) {
                                                                    if (account.getAccountType().getCode().equalsIgnoreCase("CC")) {
                                                                        return repository.save(account);
                                                                    } else {
                                                                        throw new RuntimeException("Solo esta permitido cuenta corriente");
                                                                    }
                                                                }
                                                            } else {
                                                                log.info("there is a customer for this bank account");
                                                                int countCAH = 0;
                                                                int countCC = 0;
                                                                int countCPF = 0;

                                                                for (Account acc : lstAccounts) {
                                                                    if (acc.getAccountType().getCode().equalsIgnoreCase("CAH")) {
                                                                        countCAH++;
                                                                    }

                                                                    if (acc.getAccountType().getCode().equalsIgnoreCase("CC")) {
                                                                        countCC++;
                                                                    }

                                                                    if (acc.getAccountType().getCode().equalsIgnoreCase("CPF")) {
                                                                        countCPF++;
                                                                    }
                                                                }

                                                                if (account.getCustomer().getClientType().equalsIgnoreCase("P")) {
                                                                    if ((account.getAccountType().getCode().equalsIgnoreCase("CAH") && countCAH == 0)
                                                                            || (account.getAccountType().getCode().equalsIgnoreCase("CC") && countCC == 0)
                                                                            || (account.getAccountType().getCode().equalsIgnoreCase("CPF") && countCPF == 0)) {

                                                                        log.info("Customer of type: " + account.getCustomer().getClientType());
                                                                        log.info("Saved bank account of type: " + account.getAccountType().getCode());
                                                                        return repository.save(account);
                                                                    } else {
                                                                        if ((account.getAccountType().getCode().equalsIgnoreCase("CAH") && countCAH == 1)
                                                                                || (account.getAccountType().getCode().equalsIgnoreCase("CC") && countCC == 1)
                                                                                || (account.getAccountType().getCode().equalsIgnoreCase("CPF") && countCPF == 1)) {
                                                                            throw new RuntimeException("A personal customer can only have a maximum of one savings account, one current account or fixed-term accounts");
                                                                        }
                                                                    }
                                                                } else if (account.getCustomer().getClientType().equalsIgnoreCase("B")) {
                                                                    if (!account.getAccountType().getCode().equalsIgnoreCase("CC")) {
                                                                        throw new RuntimeException("A business customer cannot have a savings or time deposit account but multiple current accounts");
                                                                    } else {
                                                                        log.info("Customer of type: " + account.getCustomer().getClientType());
                                                                        log.info("Saved bank account of type: " + account.getAccountType().getCode());
                                                                        return repository.save(account);
                                                                    }
                                                                }

                                                            }
                                                            return Mono.empty();
                                                        }).onErrorMap(ex -> new Exception(ex.getMessage()));

                                            }
                                        }).onErrorMap(ex -> new Exception(ex.getMessage())));
                    } else {
                        throw new RuntimeException("The customer does not exist");
                    }
                }).onErrorMap(ex -> new Exception(ex.getMessage()));

    }

    private Flux<Account> existAccountByCustomerIdAndAccountType(Account account) {
        return repository.findByCustomer_IdAndAccountType_Code(account.getId(), account.getAccountType().getCode())
                .switchIfEmpty(Mono.error(new CustomerException("Que paso")));
    }


}
