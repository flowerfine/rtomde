package cn.rtomde.service.spring.web.controller;

import cn.rtomde.service.driver.Driver;
import cn.sliew.milky.common.util.JacksonUtil;
import io.cloudevents.CloudEvent;

public class PrintDriver implements Driver {

    @Override
    public Object invoke(CloudEvent event) {
        System.out.println(JacksonUtil.toJsonString(event));
        return event;
    }
}
