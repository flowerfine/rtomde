package cn.rtomde.service.spring.web.driver.support;

import io.cloudevents.CloudEvent;
import io.cloudevents.spring.mvc.CloudEventHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.*;

@Slf4j
public abstract class AbstractMessageConverterArgumentResolver implements DriverArgumentResolver, DriverReturnValueHandler {

    private static final Class TARGET_CLASS = CloudEvent.class;
    private static final List<MediaType> ALL_APPLICATION_MEDIA_TYPES = Arrays.asList(MediaType.ALL, new MediaType("application"));
    private static final Set<HttpMethod> SUPPORTED_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);
    private static final Object NO_VALUE = new Object();

    protected final CloudEventHttpMessageConverter converter = new CloudEventHttpMessageConverter();
    private final ContentNegotiationManager contentNegotiationManager;

    protected AbstractMessageConverterArgumentResolver(@Nullable ContentNegotiationManager manager) {
        this.contentNegotiationManager = (manager != null ? manager : new ContentNegotiationManager());
    }

    @Override
    public CloudEvent resolve(ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return null;
    }

    protected Object readWithMessageConverters(NativeWebRequest webRequest) throws HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
        return readWithMessageConverters(inputMessage);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Object readWithMessageConverters(HttpInputMessage inputMessage) throws HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
        MediaType contentType;
        boolean noContentType = false;
        try {
            contentType = inputMessage.getHeaders().getContentType();
        } catch (InvalidMediaTypeException ex) {
            throw new HttpMediaTypeNotSupportedException(ex.getMessage());
        }
        if (contentType == null) {
            noContentType = true;
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        Class targetClass = TARGET_CLASS;

        HttpMethod httpMethod = (inputMessage instanceof HttpRequest ? ((HttpRequest) inputMessage).getMethod() : null);
        Object body = NO_VALUE;

        EmptyBodyCheckingHttpInputMessage message;
        try {
            message = new EmptyBodyCheckingHttpInputMessage(inputMessage);
            if (converter.canRead(targetClass, contentType)) {
                if (message.hasBody()) {
                    body = converter.read(targetClass, message);
                }
            }
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex, inputMessage);
        }

        if (body == NO_VALUE) {
            if (httpMethod == null || !SUPPORTED_METHODS.contains(httpMethod) || (noContentType && !message.hasBody())) {
                return null;
            }
            throw new HttpMediaTypeNotSupportedException(contentType, getSupportedMediaTypes(targetClass));
        }

        if (log.isDebugEnabled()) {
            if (log.isTraceEnabled()) {
                String formatted = LogFormatUtils.formatValue(body, false);
                log.trace("Read \"" + contentType + "\" to [" + formatted + "]");
            } else {
                String formatted = LogFormatUtils.formatValue(body, true);
                log.debug("Read \"" + contentType + "\" to [" + formatted + "]");
            }
        }
        return body;
    }

    protected <T> void writeWithMessageConverters(T value, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);
        writeWithMessageConverters(value, inputMessage, outputMessage);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T> void writeWithMessageConverters(@Nullable T value, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        Object body = value;
        Class<?> valueType = TARGET_CLASS;

        MediaType selectedMediaType = null;
        MediaType contentType = outputMessage.getHeaders().getContentType();
        boolean isContentTypePreset = contentType != null && contentType.isConcrete();
        if (isContentTypePreset) {
            if (log.isDebugEnabled()) {
                log.debug("Found 'Content-Type:" + contentType + "' in response");
            }
            selectedMediaType = contentType;
        }
        else {
            HttpServletRequest request = inputMessage.getServletRequest();
            List<MediaType> acceptableTypes;
            try {
                acceptableTypes = getAcceptableMediaTypes(request);
            }
            catch (HttpMediaTypeNotAcceptableException ex) {
                int series = outputMessage.getServletResponse().getStatus() / 100;
                if (body == null || series == 4 || series == 5) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring error response content (if any). " + ex);
                    }
                    return;
                }
                throw ex;
            }
            List<MediaType> producibleTypes = getProducibleMediaTypes(request);

            if (body != null && producibleTypes.isEmpty()) {
                throw new HttpMessageNotWritableException(
                        "No converter found for return value of type: " + valueType);
            }
            List<MediaType> mediaTypesToUse = new ArrayList<>();
            for (MediaType requestedType : acceptableTypes) {
                for (MediaType producibleType : producibleTypes) {
                    if (requestedType.isCompatibleWith(producibleType)) {
                        mediaTypesToUse.add(getMostSpecificMediaType(requestedType, producibleType));
                    }
                }
            }
            if (mediaTypesToUse.isEmpty()) {
                if (body != null) {
                    throw new HttpMediaTypeNotAcceptableException(producibleTypes);
                }
                if (log.isDebugEnabled()) {
                    log.debug("No match for " + acceptableTypes + ", supported: " + producibleTypes);
                }
                return;
            }

            MediaType.sortBySpecificityAndQuality(mediaTypesToUse);

            for (MediaType mediaType : mediaTypesToUse) {
                if (mediaType.isConcrete()) {
                    selectedMediaType = mediaType;
                    break;
                }
                else if (mediaType.isPresentIn(ALL_APPLICATION_MEDIA_TYPES)) {
                    selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Using '" + selectedMediaType + "', given " +
                        acceptableTypes + " and supported " + producibleTypes);
            }
        }

        if (selectedMediaType != null) {
            selectedMediaType = selectedMediaType.removeQualityValue();
            if (converter.canWrite(TARGET_CLASS, selectedMediaType)) {
                converter.write((CloudEvent) value, selectedMediaType, outputMessage);
                return;
            }
        }

        if (body != null) {
            Set<MediaType> producibleMediaTypes =
                    (Set<MediaType>) inputMessage.getServletRequest()
                            .getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

            if (isContentTypePreset || !CollectionUtils.isEmpty(producibleMediaTypes)) {
                throw new HttpMessageNotWritableException(
                        "No converter for [" + valueType + "] with preset Content-Type '" + contentType + "'");
            }
            throw new HttpMediaTypeNotAcceptableException(getSupportedMediaTypes(body.getClass()));
        }
    }

    protected ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        return new ServletServerHttpRequest(servletRequest);
    }

    protected ServletServerHttpResponse createOutputMessage(NativeWebRequest webRequest) {
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");
        return new ServletServerHttpResponse(response);
    }

    protected List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
        List<MediaType> result;
        if (converter.canRead(clazz, null) || converter.canWrite(clazz, null)) {
            result = converter.getSupportedMediaTypes();
        } else {
            result = Collections.emptyList();
        }
        MediaType.sortBySpecificity(result);
        return result;
    }

    private List<MediaType> getAcceptableMediaTypes(HttpServletRequest request) throws HttpMediaTypeNotAcceptableException {
        return this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
    }

    protected List<MediaType> getProducibleMediaTypes(HttpServletRequest request) {
        Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            return new ArrayList<>(mediaTypes);
        }

        List<MediaType> result = Collections.emptyList();
        if (converter.canWrite(TARGET_CLASS, null)) {
            result = converter.getSupportedMediaTypes();
        }
        return (result.isEmpty() ? Collections.singletonList(MediaType.ALL) : result);
    }

    private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
        MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);
        return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse);
    }

    private static class EmptyBodyCheckingHttpInputMessage implements HttpInputMessage {

        private final HttpHeaders headers;
        @Nullable
        private final InputStream body;

        public EmptyBodyCheckingHttpInputMessage(HttpInputMessage inputMessage) throws IOException {
            this.headers = inputMessage.getHeaders();
            InputStream inputStream = inputMessage.getBody();
            if (inputStream.markSupported()) {
                inputStream.mark(1);
                this.body = (inputStream.read() != -1 ? inputStream : null);
                inputStream.reset();
            } else {
                PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
                int b = pushbackInputStream.read();
                if (b == -1) {
                    this.body = null;
                } else {
                    this.body = pushbackInputStream;
                    pushbackInputStream.unread(b);
                }
            }
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public InputStream getBody() {
            return (this.body != null ? this.body : StreamUtils.emptyInput());
        }

        public boolean hasBody() {
            return (this.body != null);
        }
    }


}
