package cn.rtomde.service.spring.web.controller;

import cn.rtomde.service.spring.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
class CloudEventsControllerTestCase {

    @Autowired
    private CloudEventsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void echoWithCorrectHeaders() throws Exception {
        ResultActions result = mockMvc.perform(post("/cloudevents")
                .content("{\"value\":\"Dave\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .header("ce-id", "12345")
                .header("ce-specversion", "1.0")
                .header("ce-type", "io.spring.event")
                .header("ce-source", "https://spring.io/events")
        );

        result.andExpect(status().isOk())
                .andExpect(content().string("{\"value\":\"Dave\"}"))
                .andExpect(header().exists("ce-id"))
                .andExpect(header().exists("ce-source"))
                .andExpect(header().exists("ce-type"))
                .andExpect(header().exists("ce-type"))
                .andExpect(header().string("ce-type", "io.spring.event.Foo"))
                .andExpect(header().string("ce-source", "https://spring.io/foos"))
                .andDo(print());
    }

    @Test
    void structuredRequestResponseEvents() throws Exception {
        ResultActions result = mockMvc.perform(post("/cloudevents/event")
                .content("{"
                        + "\"id\":\"12345\","
                        + "\"specversion\":\"1.0\","
                        + "\"type\":\"io.spring.event\","
                        + "\"source\":\"https://spring.io/events\","
                        + "\"data\":{\"value\":\"Dave\"}}")
                .contentType(new MediaType("application", "cloudevents+json"))
        );

        result.andExpect(status().isOk())
                .andExpect(content().string("{\"value\":\"Dave\"}"))
                .andExpect(header().exists("ce-id"))
                .andExpect(header().exists("ce-source"))
                .andExpect(header().exists("ce-type"))
                .andExpect(header().exists("ce-type"))
                .andExpect(header().string("ce-type", "io.spring.event.Foo"))
                .andExpect(header().string("ce-source", "https://spring.io/foos"))
                .andDo(print());
    }

    @Test
    void requestResponseEvents() throws Exception {
        ResultActions result = mockMvc.perform(post("/cloudevents/event")
                .content("{\"value\":\"Dave\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .header("ce-id", "12345")
                .header("ce-specversion", "1.0")
                .header("ce-type", "io.spring.event")
                .header("ce-source", "https://spring.io/events")
        );

        result.andExpect(status().isOk())
                .andExpect(content().string("{\"value\":\"Dave\"}"))
                .andExpect(header().exists("ce-id"))
                .andExpect(header().exists("ce-source"))
                .andExpect(header().exists("ce-type"))
                .andExpect(header().exists("ce-type"))
                .andExpect(header().string("ce-id", "12345"))
                .andExpect(header().string("ce-type", "io.spring.event.Foo"))
                .andExpect(header().string("ce-source", "https://spring.io/foos"))
                .andDo(print());
    }

}
