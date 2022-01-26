package cn.rtomde.service.spring.web.driver.support;

import io.cloudevents.CloudEvent;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public interface DriverArgumentResolver {

    CloudEvent resolve(@Nullable ModelAndViewContainer mavContainer,
                       NativeWebRequest webRequest,
                       @Nullable WebDataBinderFactory binderFactory) throws Exception;
}
