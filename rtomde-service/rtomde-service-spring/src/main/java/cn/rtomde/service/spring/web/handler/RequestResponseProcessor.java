package cn.rtomde.service.spring.web.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.*;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Type;
import java.util.*;

public class RequestResponseProcessor {

    protected final Log logger = LogFactory.getLog(getClass());

    private static final Set<HttpMethod> SUPPORTED_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);
    private static final Object NO_VALUE = new Object();
    private static final List<MediaType> ALL_APPLICATION_MEDIA_TYPES = Arrays.asList(MediaType.ALL, new MediaType("application"));
    private static final Set<String> SAFE_MEDIA_BASE_TYPES = new HashSet<>(Arrays.asList("audio", "image", "video"));

    private final Set<String> safeExtensions = new HashSet<>();

    private final List<HttpMessageConverter<?>> converters;
    private final ContentNegotiationManager contentNegotiationManager;
    protected final List<MediaType> allSupportedMediaTypes;

    public RequestResponseProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
        this.converters = converters;
        this.contentNegotiationManager = contentNegotiationManager;
        this.allSupportedMediaTypes = getAllSupportedMediaTypes(converters);
    }

    private static List<MediaType> getAllSupportedMediaTypes(List<HttpMessageConverter<?>> messageConverters) {
        Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<>();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
        }
        List<MediaType> result = new ArrayList<>(allSupportedMediaTypes);
        MediaType.sortBySpecificity(result);
        return Collections.unmodifiableList(result);
    }

    public Object resolveArgument(Class parameterType, NativeWebRequest webRequest) throws Exception {
        return readWithMessageConverters(webRequest, parameterType);
    }

    public void handleReturnValue(@Nullable Object returnValue, Class returnType, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        // Try even with null return value. ResponseBodyAdvice could get involved.
        writeWithMessageConverters(ResultDO.success(returnValue), returnType, inputMessage, outputMessage);
    }

    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, Type paramType) throws HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);

        Object arg = readWithMessageConverters(inputMessage, paramType);
        if (arg == null) {
            throw new HttpMessageNotReadableException("Required request body is missing: " +
                    paramType.getTypeName(), inputMessage);
        }
        return arg;
    }

    protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage, Type targetType) throws HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

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

        Class<T> targetClass = (Class<T>) targetType;
        HttpMethod httpMethod = (inputMessage instanceof HttpRequest ? ((HttpRequest) inputMessage).getMethod() : null);
        Object body = NO_VALUE;

        EmptyBodyCheckingHttpInputMessage message;
        try {
            message = new EmptyBodyCheckingHttpInputMessage(inputMessage);

            for (HttpMessageConverter<?> converter : this.converters) {
                Class<HttpMessageConverter<?>> converterType = (Class<HttpMessageConverter<?>>) converter.getClass();
                GenericHttpMessageConverter<?> genericConverter = (converter instanceof GenericHttpMessageConverter ? (GenericHttpMessageConverter<?>) converter : null);
                if (genericConverter != null ? genericConverter.canRead(targetType, null, contentType) : (targetClass != null && converter.canRead(targetClass, contentType))) {
                    if (message.hasBody()) {
                        body = (genericConverter != null ? genericConverter.read(targetType, null, message) :
                                ((HttpMessageConverter<T>) converter).read(targetClass, message));
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex, inputMessage);
        }

        if (body == NO_VALUE) {
            if (httpMethod == null || !SUPPORTED_METHODS.contains(httpMethod) ||
                    (noContentType && !message.hasBody())) {
                return null;
            }
            throw new HttpMediaTypeNotSupportedException(contentType, this.allSupportedMediaTypes);
        }

        MediaType selectedContentType = contentType;
        Object theBody = body;
        LogFormatUtils.traceDebug(logger, traceOn -> {
            String formatted = LogFormatUtils.formatValue(theBody, !traceOn);
            return "Read \"" + selectedContentType + "\" to [" + formatted + "]";
        });

        return body;
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

    protected <T> void writeWithMessageConverters(@Nullable T value, Type returnType,
                                                  ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        Object body = value;
        Class<?> valueType = value.getClass();
        Type targetType = returnType;

        if (value instanceof CharSequence) {
            body = value.toString();
            valueType = String.class;
            targetType = String.class;
        }

        MediaType selectedMediaType = null;
        MediaType contentType = outputMessage.getHeaders().getContentType();
        boolean isContentTypePreset = contentType != null && contentType.isConcrete();
        if (isContentTypePreset) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found 'Content-Type:" + contentType + "' in response");
            }
            selectedMediaType = contentType;
        } else {
            HttpServletRequest request = inputMessage.getServletRequest();
            List<MediaType> acceptableTypes = getAcceptableMediaTypes(request);
            List<MediaType> producibleTypes = getProducibleMediaTypes(request, valueType, targetType);

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
                if (logger.isDebugEnabled()) {
                    logger.debug("No match for " + acceptableTypes + ", supported: " + producibleTypes);
                }
                return;
            }

            MediaType.sortBySpecificityAndQuality(mediaTypesToUse);

            for (MediaType mediaType : mediaTypesToUse) {
                if (mediaType.isConcrete()) {
                    selectedMediaType = mediaType;
                    break;
                } else if (mediaType.isPresentIn(ALL_APPLICATION_MEDIA_TYPES)) {
                    selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Using '" + selectedMediaType + "', given " +
                        acceptableTypes + " and supported " + producibleTypes);
            }
        }

        if (selectedMediaType != null) {
            selectedMediaType = selectedMediaType.removeQualityValue();
            for (HttpMessageConverter<?> converter : this.converters) {
                GenericHttpMessageConverter genericConverter = (converter instanceof GenericHttpMessageConverter ?
                        (GenericHttpMessageConverter<?>) converter : null);
                if (genericConverter != null ?
                        ((GenericHttpMessageConverter) converter).canWrite(targetType, valueType, selectedMediaType) :
                        converter.canWrite(valueType, selectedMediaType)) {
                    if (body != null) {
                        Object theBody = body;
                        LogFormatUtils.traceDebug(logger, traceOn ->
                                "Writing [" + LogFormatUtils.formatValue(theBody, !traceOn) + "]");
                        addContentDispositionHeader(inputMessage, outputMessage);
                        if (genericConverter != null) {
                            genericConverter.write(body, targetType, selectedMediaType, outputMessage);
                        } else {
                            ((HttpMessageConverter) converter).write(body, selectedMediaType, outputMessage);
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Nothing to write: null body");
                        }
                    }
                    return;
                }
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
            throw new HttpMediaTypeNotAcceptableException(this.allSupportedMediaTypes);
        }
    }

    private List<MediaType> getAcceptableMediaTypes(HttpServletRequest request)
            throws HttpMediaTypeNotAcceptableException {

        return this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
    }

    protected List<MediaType> getProducibleMediaTypes(
            HttpServletRequest request, Class<?> valueClass, @Nullable Type targetType) {

        Set<MediaType> mediaTypes =
                (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            return new ArrayList<>(mediaTypes);
        } else if (!this.allSupportedMediaTypes.isEmpty()) {
            List<MediaType> result = new ArrayList<>();
            for (HttpMessageConverter<?> converter : this.converters) {
                if (converter instanceof GenericHttpMessageConverter && targetType != null) {
                    if (((GenericHttpMessageConverter<?>) converter).canWrite(targetType, valueClass, null)) {
                        result.addAll(converter.getSupportedMediaTypes());
                    }
                } else if (converter.canWrite(valueClass, null)) {
                    result.addAll(converter.getSupportedMediaTypes());
                }
            }
            return result;
        } else {
            return Collections.singletonList(MediaType.ALL);
        }
    }

    /**
     * Return the more specific of the acceptable and the producible media types
     * with the q-value of the former.
     */
    private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
        MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);
        return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse);
    }

    private void addContentDispositionHeader(ServletServerHttpRequest request, ServletServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        if (headers.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
            return;
        }

        try {
            int status = response.getServletResponse().getStatus();
            if (status < 200 || (status > 299 && status < 400)) {
                return;
            }
        } catch (Throwable ex) {
            // ignore
        }

        HttpServletRequest servletRequest = request.getServletRequest();
        String requestUri = UrlPathHelper.rawPathInstance.getOriginatingRequestUri(servletRequest);

        int index = requestUri.lastIndexOf('/') + 1;
        String filename = requestUri.substring(index);
        String pathParams = "";

        index = filename.indexOf(';');
        if (index != -1) {
            pathParams = filename.substring(index);
            filename = filename.substring(0, index);
        }

        filename = UrlPathHelper.defaultInstance.decodeRequestString(servletRequest, filename);
        String ext = StringUtils.getFilenameExtension(filename);

        pathParams = UrlPathHelper.defaultInstance.decodeRequestString(servletRequest, pathParams);
        String extInPathParams = StringUtils.getFilenameExtension(pathParams);

        if (!safeExtension(servletRequest, ext) || !safeExtension(servletRequest, extInPathParams)) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=f.txt");
        }
    }

    private boolean safeExtension(HttpServletRequest request, @Nullable String extension) {
        if (!StringUtils.hasText(extension)) {
            return true;
        }
        extension = extension.toLowerCase(Locale.ENGLISH);
        if (this.safeExtensions.contains(extension)) {
            return true;
        }
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern != null && pattern.endsWith("." + extension)) {
            return true;
        }
        if (extension.equals("html")) {
            String name = HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;
            Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(name);
            if (!CollectionUtils.isEmpty(mediaTypes) && mediaTypes.contains(MediaType.TEXT_HTML)) {
                return true;
            }
        }
        MediaType mediaType = resolveMediaType(request, extension);
        return (mediaType != null && (safeMediaType(mediaType)));
    }

    private MediaType resolveMediaType(ServletRequest request, String extension) {
        MediaType result = null;
        String rawMimeType = request.getServletContext().getMimeType("file." + extension);
        if (StringUtils.hasText(rawMimeType)) {
            result = MediaType.parseMediaType(rawMimeType);
        }
        if (result == null || MediaType.APPLICATION_OCTET_STREAM.equals(result)) {
            result = MediaTypeFactory.getMediaType("file." + extension).orElse(null);
        }
        return result;
    }

    private boolean safeMediaType(MediaType mediaType) {
        return (SAFE_MEDIA_BASE_TYPES.contains(mediaType.getType()) ||
                mediaType.getSubtype().endsWith("+xml"));
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
