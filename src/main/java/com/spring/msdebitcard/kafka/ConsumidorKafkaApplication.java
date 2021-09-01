package com.spring.msdebitcard.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.msdebitcard.entity.BankAccount;
import com.spring.msdebitcard.entity.CurrentAccount;
import com.spring.msdebitcard.entity.DebitCard;
import com.spring.msdebitcard.entity.FixedTerm;
import com.spring.msdebitcard.entity.SavingAccount;
import com.spring.msdebitcard.service.DebitCardService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class ConsumidorKafkaApplication {
	@Autowired
	DebitCardService debitCardService;  
	
	@Autowired
	ProductorKafkaBankAccount productorKafka;
	
	@Autowired
	private KafkaTemplate<String, Object> template;

	ObjectMapper objectMapper = new ObjectMapper();
	
	@Bean
    public NewTopic topic(){
        return TopicBuilder.name("topico-everis2")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @KafkaListener(id="myId", topics = "topico-everis2")
    public void listen(String message) throws Exception{
    	System.out.println(">>>>> topico-everis2 @KafkaListener <<<<<");
    	DebitCard dc = objectMapper.readValue(message, DebitCard.class);
    	System.out.println(">>>>> ORIGIN topico-everis2 ");
    	System.out.println(dc);
    	
    	debitCardService.findPrincipalBankAccount( Flux.fromIterable(dc.getAccounts()) )
    		.filter(opt -> opt.isPresent())
    		.flatMap(opt -> {
    			System.out.println("Enviando topico > " );
    			BankAccount bankAccount = opt.get();
    			if(bankAccount instanceof CurrentAccount) {
    				System.out.println("CurrentAccount > topico-everis3");
    				CurrentAccount ca = (CurrentAccount)bankAccount;
    				ca.setBalance(ca.getBalance()+dc.getAmountPurseTransaction());
    				template.send("topico-everis3", ca);
    			}
				if(bankAccount instanceof FixedTerm) {
					System.out.println("FixedTerm > topico-everis4");
					FixedTerm ft = (FixedTerm)bankAccount;
					ft.setBalance(ft.getBalance()+dc.getAmountPurseTransaction());
					template.send("topico-everis4", ft);
    			}
				if(bankAccount instanceof SavingAccount) {
					System.out.println("SavingAccount > topico-everis5");
					SavingAccount sa = (SavingAccount)bankAccount;
					sa.setBalance(sa.getBalance()+dc.getAmountPurseTransaction());
					template.send("topico-everis5", sa);
				}
    			return Mono.empty();
    		}).subscribe();
//    	purseService.findByPhoneNumber(ptr.getNumberOrigin())
//				.flatMap(origin -> {
//								System.out.println("Encontro el ORIGEN " + origin.getPhoneNumber());
//								return purseService.findByPhoneNumber(ptr.getNumberDetiny())
//									.flatMap(destiny -> {
//										System.out.println("Encontro el DESTINY " + destiny.getNumberDoc());
//										origin.setBalance(origin.getBalance()-ptr.getBalance());
//										return purseService.update(origin)
//												.flatMap(originUpdate -> {
//													System.out.println("Encontro el UPDATE ORIGIN " + originUpdate.getBalance());
//													destiny.setBalance(destiny.getBalance()+ptr.getBalance());
//													return purseService.update(destiny);
//												});
//									});
//						}
//				).subscribe();
        	
    }
}
