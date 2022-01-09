package cn.rtomde.template.mapping;

import io.cloudevents.CloudEvent;

public interface SqlTemplate {

    BoundSql bind(CloudEvent event);
}
