package cn.sliew.rtomde.platform.mybatis.io;

import cn.sliew.rtomde.platform.mybatis.logging.Log;
import cn.sliew.rtomde.platform.mybatis.logging.LogFactory;

import java.security.Security;

public final class SerialFilterChecker {
    private static final Log log = LogFactory.getLog(SerialFilterChecker.class);
    /* Property key for the JEP-290 serialization filters */
    private static final String JDK_SERIAL_FILTER = "jdk.serialFilter";
    private static final boolean SERIAL_FILTER_MISSING;
    private static boolean firstInvocation = true;

    static {
        Object serialFilter;
        try {
            Class<?> objectFilterConfig = Class.forName("java.io.ObjectInputFilter$Config");
            serialFilter = objectFilterConfig.getMethod("getSerialFilter").invoke(null);
        } catch (ReflectiveOperationException e) {
            // Java 1.8
            serialFilter = System.getProperty(JDK_SERIAL_FILTER, Security.getProperty(JDK_SERIAL_FILTER));
        }
        SERIAL_FILTER_MISSING = serialFilter == null;
    }

    public static void check() {
        if (firstInvocation && SERIAL_FILTER_MISSING) {
            firstInvocation = false;
            log.warn(
                    "As you are using functionality that deserializes object streams, it is recommended to define the JEP-290 serial filter. "
                            + "Please refer to https://docs.oracle.com/pls/topic/lookup?ctx=javase15&id=GUID-8296D8E8-2B93-4B9A-856E-0A65AF9B8C66");
        }
    }

    private SerialFilterChecker() {
    }
}
