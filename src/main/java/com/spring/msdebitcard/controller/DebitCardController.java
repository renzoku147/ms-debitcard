package com.spring.msdebitcard.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.msdebitcard.entity.CreditTransaction;
import com.spring.msdebitcard.entity.DebitCard;
import com.spring.msdebitcard.entity.DebitCardTransaction;
import com.spring.msdebitcard.service.DebitCardService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RefreshScope
@RestController
@RequestMapping("/debitCard")
@Slf4j
public class DebitCardController {
	
	@Autowired
	DebitCardService debitCardService;
	
	@GetMapping("list")
    public Flux<DebitCard> findAll(){
        return debitCardService.findAll();
    }

    @GetMapping("/find/{id}")
    public Mono<DebitCard> findById(@PathVariable String id){
        return debitCardService.findById(id);
    }

    @GetMapping("/findCreditCardByCardNumber/{cardNumber}")
    public Mono<DebitCard> findCreditCardByCardNumber(@PathVariable String cardNumber){
        return debitCardService.findCreditCardByCardNumber(cardNumber);
    }
    
    @GetMapping("/findByCustomerId/{idcustomer}")
    public Flux<DebitCard> findByCustomerId(@PathVariable String idcustomer){
        return debitCardService.findByCustomerId(idcustomer);
    }
    
    @PutMapping("/checkUpdateBalanceDebitCard/{cardNumber}")
    public Mono<DebitCardTransaction> checkUpdateBalanceDebitCard(@PathVariable String cardNumber,@RequestBody DebitCardTransaction debitCardTransaction){
    	log.info(">>> checkUpdateBalanceDebitCard <<<");
    	log.info("cardNumber : "+ cardNumber);
    	log.info("creditTransaction : "+ debitCardTransaction.getTransactionAmount());
        return debitCardService.checkUpdateBalanceDebitCard(cardNumber, debitCardTransaction);
    }
    
    @PostMapping("/create")
    public Mono<ResponseEntity<DebitCard>> create(@Valid @RequestBody DebitCard debitCard){
    	
    	log.info("Metodo create DebitCard!");
    	return debitCardService.verifyCardNumber(debitCard.getCardNumber())
    			.filter(optCardNumber -> {
					log.info("Optional CardNumber : " + optCardNumber.isEmpty());
					return optCardNumber.isEmpty();
				})
    			.flatMap(optCardNumber -> debitCardService.findCustomer(debitCard.getCustomer().getId())
				    					.flatMap(customer -> debitCardService.findBankAccount(Flux.fromIterable(debitCard.getAccounts()), customer)
						    					.filter(optBankAccount -> {
						    						log.info("Primer filtro, reviso que no tenga paramentros repetidos");
						    						return debitCard.getAccounts().stream().map(x -> x.getAccountNumber()).distinct().count() == debitCard.getAccounts().size() &&
						    								debitCard.getAccounts().stream().map(x -> x.getPriority()).distinct().count() == debitCard.getAccounts().size();
						    					})									   
												.filter(optBankAccount -> {
													log.info("Segundo filter, reviso si las cuentas bancarias existen : " + optBankAccount);
													return optBankAccount.isPresent();
												})
												.count()
												.filter(count -> {
													log.info("Contador : " + count);
													log.info("Tercer filter, verifica si las todas la cuentas ingresadas fueron validas : " + (count == debitCard.getAccounts().size()));
													return count == debitCard.getAccounts().size();
												})
												.flatMap(count -> {
														debitCard.setCustomer(customer);
														return debitCardService.create(debitCard)
																.flatMap(debitCardCreated -> debitCardService.updateBankAccount(Flux.fromIterable(debitCard.getAccounts()), debitCardCreated)	
																							.collectList()
																							.map(cl -> new ResponseEntity<>(debitCardCreated, HttpStatus.CREATED)));
															}
													)
										)
    					)
    			.defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
    
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return debitCardService.delete(id)
                .filter(deleteDebitCard -> deleteDebitCard)
                .map(deleteFixedTerm -> new ResponseEntity<>("Customer Deleted", HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
