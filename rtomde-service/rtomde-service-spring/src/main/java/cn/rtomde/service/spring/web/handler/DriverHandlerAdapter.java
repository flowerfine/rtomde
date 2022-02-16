package cn.rtomde.service.spring.web.handler;

import cn.rtomde.service.driver.Driver;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DriverHandlerAdapter extends WebContentGenerator implements HandlerAdapter, Ordered {

    private int order = Ordered.HIGHEST_PRECEDENCE;

    private RequestResponseProcessor requestResponseProcessor;

    public DriverHandlerAdapter() {
        super(false);
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof Driver;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        checkRequest(request);

        return invokeHandlerMethod(request, response, (Driver) handler);
    }

    protected ModelAndView invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response, Driver invoker) throws Exception {
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        try {
            InvocableDriver invocableDriver = new InvocableDriver(invoker);
            invocableDriver.setRequestResponseProcessor(requestResponseProcessor);
            invocableDriver.invokeAndHandle(webRequest);
            return null;
        } finally {
            webRequest.requestCompleted();
        }
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setRequestResponseProcessor(RequestResponseProcessor requestResponseProcessor) {
        this.requestResponseProcessor = requestResponseProcessor;
    }
}
