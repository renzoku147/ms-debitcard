package com.spring.msdebitcard.entity;

import lombok.Data;

@Data
public class TypeCustomer {
    String id;

    EnumTypeCustomer value;

    SubType subType;

    public enum EnumTypeCustomer {
        EMPRESARIAL, PERSONAL
    }
}
