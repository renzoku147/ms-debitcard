package com.spring.msdebitcard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document("DebitCard")
@AllArgsConstructor
@NoArgsConstructor
public class DebitCard implements Card{
	@Id
    private String id;

    @NotEmpty
    private String cardNumber;
    
    @NotNull
    private Customer customer;
    
    @NotEmpty
    private List<Accounts> accounts;

    @JsonSerialize( using = LocalDateSerializer.class )
    @JsonDeserialize(using=LocalDateDeserializer.class)
    @NotNull
    private LocalDate expirationDate;

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)
    @JsonSerialize(using=LocalDateTimeSerializer.class)
    private LocalDateTime date;
    
    private Double amountPurseTransaction;
}
