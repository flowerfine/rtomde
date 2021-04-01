package cn.sliew.rtomde.service.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(DubboServiceBootstrap.ORDER - 1)
@Component
public class ZookeeperBootstrap implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new EmbeddedZooKeeper(2181, true).start();
    }
}
