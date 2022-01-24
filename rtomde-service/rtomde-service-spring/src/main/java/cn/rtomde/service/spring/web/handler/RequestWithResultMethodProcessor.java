package cn.rtomde.service.spring.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RequestWithResultMethodProcessor implements HandlerMethodReturnValueHandler, InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    private RequestResponseBodyMethodProcessor processor;

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> unmodifiableList = adapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> list = new ArrayList<>(unmodifiableList.size());
        for (HandlerMethodReturnValueHandler returnValueHandler : unmodifiableList) {
            if (returnValueHandler instanceof RequestResponseBodyMethodProcessor) {
                this.processor = (RequestResponseBodyMethodProcessor) returnValueHandler;
                list.add(this);
            } else {
                list.add(returnValueHandler);
            }
        }
        adapter.setReturnValueHandlers(list);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        if (supportWithResult(returnType)) {
            WithApiResult resultAnno = returnType.getMethodAnnotation(WithApiResult.class);
            if (resultAnno == null) {
                resultAnno = returnType.getContainingClass().getAnnotation(WithApiResult.class);
            }
            ResultDO response = convertValue(returnValue);
            if (StringUtils.hasText(resultAnno.success())) {
                response.setMessage(resultAnno.success());
            }
            processor.handleReturnValue(response, returnType, mavContainer, webRequest);
        } else {
            processor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
        }
    }

    private ResultDO convertValue(Object returnValue) {
        if (returnValue != null && returnValue instanceof PagedDTO) {
            PagedDTO paged = (PagedDTO) returnValue;
            ResultDO response = ResultDO.success(paged.getDetails(), paged.getTotalRecord());
            response.setHasNext(paged.getTotalPage() > paged.getPageNum());
            return response;
        } else if (null != returnValue && returnValue instanceof List) {
            List list = (List) returnValue;
            return ResultDO.success(list, list.size());
        } else {
            return ResultDO.success(returnValue);
        }
    }

    private boolean supportWithResult(MethodParameter returnType) {
        if (returnType.getMethod().getReturnType().equals(ResultDO.class)) {
            return false;
        }
        if (returnType.hasMethodAnnotation(NoApiWrapper.class)) {
            return false;
        }

        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), WithApiResult.class)
                || returnType.hasMethodAnnotation(WithApiResult.class);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return processor.supportsReturnType(returnType);
    }
}