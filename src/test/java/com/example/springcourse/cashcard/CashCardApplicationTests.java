package com.example.springcourse.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CashCardApplicationTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void shouldReturnCashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .getForEntity("/cashcards/100", String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");

            Assertions.assertThat(id).isNotNull();
            Assertions.assertThat(id).isEqualTo(100);
    }

    @Test
    public void shouldReturn404WhenDataIsNotFound() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .getForEntity("/cashcards/900", String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldListThreeEntitiesPrevioslySetOnDataSQLFile() {
        ResponseEntity<List<CashCard>> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .exchange(
                    "/cashcards",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CashCard>>() { }
                );

        Assertions.assertThat(Objects.requireNonNull(response.getBody()).size()).isEqualTo(3);
    }

    @Test
    public void shouldFindDataForKumar() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kumar2","12345")
                        .getForEntity("/cashcards/400", String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    @DirtiesContext
    public void shouldCreateANewCashCard() {

        CashCard newCashCard = new CashCard(null, "Cash Card 2", null);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .postForEntity("/cashcards", newCashCard, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI location = response.getHeaders().getLocation();

        ResponseEntity<String> newCashCardFoundResponse = restTemplate
                .withBasicAuth("sarah1","12345")
                .getForEntity(location, String.class);

        Assertions.assertThat(newCashCardFoundResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .getForEntity("/cashcards/query?page=0&size=1&sort=name,desc", String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        Assertions.assertThat(read.size()).isEqualTo(1);

        String amount = documentContext.read("$[0].name");
        Assertions.assertThat(amount).isEqualToIgnoringCase("Cash Card 3");
    }

    @Test
    void shouldReturnThreeWhenFindAll() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .getForEntity("/cashcards", String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
            int cashCardCount = documentContext.read("$.length()");
            Assertions.assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
            Assertions.assertThat(ids).containsExactlyInAnyOrder(100,200,300);

        JSONArray names = documentContext.read("$..name");
            Assertions.assertThat(names).containsExactlyInAnyOrder("Cash Card 1", "Cash Card 2", "Cash Card 3");
    }

    @Test
    void shoulReturn401WhenUserNotAuthorized() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "123456")
                .getForEntity("/cashcards/100", String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void  shouldRejectHank() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cards", "12345")
                .getForEntity("/cashcards/100", String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAccesCashCardsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "12345")
                .getForEntity("/cashcards/400", String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateExistingCashCard() {
        CashCard cashCardUpdate = new CashCard(null, "Cash Card Update", null);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1", "12345")
                .exchange("/cashcards/100", HttpMethod.PUT, request, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("sarah1", "12345")
                .getForEntity("/cashcards/100", String.class);

        Assertions.assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");

        Assertions.assertThat(id).isEqualTo(100);
        Assertions.assertThat(name).isEqualTo("Cash Card Update");
    }

    @Test
    void shoulNotUpdateCashCardThatDoesNotExist() {
        CashCard unknown = new CashCard(null, "", null);
        HttpEntity<CashCard> request = new HttpEntity<>(unknown);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "12345")
                .exchange("/cashcards/999999", HttpMethod.PUT, request, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shoulDeleteCashCard() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .exchange("/cashcards/100", HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> queryResponse = restTemplate
                .withBasicAuth("sarah1","12345")
                .getForEntity("/cashcards/100", String.class);

        Assertions.assertThat(queryResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void shouldNotDeleteCashCardThatDoesNotExist() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .exchange("/cashcards/9090290", HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAllowToDeleteCashCardsTheyNotOwn() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1","12345")
                .exchange("/cashcards/400", HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> queryResponse = restTemplate
                .withBasicAuth("kumar2","12345")
                .getForEntity("/cashcards/400", String.class);

        Assertions.assertThat(queryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
