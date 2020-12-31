package cn.sliew.rtomde.executor.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "cn.sliew.rtomde.executor.mapper")
public class MybatisConfig {

}