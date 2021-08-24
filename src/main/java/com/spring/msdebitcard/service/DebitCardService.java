package com.spring.msdebitcard.service;


import java.util.Optional;

import com.spring.msdebitcard.entity.Accounts;
import com.spring.msdebitcard.entity.BankAccount;
import com.spring.msdebitcard.entity.Card;
import com.spring.msdebitcard.entity.CreditTransaction;
import com.spring.msdebitcard.entity.Customer;
import com.spring.msdebitcard.entity.DebitCard;
import com.spring.msdebitcard.entity.DebitCardTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DebitCardService {
	
	Mono<DebitCard> create(DebitCard t);

    Flux<DebitCard> findAll();

    Mono<DebitCard> findById(String id);

    Mono<DebitCard> update(DebitCard t);

    Mono<Boolean> delete(String t);
    
    Mono<Customer> findCustomer(String id);
    
    Mono<DebitCard> findCreditCardByCardNumber(String cardNumber);
    
    Flux<Optional<BankAccount>> findBankAccount(Flux<Accounts> cardNumber, Customer customer);
    
    Mono<Optional<Card>> verifyCardNumber(String t);
    
    Flux<Optional<BankAccount>> updateBankAccount(Flux<Accounts> cardNumber, DebitCard debitCard);
    
    Mono<DebitCardTransaction> checkUpdateBalanceDebitCard(String cardNumber, DebitCardTransaction creditTransaction);
    
    Flux<DebitCard> findByCustomerId(String idcustomer);
    
}
