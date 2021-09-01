package com.spring.msdebitcard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FixedTerm implements BankAccount{
    private String id;

    private Customer customer;

    private String accountNumber;

    private List<Person> holders;

    private List<Person> signers;

    private Double balance;

    private Integer limitDeposits;

    private Integer limitDraft;

    @JsonDeserialize(using=LocalDateDeserializer.class)
    @JsonSerialize(using=LocalDateSerializer.class)
    private LocalDate allowDateTransaction;

    private Integer freeTransactions;

    private Double commissionTransactions;
    
    private DebitCard debitCard;

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)
    @JsonSerialize(using=LocalDateTimeSerializer.class)
    private LocalDateTime date;

}
