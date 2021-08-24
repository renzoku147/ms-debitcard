package com.spring.msdebitcard.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.spring.msdebitcard.entity.DebitCard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {
	
	Mono<DebitCard> findByCardNumber(String cardNumber);
	
	Flux<DebitCard> findByCustomerId(String idcustomer);
	
}
