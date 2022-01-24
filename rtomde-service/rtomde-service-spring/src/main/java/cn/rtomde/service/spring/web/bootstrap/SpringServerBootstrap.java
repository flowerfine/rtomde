package cn.rtomde.service.spring.web.bootstrap;

import cn.rtomde.service.spring.web.handler.ConcurrentUrlHandlerMapping;
import cn.rtomde.service.spring.web.handler.DriverHandlerAdapter;
import cn.rtomde.service.spring.web.handler.RequestResponseProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.EnableWebMvcConfiguration.class)
public class SpringServerBootstrap {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Autowired
    @Qualifier("mvcContentNegotiationManager")
    private ContentNegotiationManager contentNegotiationManager;

    @Bean
    public ConcurrentUrlHandlerMapping concurrentUrlHandlerMapping() {
        ConcurrentUrlHandlerMapping handlerMapping = new ConcurrentUrlHandlerMapping();
        handlerMapping.setOrder(requestMappingHandlerMapping.getOrder() - 1);
        return handlerMapping;
    }

    @Bean
    public DriverHandlerAdapter driverHandlerAdapter() {
        DriverHandlerAdapter handlerAdapter = new DriverHandlerAdapter();
        List<HttpMessageConverter<?>> messageConverters = requestMappingHandlerAdapter.getMessageConverters();
        handlerAdapter.setRequestResponseProcessor(new RequestResponseProcessor(messageConverters, contentNegotiationManager));
        handlerAdapter.setOrder(requestMappingHandlerAdapter.getOrder() - 1);
        return handlerAdapter;
    }

}
