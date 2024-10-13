package com.example.springcourse.cashcard;

import org.springframework.data.annotation.Id;

public record CashCard(
        @Id
        Long id,
        String name,
        String owner
) {
}
