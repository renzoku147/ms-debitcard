package com.spring.msdebitcard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;


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

    private String id;

    @NotEmpty
    private String cardNumber;
    
    @NotNull
    private Customer customer;
    
    @NotEmpty
    private List<Accounts> accounts;

    @NotNull
    private LocalDate expirationDate;

    private LocalDateTime date;
    
}
