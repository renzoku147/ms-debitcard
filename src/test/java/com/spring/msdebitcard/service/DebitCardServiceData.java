package com.spring.msdebitcard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import com.spring.msdebitcard.entity.Accounts;
import com.spring.msdebitcard.entity.Customer;
import com.spring.msdebitcard.entity.DebitCard;
import com.spring.msdebitcard.entity.SubType;
import com.spring.msdebitcard.entity.TypeCustomer;
import com.spring.msdebitcard.entity.SubType.EnumSubType;
import com.spring.msdebitcard.entity.TypeCustomer.EnumTypeCustomer;

import reactor.core.publisher.Mono;

public class DebitCardServiceData {
	
	public static Mono<DebitCard> getMockCreate() {
		return Mono.just(DebitCard.builder()
							.id("12345")
							.cardNumber("AAA")
							.customer(Customer.builder()
										.id("98765")
										.name("Mockito")
										.lastName("Prueba Unit")
										.typeCustomer(TypeCustomer.builder()
														.id("546")
														.value(EnumTypeCustomer.EMPRESARIAL)
														.subType(SubType.builder()
																	.id("786")
																	.value(EnumSubType.NORMAL)
																	.build())
														.build()
													)
										.build()
									)
							.accounts(Arrays.asList(Accounts.builder().accountNumber("111").priority(1).build(),
													Accounts.builder().accountNumber("111").priority(1).build())
									)
							.expirationDate(LocalDate.now())
							.date(LocalDateTime.now())
							.build()
							
						);
						
	}
}
