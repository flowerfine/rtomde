package cn.rtomde.service.spring;

import io.cloudevents.CloudEvent;

public interface Driver {

    Object invoke(CloudEvent event);
}
