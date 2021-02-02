package cn.sliew.rtomde.akka.spring.boot.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActorBean {
}
