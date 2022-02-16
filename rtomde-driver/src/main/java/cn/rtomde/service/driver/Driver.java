package cn.rtomde.service.driver;

import io.cloudevents.CloudEvent;

public interface Driver {

    Object invoke(CloudEvent event);
}
