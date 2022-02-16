package cn.rtomde.service.spring.web.controller;

import cn.rtomde.service.spring.Application;
import cn.sliew.milky.test.MilkyTestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
class CloudEventsControllerTestCase extends MilkyTestCase {

    @LocalServerPort
    private int port;
    @Value("${server.servlet.context-path:service-spring}")
    private String contextPath;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void echoWithCorrectHeaders() {
        String uri = "http://localhost:" + port + "/" + contextPath + "/cloudevents/";
        ResponseEntity<String> response = rest.exchange(RequestEntity.post(URI.create(uri))
                .header("ce-id", "12345")
                .header("ce-specversion", "1.0")
                .header("ce-type", "io.spring.event")
                .header("ce-source", "https://spring.io/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"value\":\"Dave\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"value\":\"Dave\"}");

        HttpHeaders headers = response.getHeaders();
        assertThat(headers).containsKey("ce-id");
        assertThat(headers).containsKey("ce-source");
        assertThat(headers).containsKey("ce-type");

        // assertThat(headers.getFirst("ce-id")).isNotEqualTo("12345");
        assertThat(headers.getFirst("ce-type")).isEqualTo("io.spring.event.Foo");
        assertThat(headers.getFirst("ce-source")).isEqualTo("https://spring.io/foos");
    }

    @Test
    void structuredRequestResponseEvents() {
        String uri = "http://localhost:" + port + "/" + contextPath + "/cloudevents/event/";
        ResponseEntity<String> response = rest.exchange(RequestEntity.post(URI.create(uri))
                        .contentType(new MediaType("application", "cloudevents+json"))
                        .body("{"
                                + "\"id\":\"12345\","
                                + "\"specversion\":\"1.0\","
                                + "\"type\":\"io.spring.event\","
                                + "\"source\":\"https://spring.io/events\","
                                + "\"data\":{\"value\":\"Dave\"}}"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"value\":\"Dave\"}");

        HttpHeaders headers = response.getHeaders();
        assertThat(headers).containsKey("ce-id");
        assertThat(headers).containsKey("ce-source");
        assertThat(headers).containsKey("ce-type");

        // assertThat(headers.getFirst("ce-id")).isNotEqualTo("12345");
        assertThat(headers.getFirst("ce-type")).isEqualTo("io.spring.event.Foo");
        assertThat(headers.getFirst("ce-source")).isEqualTo("https://spring.io/foos");
    }

    @Test
    void requestResponseEvents() {
        String uri = "http://localhost:" + port + "/" + contextPath + "/cloudevents/event/";
        ResponseEntity<String> response = rest.exchange(RequestEntity.post(URI.create(uri))
                .header("ce-id", "12345")
                .header("ce-specversion", "1.0")
                .header("ce-type", "io.spring.event")
                .header("ce-source", "https://spring.io/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"value\":\"Dave\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"value\":\"Dave\"}");

        HttpHeaders headers = response.getHeaders();
        assertThat(headers).containsKey("ce-id");
        assertThat(headers).containsKey("ce-source");
        assertThat(headers).containsKey("ce-type");

        assertThat(headers.getFirst("ce-id")).isNotEqualTo("12345");
        assertThat(headers.getFirst("ce-type")).isEqualTo("io.spring.event.Foo");
        assertThat(headers.getFirst("ce-source")).isEqualTo("https://spring.io/foos");
    }

}
