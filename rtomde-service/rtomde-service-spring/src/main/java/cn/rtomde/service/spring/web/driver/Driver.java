package cn.rtomde.service.spring.web.driver;

import io.cloudevents.CloudEvent;

public interface Driver {

    Object invoke(CloudEvent event);
}
