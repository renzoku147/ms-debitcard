package com.spring.msdebitcard.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.spring.msdebitcard.entity.Accounts;
import com.spring.msdebitcard.entity.BankAccount;
import com.spring.msdebitcard.entity.Card;
import com.spring.msdebitcard.entity.CreditCard;
import com.spring.msdebitcard.entity.CurrentAccount;
import com.spring.msdebitcard.entity.Customer;
import com.spring.msdebitcard.entity.DebitCard;
import com.spring.msdebitcard.entity.DebitCardTransaction;
import com.spring.msdebitcard.entity.FixedTerm;
import com.spring.msdebitcard.entity.SavingAccount;
import com.spring.msdebitcard.repository.DebitCardRepository;
import com.spring.msdebitcard.service.DebitCardService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DebitCardServiceImpl implements DebitCardService{

	@Autowired
	DebitCardRepository debitCardRepository;
	
    WebClient webClientCurrent = WebClient.create("http://localhost:8887/ms-current-account/currentAccount");

    WebClient webClientFixed = WebClient.create("http://localhost:8887/ms-fixed-term/fixedTerm");

    WebClient webClientSaving = WebClient.create("http://localhost:8887/ms-saving-account/savingAccount");
    
    WebClient webClientCustomer = WebClient.create("http://localhost:8887/ms-customer/customer");
    
    WebClient webClientCreditCard = WebClient.create("http://localhost:8887/ms-creditcard/creditCard");
	
	@Override
	public Mono<DebitCard> create(DebitCard t) {
		t.setDate(LocalDateTime.now());
		return debitCardRepository.save(t);
	}

	@Override
	public Flux<DebitCard> findAll() {
		return debitCardRepository.findAll();
	}

	@Override
	public Mono<DebitCard> findById(String id) {
		return debitCardRepository.findById(id);
	}

	@Override
	public Mono<DebitCard> update(DebitCard t) {
		return debitCardRepository.save(t);
	}

	@Override
	public Mono<Boolean> delete(String t) {
		return debitCardRepository.findById(t)
                .flatMap(dc -> debitCardRepository.delete(dc).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
	}
	
	@Override
	public Mono<Customer> findCustomer(String id) {
		return webClientCustomer.get().uri("/find/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Customer.class);
	}

	@Override
	public Flux<Optional<BankAccount>> findBankAccount(Flux<Accounts> accountNumbers, Customer customer) {
		log.info("findBankAccount Inicio >>> ");
		return accountNumbers.flatMap(acc -> webClientCurrent.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
							                .accept(MediaType.APPLICATION_JSON)
							                .retrieve()
							                .bodyToMono(CurrentAccount.class)
							                .filter(currentAccount -> currentAccount.getDebitCard() == null && customer.getId().equals(currentAccount.getCustomer().getId()))
							                .map(currentAccount -> {
							                    System.out.println("Encontro currentAccount > " + currentAccount.getId());
							                    return List.of(Optional.of((BankAccount)currentAccount));
						                    })
							                .switchIfEmpty(webClientFixed.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
					                                .accept(MediaType.APPLICATION_JSON)
					                                .retrieve()
					                                .bodyToMono(FixedTerm.class)
					                                .filter(fixedTerm -> fixedTerm.getDebitCard() == null && customer.getId().equals(fixedTerm.getCustomer().getId()))
					                                .map(fixedTerm -> {
					                                    System.out.println("Encontro fixedTerm > " + fixedTerm.getId());
					                                    return List.of(Optional.of((BankAccount)fixedTerm));
					                                })
					                                .switchIfEmpty(webClientSaving.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
					                                                .accept(MediaType.APPLICATION_JSON)
					                                                .retrieve()
					                                                .bodyToMono(SavingAccount.class)
					                                                .filter(savingAccount -> savingAccount.getDebitCard() == null && customer.getId().equals(savingAccount.getCustomer().getId()))
					                                                .map(savingAccount -> {
					                                                    System.out.println("Encontro savingAccount > " + savingAccount.getId());
					                                                    return List.of(Optional.of((BankAccount)savingAccount));
					                                                }))
					                                				.defaultIfEmpty(List.of(Optional.empty()))
					                                )
							                .flatMapMany(Flux::fromIterable)
					                );
	}

	@Override
	public Mono<Optional<Card>> verifyCardNumber(String cardNumber) {
		return debitCardRepository.findByCardNumber(cardNumber)
				.map(dc -> Optional.of((Card)dc))
				.switchIfEmpty(webClientCreditCard.get().uri("/findCreditCardByCardNumber/{cardNumber}", cardNumber)
	                    .accept(MediaType.APPLICATION_JSON)
	                    .retrieve()
	                    .bodyToMono(CreditCard.class)
	                    .map(creditCard -> {
	                        System.out.println("Encontro creditCard > " + creditCard.getId());
	                        return Optional.of((Card)creditCard);
	                    })
	                    .defaultIfEmpty(Optional.empty()));
	}


	@Override
	public Mono<DebitCard> findCreditCardByCardNumber(String cardNumber) {
		return debitCardRepository.findByCardNumber(cardNumber);
	}
	
	@Override
	public Flux<Optional<BankAccount>> updateBankAccount(Flux<Accounts> accountNumbers, DebitCard debitCard) {
		return accountNumbers.flatMap(acc -> webClientCurrent.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CurrentAccount.class)
                .flatMap(current -> {
                				current.setDebitCard(debitCard);
                				log.info("Encontro CurrentAccount " + current.getAccountNumber());
                				return webClientCurrent.put().uri("/update")
			                        .accept(MediaType.APPLICATION_JSON)
			                        .syncBody(current)
			                        .retrieve()
			                        .bodyToMono(CurrentAccount.class)
		                        	.map(currentUp -> {
		                        		log.info("Actualizo CurrentAccount " + currentUp);
		                        		return List.of(Optional.of((BankAccount)currentUp));
		                        	});
		                        	}
	    			)
	                .switchIfEmpty(webClientFixed.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
				                .accept(MediaType.APPLICATION_JSON)
				                .retrieve()
				                .bodyToMono(FixedTerm.class)
				                .flatMap(fixed -> {
				                				fixed.setDebitCard(debitCard);
				                				log.info("Encontro FixedTerm " + fixed.getAccountNumber() );
				                				return webClientFixed.put().uri("/update")
							                        .accept(MediaType.APPLICATION_JSON)
							                        .syncBody(fixed)
							                        .retrieve()
							                        .bodyToMono(FixedTerm.class)
						                        	.map(fixedUp -> {
						                        		fixedUp.setDebitCard(debitCard);
						                        		log.info("Actualizo FixedTerm " + fixedUp );
						                        			return List.of(Optional.of((BankAccount)fixedUp));
						                        		});
				                		}
	                        	)
				                .switchIfEmpty(webClientSaving.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
							                .accept(MediaType.APPLICATION_JSON)
							                .retrieve()
							                .bodyToMono(SavingAccount.class)
							                .flatMap(saving -> {
							                				saving.setDebitCard(debitCard);
						                					log.info("Encontro SavingAccount " + saving.getAccountNumber());
							                				return webClientSaving.put().uri("/update")
										                        .accept(MediaType.APPLICATION_JSON)
										                        .syncBody(saving)
										                        .retrieve()
										                        .bodyToMono(SavingAccount.class)
									                        	.map(savingUp -> {
									                        		log.info("Actualizo SavingAccount " + savingUp );
									                        			return List.of(Optional.of((BankAccount)savingUp));
									                        		})
									                        	.defaultIfEmpty(List.of(Optional.empty()));
							                		}
				                        	)
		                		)
	        		)
	                .flatMapMany(Flux::fromIterable)
        );
		
	}

	@Override
	public Mono<DebitCardTransaction> checkUpdateBalanceDebitCard(String cardNumber, DebitCardTransaction debitCardTransaction) {
		log.info(">>> Implement Method DebitCard <<<");
		DebitCardTransaction amountInitial = DebitCardTransaction.builder().transactionAmount(debitCardTransaction.getTransactionAmount()).build();
		return debitCardRepository.findByCardNumber(cardNumber)
				.map(dc -> dc.getAccounts())
				.flatMapIterable(list -> list)
				.sort((x,y) -> x.getPriority() - y.getPriority())
				.concatMap(acc -> webClientCurrent.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
					                .accept(MediaType.APPLICATION_JSON)
					                .retrieve()
					                .bodyToMono(CurrentAccount.class)
					                .filter(ca -> amountInitial.getTransactionAmount()>0)
					                .map(ca -> {
					                	log.info("Encontro CurrentAccount > " + ca.getAccountNumber() +" > " + ca.getBalance() + " | " + "Saldo CreditTransaction > " + amountInitial.getTransactionAmount());
					                	if(amountInitial.getTransactionAmount()>ca.getBalance()) {
					                		amountInitial.setTransactionAmount(amountInitial.getTransactionAmount()-ca.getBalance());
		                					ca.setBalance(0.0);
		                				}else {
		                					ca.setBalance(ca.getBalance()-amountInitial.getTransactionAmount());
		                					amountInitial.setTransactionAmount(0.0);
		                				}
					                	log.info("Balance CurrentAccount > " + ca.getBalance() + " | " + "Saldo CreditTransaction > " + amountInitial.getTransactionAmount());
					                	return Optional.of((BankAccount)ca);
					                })
					                .switchIfEmpty(webClientFixed.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
								                .accept(MediaType.APPLICATION_JSON)
								                .retrieve()
								                .bodyToMono(FixedTerm.class)
								                .filter(ca -> amountInitial.getTransactionAmount()>0)
								                .map(ft -> {
								                	log.info("Encontro FixedTerm > "+ ft.getAccountNumber() +" > " + ft.getBalance() + " | " + "Saldo CreditTransaction > " + amountInitial.getTransactionAmount());
					                				if(amountInitial.getTransactionAmount()>ft.getBalance()) {
					                					amountInitial.setTransactionAmount(amountInitial.getTransactionAmount()-ft.getBalance());
					                					ft.setBalance(0.0);
					                				}else {
					                					ft.setBalance(ft.getBalance()-amountInitial.getTransactionAmount());
					                					amountInitial.setTransactionAmount(0.0);
					                				}
					                				log.info("Balance FixedTerm > " + ft.getBalance() + " | " + "Saldo CreditTransaction > " + amountInitial.getTransactionAmount());
								                	return Optional.of((BankAccount)ft);
								                })
								                .switchIfEmpty(webClientSaving.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
											                .accept(MediaType.APPLICATION_JSON)
											                .retrieve()
											                .bodyToMono(SavingAccount.class)
											                .filter(ca -> amountInitial.getTransactionAmount()>0)
											                .map(sa -> {
											                	log.info("Encontro SavingAccount > "+ sa.getAccountNumber()+ " > " + sa.getBalance() + " | " + "Saldo CreditTransaction > " + amountInitial.getTransactionAmount());
							                					if(amountInitial.getTransactionAmount()>sa.getBalance()) {
							                						amountInitial.setTransactionAmount(amountInitial.getTransactionAmount()-sa.getBalance());
								                					sa.setBalance(0.0);
								                				}else {
								                					sa.setBalance(sa.getBalance()-amountInitial.getTransactionAmount());
								                					amountInitial.setTransactionAmount(0.0);
								                				}
							                					log.info("Balance SavingAccount > " + sa.getBalance() + " | " + "Saldo CreditTransaction > " + amountInitial.getTransactionAmount());
											                	return Optional.of((BankAccount)sa);
											                })
											                .defaultIfEmpty(Optional.empty())
					                			)
								                
					        		)
                )
				.filter(opt -> amountInitial.getTransactionAmount()==0) // VERIFICO SI EL SALDO DE LA CUENTAS CUBRE EL MONTO DE LA TRANSACCION
				.flatMap(monoAcc -> debitCardRepository.findByCardNumber(cardNumber)
									.map(dc -> dc.getAccounts())
									.flatMapIterable(list -> {
										log.info("---------------Paso filtro----->" + debitCardTransaction.getTransactionAmount());
										Collections.sort(list, (x,y) -> x.getPriority() - y.getPriority());
										return list;
									})
									.sort((x,y) -> x.getPriority() - y.getPriority())
									.concatMap(acc -> webClientCurrent.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
							                .accept(MediaType.APPLICATION_JSON)
							                .retrieve()
							                .bodyToMono(CurrentAccount.class)
							                .filter(ca -> debitCardTransaction.getTransactionAmount()>0)
							                .flatMap(current -> {
									                	log.info("[UPDATE] Encontro CurrentAccount > " + current.getAccountNumber() +" > " + current.getBalance() + " | " + "Saldo CreditTransaction > " + debitCardTransaction.getTransactionAmount());
									                	if(debitCardTransaction.getTransactionAmount()>current.getBalance()) {
						                					debitCardTransaction.setTransactionAmount(debitCardTransaction.getTransactionAmount()-current.getBalance());
						                					current.setBalance(0.0);
						                				}else {
						                					current.setBalance(current.getBalance()-debitCardTransaction.getTransactionAmount());
						                					debitCardTransaction.setTransactionAmount(0.0);
						                				}
							                				return webClientCurrent.put().uri("/update")
										                        .accept(MediaType.APPLICATION_JSON)
										                        .syncBody(current)
										                        .retrieve()
										                        .bodyToMono(CurrentAccount.class)
									                        	.map(currentUp -> {
									                        		log.info("[UPDATE] Balance CurrentAccount > " + currentUp.getBalance() + " | " + "Saldo CreditTransaction > " + debitCardTransaction.getTransactionAmount());
									                        		return List.of(Optional.of((BankAccount)currentUp));
									                        	});
									                        	}
								    			)
								                .switchIfEmpty(webClientFixed.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
											                .accept(MediaType.APPLICATION_JSON)
											                .retrieve()
											                .bodyToMono(FixedTerm.class)
											                .filter(ft -> debitCardTransaction.getTransactionAmount()>0)
											                .flatMap(fixed -> {
														                	log.info("[UPDATE] Encontro FixedTerm > "+ fixed.getAccountNumber() +" > " + fixed.getBalance() + " | " + "Saldo CreditTransaction > " + debitCardTransaction.getTransactionAmount());
											                				if(debitCardTransaction.getTransactionAmount()>fixed.getBalance()) {
											                					debitCardTransaction.setTransactionAmount(debitCardTransaction.getTransactionAmount()-fixed.getBalance());
											                					fixed.setBalance(0.0);
											                				}else {
											                					fixed.setBalance(fixed.getBalance()-debitCardTransaction.getTransactionAmount());
											                					debitCardTransaction.setTransactionAmount(0.0);
											                				}
											                				return webClientFixed.put().uri("/update")
														                        .accept(MediaType.APPLICATION_JSON)
														                        .syncBody(fixed)
														                        .retrieve()
														                        .bodyToMono(FixedTerm.class)
													                        	.map(fixedUp -> {
													                        			log.info("[UPDATE] Balance FixedTerm > " + fixedUp.getBalance() + " | " + "Saldo CreditTransaction > " + debitCardTransaction.getTransactionAmount());
													                        			return List.of(Optional.of((BankAccount)fixedUp));
													                        		});
											                		}
								                        	)
											                .switchIfEmpty(webClientSaving.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
														                .accept(MediaType.APPLICATION_JSON)
														                .retrieve()
														                .bodyToMono(SavingAccount.class)
														                .filter(sa -> debitCardTransaction.getTransactionAmount()>0)
														                .flatMap(saving -> {
																	                	log.info("[UPDATE] Encontro SavingAccount > "+ saving.getAccountNumber()+ " > " + saving.getBalance() + " | " + "Saldo CreditTransaction > " + debitCardTransaction.getTransactionAmount());
													                					if(debitCardTransaction.getTransactionAmount()>saving.getBalance()) {
														                					debitCardTransaction.setTransactionAmount(debitCardTransaction.getTransactionAmount()-saving.getBalance());
														                					saving.setBalance(0.0);
														                				}else {
														                					saving.setBalance(saving.getBalance()-debitCardTransaction.getTransactionAmount());
														                					debitCardTransaction.setTransactionAmount(0.0);
														                				}
														                				return webClientSaving.put().uri("/update")
																	                        .accept(MediaType.APPLICATION_JSON)
																	                        .syncBody(saving)
																	                        .retrieve()
																	                        .bodyToMono(SavingAccount.class)
																                        	.map(savingUp -> {
																                        			log.info("[UPDATE] Balance SavingAccount > " + savingUp.getBalance() + " | " + "Saldo CreditTransaction > " + debitCardTransaction.getTransactionAmount());
																                        			return List.of(Optional.of((BankAccount)savingUp));
																                        		})
																                        	.defaultIfEmpty(List.of(Optional.empty()));
														                		}
											                        	)
									                		)
								        		)
								                .flatMapMany(Flux::fromIterable)
							        )
						
						)
						.then(Mono.just(debitCardTransaction));
	}

	@Override
	public Flux<DebitCard> findByCustomerId(String idcustomer) {
		return debitCardRepository.findByCustomerId(idcustomer);
	}

	@Override
	public Mono<Optional<BankAccount>> findPrincipalBankAccount(Flux<Accounts> accountNumbers) {
		return 	accountNumbers
				.collectList()
				.map(list -> {
					Collections.sort(list,(a,b)->a.getPriority()-b.getPriority());
					return list.get(0);
				})
				.flatMap(acc -> webClientCurrent.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
		                .accept(MediaType.APPLICATION_JSON)
		                .retrieve()
		                .bodyToMono(CurrentAccount.class)
		                .map(currentAccount -> {
		                    System.out.println("Encontro currentAccount > " + currentAccount.getId());
		                    return Optional.of((BankAccount)currentAccount);
		                })
		                .switchIfEmpty(webClientFixed.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
		                        .accept(MediaType.APPLICATION_JSON)
		                        .retrieve()
		                        .bodyToMono(FixedTerm.class)
		                        .map(fixedTerm -> {
		                            System.out.println("Encontro fixedTerm > " + fixedTerm.getId());
		                            return Optional.of((BankAccount)fixedTerm);
		                        })
		                        .switchIfEmpty(webClientSaving.get().uri("/findByAccountNumber/{numberAccount}", acc.getAccountNumber())
		                                        .accept(MediaType.APPLICATION_JSON)
		                                        .retrieve()
		                                        .bodyToMono(SavingAccount.class)
		                                        .map(savingAccount -> {
		                                            System.out.println("Encontro savingAccount > " + savingAccount.getId());
		                                            return Optional.of((BankAccount)savingAccount);
		                                        }))
		                        				.defaultIfEmpty(Optional.empty())
		                        )
		        );
	}
	
}
