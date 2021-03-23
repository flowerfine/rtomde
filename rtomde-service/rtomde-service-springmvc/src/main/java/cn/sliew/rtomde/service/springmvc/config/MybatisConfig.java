package cn.sliew.rtomde.service.springmvc.config;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MybatisAutoConfiguration.class)
public class MybatisConfig {
}
