package com.spring.msdebitcard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.spring.msdebitcard.entity.DebitCard;
import com.spring.msdebitcard.repository.DebitCardRepository;
import com.spring.msdebitcard.service.DebitCardService;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class DebitCardServiceTest {
	
//	@InjectMocks
//	private DebitCardService orderService;
	
	@Mock
	DebitCardRepository debitCardRepository;
	
	@Mock
	WebClient webClientCurrent;

	@Mock
    WebClient webClientFixed;

	@Mock
    WebClient webClientSaving;
    
	@Mock 
    WebClient webClientCustomer;
    
	@Mock
    WebClient webClientCreditCard;
	
//	@BeforeEach
//    public void init() {
//        AccountDto mockAccount = OrderServiceDataTestUtils.getMockAccount("12345678");
//        Mockito.doReturn(Optional.of(mockAccount)).when(customerClient).findAccount(anyString());
//    }
	
	@DisplayName("Test find debit card by id")
	@Test
	public void shouldGetFindById() {
		Mono<DebitCard> dc = DebitCardServiceData.getMockCreate();
		;
		Mockito.when(debitCardRepository.findById(""));
		dc.subscribe(System.out::println);
	}
}
