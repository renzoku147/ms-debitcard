package com.spring.msdebitcard.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    String id;

    String name;

    String lastName;

    TypeCustomer typeCustomer;

    String dni;

    Integer age;

    String gender;

}
