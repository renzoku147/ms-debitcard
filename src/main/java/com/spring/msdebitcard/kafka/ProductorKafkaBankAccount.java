package com.spring.msdebitcard.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import com.spring.msdebitcard.entity.CurrentAccount;
import com.spring.msdebitcard.entity.FixedTerm;
import com.spring.msdebitcard.entity.SavingAccount;

import org.springframework.kafka.support.serializer.JsonSerializer;


@Component
public class ProductorKafkaBankAccount {
	
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate3;
	
//	private final String kafkaTopicCurrentAccount = "topico-everis3";
//	private final String kafkaTopicFixedTerm = "topico-everis4";
//	private final String kafkaTopicSavingAccount = "topico-everis5";
//	
//	public void sendTransaction(CurrentAccount message) {
//		kafkaTemplate3.send(kafkaTopicCurrentAccount, message);
//	}
//	public void sendTransaction(FixedTerm message) {
//		kafkaTemplate3.send(kafkaTopicFixedTerm, message);
//	}
//	
//	public void sendTransaction(SavingAccount message) {
//		kafkaTemplate3.send(kafkaTopicSavingAccount, message);
//	}
	
	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		
		return new DefaultKafkaProducerFactory<String, Object>(configs);
	}
	
	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}
