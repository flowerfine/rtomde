package cn.sliew.rtomde.service.dubbo;

import cn.sliew.milky.test.MilkyTestCase;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class GenericConsumerTest extends MilkyTestCase {

    private GenericService genericService;

    @BeforeEach
    private void beforeEach() {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("demo-generic-consumer");

        RegistryConfig multicast = new RegistryConfig();
        multicast.setProtocol("multicast");
        multicast.setAddress("224.5.6.7:1234");

        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface("dubbo.MapperService");
        reference.setApplication(application);
        reference.setRegistry(multicast);
        reference.setGeneric(true);
        reference.setAsync(true);
        reference.setTimeout(7000);
        genericService = reference.get();
    }

    @Test
    public void test_users_selectByPrimaryKey() throws InterruptedException {
        genericService.$invoke("users_selectByPrimaryKey", new String[]{"long"}, new Object[]{1L});
        CountDownLatch latch = new CountDownLatch(1);

        CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
        future.whenComplete((value, t) -> {
            System.err.println("users_selectByPrimaryKey(whenComplete): " + value);
            latch.countDown();
        });
        latch.await();
    }



}
