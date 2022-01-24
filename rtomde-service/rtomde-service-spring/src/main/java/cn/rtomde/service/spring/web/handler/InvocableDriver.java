package cn.rtomde.service.spring.web.handler;

import cn.rtomde.service.spring.web.driver.Driver;
import io.cloudevents.CloudEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InvocableDriver {

    protected static final Log logger = LogFactory.getLog(InvocableDriver.class);

    private Driver driver;
    private RequestResponseProcessor requestResponseProcessor;

    public InvocableDriver(Driver driver) {
        this.driver = driver;
    }

    public void invokeAndHandle(ServletWebRequest webRequest) throws Exception {
        Object returnValue = invokeForRequest(webRequest);

        if (returnValue == null) {
            if (webRequest.isNotModified()) {
                disableContentCachingIfNecessary(webRequest);
                return;
            }
        }

        try {
            this.requestResponseProcessor.handleReturnValue(returnValue, webRequest);
        } catch (Exception ex) {
            if (logger.isTraceEnabled()) {
                logger.trace(formatErrorForReturnValue(returnValue), ex);
            }
            throw ex;
        }
    }

    public Object invokeForRequest(NativeWebRequest request) throws Exception {
        CloudEvent event = getMethodArgumentValues(request);
        if (logger.isTraceEnabled()) {
            logger.trace("Arguments: " + Arrays.toString(new Object[]{event}));
        }
        return doInvoke(event);
    }

    protected CloudEvent getMethodArgumentValues(NativeWebRequest request) throws Exception {
        return (CloudEvent) requestResponseProcessor.resolveArgument(CloudEvent.class, request);
    }

    /**
     * Invoke the handler method with the given argument values.
     */
    @Nullable
    protected Object doInvoke(CloudEvent event) throws Exception {
        try {
            return driver.invoke(event);
        } catch (IllegalArgumentException ex) {
            String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
            throw new IllegalStateException(formatInvokeError(text, new Object[]{event}), ex);
        } catch (Exception targetException) {
            throw new IllegalStateException(formatInvokeError("Invocation failure", new Object[]{event}), targetException);
        }
    }

    protected String formatInvokeError(String text, Object[] args) {
        String formattedArgs = IntStream.range(0, args.length)
                .mapToObj(i -> (args[i] != null ?
                        "[" + i + "] [type=" + args[i].getClass().getName() + "] [value=" + args[i] + "]" :
                        "[" + i + "] [null]"))
                .collect(Collectors.joining(",\n", " ", " "));
        return text + "\n" +
                "Driver [" + driver.getClass().getName() + "]\n" +
                "with argument values:\n" + formattedArgs;
    }

    private void disableContentCachingIfNecessary(ServletWebRequest webRequest) {
        if (webRequest.isNotModified()) {
            HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
            Assert.notNull(response, "Expected HttpServletResponse");
            if (StringUtils.hasText(response.getHeader(HttpHeaders.ETAG))) {
                HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
                Assert.notNull(request, "Expected HttpServletRequest");
            }
        }
    }

    private String formatErrorForReturnValue(@Nullable Object returnValue) {
        return "Error handling return value=[" + returnValue + "]" +
                (returnValue != null ? ", type=" + returnValue.getClass().getName() : "") +
                " in " + toString();
    }

    public void setRequestResponseProcessor(RequestResponseProcessor requestResponseProcessor) {
        this.requestResponseProcessor = requestResponseProcessor;
    }
}
