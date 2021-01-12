package cn.sliew.rtomde.executor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.fu.jafu.BeanDefinitionDsl;
import org.springframework.fu.jafu.JafuApplication;
import org.springframework.fu.jafu.webmvc.WebMvcServerDsl;
import org.springframework.web.servlet.function.RouterFunctions;

import static org.springframework.fu.jafu.Jafu.webApplication;
import static org.springframework.fu.jafu.webmvc.WebMvcServerDsl.webMvc;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        JafuApplication app =
                webApplication(applicationDsl -> applicationDsl.beans(beanDefinitionDsl -> initBeans(beanDefinitionDsl))
                        .enable(webMvc(serverDsl -> serverDsl.port(80)
                                .router(router -> initRouter(serverDsl, router))
                                .converters(c -> initConverters(serverDsl)))));
        app.run(args);
    }


    // 设置格式转换
    public static WebMvcServerDsl initConverters(WebMvcServerDsl serverDsl) {
        return serverDsl.converters(c -> c.string().jackson());
    }

    // 设置接口地址路由
    public static WebMvcServerDsl initRouter(WebMvcServerDsl serverDsl, RouterFunctions.Builder router) {
        TestHandler handler = serverDsl.ref(TestHandler.class);
        router.GET("/", handler::hello);
        router.GET("/json", handler::json);
        router.GET("/json2", handler::json2);
        return serverDsl;
    }

    // 声明bean
    public static BeanDefinitionDsl initBeans(BeanDefinitionDsl beanDefinitionDsl) {
        beanDefinitionDsl
                .bean(TestHandler.class)
                .bean(TestService.class)
                .bean(TestService2.class);
        return beanDefinitionDsl;
    }
}