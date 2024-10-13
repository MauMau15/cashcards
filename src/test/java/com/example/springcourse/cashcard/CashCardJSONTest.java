package com.example.springcourse.cashcard;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@JsonTest
public class CashCardJSONTest {

    @Autowired
    JacksonTester<CashCard> json;

    @Autowired
    JacksonTester<List<CashCard>> jsonList;

    private List<CashCard> cashCards;

    @BeforeEach
    void setUp() {
        cashCards = Arrays.asList(new CashCard(100L, "Cash Card 1", "sarah1"), new CashCard(200L, "Cash Card 2", "sarah1"), new CashCard(300L, "Cash Card 3", "sarah1"));
    }

    @Test
    void cashCardInitializationTest() throws IOException {
        CashCard cc = cashCards.get(0);

        Assertions.assertThat(json.write(cc)).isStrictlyEqualToJson("single.json");

        Assertions.assertThat(json.write(cc)).hasJsonPathNumberValue("@.id");
        Assertions.assertThat(json.write(cc)).extractingJsonPathNumberValue("@.id").isEqualTo(100);

        Assertions.assertThat(json.write(cc)).hasJsonPathStringValue("@.name");
        Assertions.assertThat(json.write(cc)).extractingJsonPathStringValue("@.name").isEqualTo("Cash Card 1");

    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String expected = """
                    {
                        "id":99,
                        "name":"Cash Card 1",
                        "owner":"sarah1"
                    }
                """;

        Assertions.assertThat(json.parse(expected)).isEqualTo(new CashCard(99L, "Cash Card 1", "sarah1"));
        Assertions.assertThat(json.parseObject(expected).id()).isEqualTo(99);
        Assertions.assertThat(json.parseObject(expected).name()).isEqualTo("Cash Card 1");
    }

    @Test
    void shouldBeEqualToListJSON() throws IOException {
        Assertions.assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void shouldSerializeList() throws IOException {
        String expected = """
                [
                    {"id":100, "name": "Cash Card 1","owner":"sarah1"},
                    {"id":200, "name": "Cash Card 2","owner":"sarah1"},
                    {"id":300, "name": "Cash Card 3","owner":"sarah1"}
                ]
                """;

        Assertions.assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }

}

