package com.nttdata.apirestaccounts.service;

import com.nttdata.apirestaccounts.dto.CreditDto;
import com.nttdata.apirestaccounts.dto.CustomerDto;
import com.nttdata.apirestaccounts.dto.DebitCardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class APIClientsImpl implements APIClients{

    @Autowired
    private WebClient webClient;

    @Value("${config.base.enpoint.credits}")
    private String urlCredits;

    @Value("${config.base.enpoint.debitcards}")
    private String urlDebitCards;

    @Value("${config.base.enpoint.creditcards}")
    private String urlCreditCards;

    @Override
    public Mono<CustomerDto> findByCustomer(String customerId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", customerId);
        return webClient.get().uri(urlCredits + "/{id}", params).accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response.bodyToMono(CustomerDto.class));
    }

    @Override
    public Mono<DebitCardDto> updateDebitCard(String idDebitCard, DebitCardDto debitCard) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", idDebitCard);
        return webClient.put().uri(urlDebitCards + "/{id}", params)
                .body(Mono.just(debitCard), DebitCardDto.class)
                .retrieve()
                .bodyToMono(DebitCardDto.class);
    }

    @Override
    public Mono<DebitCardDto> findDebitCardByCustomer(String customerId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", customerId);
        return webClient.get().uri(urlDebitCards + "/customer/{id}", params).accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response.bodyToMono(DebitCardDto.class));
    }

    @Override
    public Flux<CreditDto> findCreditByCustomer(String customerId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", customerId);
        return webClient.get().uri(urlCredits + "/customer/{id}", params).accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(CreditDto.class));
    }

    @Override
    public Mono<Boolean> expiredDebtCreditsByCustomer(String customerId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("customerId", customerId);
        return webClient.get().uri(urlCredits + "/expiredDebt/customer/{customerId}", params).accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(CreditDto.class))
                .collectList()
                .flatMap(lst -> {
                    if (!lst.isEmpty()) {
                        return Mono.just(true);
                    }
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Boolean> expiredDebtCreditCardsByCustomer(String customerId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("customerId", customerId);
        return webClient.get().uri(urlCreditCards + "/expiredDebt/customer/{customerId}", params).accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(CreditDto.class))
                .collectList()
                .flatMap(lst -> {
                    if (!lst.isEmpty()) {
                        return Mono.just(true);
                    }
                    return Mono.just(false);
                });
    }
}
