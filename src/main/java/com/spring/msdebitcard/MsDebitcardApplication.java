package com.spring.msdebitcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MsDebitcardApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsDebitcardApplication.class, args);
	}

}
