package cn.sliew.rtomde.service.dubbo;

import cn.sliew.milky.test.MilkyTestCase;
import org.apache.dubbo.common.utils.PojoUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class GenericConsumerTest extends MilkyTestCase {

    private GenericService genericService;

    @BeforeEach
    private void beforeEach() {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("mybatis_generic_consumer");

        RegistryConfig zookeeper = new RegistryConfig();
        zookeeper.setProtocol("zookeeper");
        zookeeper.setAddress("127.0.0.1:2181");
        RegistryConfig multicast = new RegistryConfig();
        multicast.setProtocol("multicast");
        multicast.setAddress("224.5.6.7:1234");

        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface("cn.sliew.datacenter.users.SysUserMapper");
        application.setRegistries(Arrays.asList(zookeeper, multicast));
        reference.setApplication(application);
        reference.setProtocol("dubbo");
        reference.setGeneric(true);
//        reference.setAsync(true);
        reference.setTimeout(7000);
        genericService = reference.get();
    }

    @Test
    public void test_users_selectByPrimaryKey() throws InterruptedException {
        Map<String , Object> params = new HashMap<>();
        params.put("id", 1L);
        Object result = genericService.$invoke("cn_sliew_datacenter_users_SysUserMapper_selectByPrimaryKey", new String[]{"cn.sliew.datacenter.users.UserParam"}, new Object[]{params});
        System.err.println(result);

//        CountDownLatch latch = new CountDownLatch(1);
//        CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
//        future.whenComplete((value, t) -> {
//            System.err.println("cn_sliew_datacenter_users_SysUserMapper_selectByPrimaryKey(whenComplete): " + value);
//            latch.countDown();
//        });
//        latch.await();
    }




}
