package cn.rtomde.service.spring.web.controller;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.spring.http.CloudEventHttpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("cloudevents")
@Tag(name = "cloudevents 测试", description = "cloudevents-api")
public class CloudEventsController {

    @PostMapping("/event")
    @Operation(summary = "注入 event", description = "自动注入 event")
    public CloudEvent get(@RequestBody CloudEvent event) {
        CloudEvent attributes = CloudEventBuilder.from(event) //
                .withId(UUID.randomUUID().toString()) //
                .withSource(URI.create("https://spring.io/foos")) //
                .withType("io.spring.event.Foo") //
                .withData(event.getData().toBytes()) //
                .build();
        return attributes;
    }

    @PostMapping
    public ResponseEntity<Foo> echo(@RequestBody Foo foo, @RequestHeader HttpHeaders headers) {
        CloudEvent attributes = CloudEventHttpUtils.fromHttp(headers) //
                .withId(UUID.randomUUID().toString()) //
                .withSource(URI.create("https://spring.io/foos")) //
                .withType("io.spring.event.Foo") //
                .build();
        HttpHeaders outgoing = CloudEventHttpUtils.toHttp(attributes);
        return ResponseEntity.ok().headers(outgoing).body(foo);
    }

    @Getter
    @Setter
    public static class Foo {

        private String value;
    }

}
