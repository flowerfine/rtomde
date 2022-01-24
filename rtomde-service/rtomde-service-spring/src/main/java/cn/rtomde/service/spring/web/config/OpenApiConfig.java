package cn.rtomde.service.spring.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("rtomde API")
                        .description("rtomde")
                        .version("1.0.0")
                        .contact(contact())
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.txt")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("controller api")
                .packagesToScan("cn.rtomde.service.spring.web.controller")
                .build();
    }

    private Contact contact() {
        Contact contact = new Contact();
        contact.setName("wangqi");
        contact.setUrl("http://www.rtomde.cn");
        contact.setEmail("wangqi@rtomde.cn");
        return contact;
    }
}