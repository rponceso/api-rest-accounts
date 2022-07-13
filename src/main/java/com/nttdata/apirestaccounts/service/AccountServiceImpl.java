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

    private Mono<Account> existAccount(Account account) {

        Flux<Account> fluxAccount = getByCustomer_Id(account.getCustomer().getId());

//        Flux<Account> fluxAccountCustomerType = findByCustomer_IdAndAccountType_Code(account.getCustomer().getId(), account.getCustomer().getClientType())

        //Un cliente personal solo puede tener un máximo de una cuenta de ahorro, una cuenta corriente o cuentas a plazo fijo.


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
                            }else{
                                log.info("Customer of type: " + account.getCustomer().getClientType());
                                log.info("Saved bank account of type: " + account.getAccountType().getCode());
                                return repository.save(account);
                            }

                        }

                        return Mono.just(account);
                    }
                    return Mono.empty();
                }).onErrorMap(ex -> new Exception(ex.getMessage()));
        // .thenReturn(account);


//                .switchIfEmpty(Mono.error(new Exception("There is no customer for this bank account")))
//                .switchIfEmpty(;throw new Exception("");
//                .collectList()

//                .switchIfEmpty(Mono.error(new AccountException("ssss")));

  /*
                /*.flatMap(lst -> {
                    if (lst == null) {
                        log.info("esta nula");
                    } else {
                        if (lst.isEmpty()) {
                            log.info("esta vacio");
                            return Mono.empty();
                        } else {
                            log.info("esta llena");
                        }
                    }
                    return Mono.just(lst.get(0));
                });*/


             /*   .flatMap(lst -> {
                    if (lst == null) {
                        log.info("esta nula");
                    } else {
                        if (lst.isEmpty()) {
                            log.info("esta vacio");
                        } else {
                            log.info("esta llean");
                        }
                    }

//                    return Mono.just(account);
                    return null;
                });*/
//                .switchIfEmpty(Mono.error(new AccountException("ssss")))
  /*              .defaultIfEmpty(account)
                .filter(acc -> acc.getCustomer().getCustomerType().equals("P"))
                .filter(acc -> acc.getAccountType().getCode().equalsIgnoreCase("CAH")
                            || acc.getAccountType().getCode().equalsIgnoreCase("CC")
                            || acc.getAccountType().getCode().equalsIgnoreCase("CPF"))
                .collectList()
                .flatMap(lstFilter->{

                    if(lstFilter.isEmpty()){
                        log.info("Lista vacia");
                    }else{
//                        a=lstFilter.get(0);
                        if(lstFilter.size()==1){
                            log.info("lista con unico elemento");
                        }
                    }
                    //return a;
                    return Mono.empty();
                }).thenReturn(account);*/


    }

    private Flux<Account> existAccountByCustomerIdAndAccountType(Account account) {
        return repository.findByCustomer_IdAndAccountType_Code(account.getId(), account.getAccountType().getCode())
                .switchIfEmpty(Mono.error(new CustomerException("Que paso")));
    }

/*    private Flux<Account> existCustomerAccount(Account account) {
        return existCustomerTypeAccount(account)
                .flatMap(acc-)


        return repository.findByCustomer_Id(account.getCustomer().getId())
                .flatMap(acc->{
                    if(acc.getCustomer().getCustomerType().equals("P")){
                        if(acc.getAccountType().getCode().equals("CAH")
                            || acc.getAccountType().getCode().equals("CC")
                            || acc.getAccountType().getCode().equals("CPF")){

                        }
                    }
                })
                .switchIfEmpty(Mono.error(new CustomerException("Que paso aqui")));
    }*/


    private Mono<Account> validateSaveAccount(Account account) {

        Mono<Account> monoAccount = existAccount(account);

        return monoAccount;


//      Mono<Account> monoAccountCustomer = repository.findByCustomer_IdAndAccountType_Code(account.getCustomer().getId(), account.getAccountType().getCode());

//      Mono<Account> monoAccount = Mono.just(account);

        /*
        *     return resourceService.findByFilter(resourceName)
            .flatMap(res -> service1.findByFilter(resourceName)
                    .switchIfEmpty(service2.findByFilter(resourceName))
                    .switchIfEmpty(service3.findByFilter(resourceName))
                    .doOnEach(stringSignal -> res.setResource(stringSignal.get()))
                    .thenReturn(res));
        *
        * */




/*        return monoAccount
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
                                })));*/

    }


}
