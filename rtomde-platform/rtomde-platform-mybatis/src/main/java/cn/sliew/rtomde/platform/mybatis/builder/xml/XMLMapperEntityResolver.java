package cn.sliew.rtomde.platform.mybatis.builder.xml;

import cn.sliew.rtomde.platform.mybatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Offline entity resolver for the MyBatis DTDs.
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final String MYBATIS_PLATFORM_SYSTEM = "mybatis-metadata-1.dtd";
    private static final String MYBATIS_APPLICATION_SYSTEM = "mybatis-application-1.dtd";
    private static final String MYBATIS_MAPPER_SYSTEM = "mybatis-mapper-1.dtd";

    private static final String MYBATIS_PLATFORM_DTD = "cn/sliew/rtomde/platform/mybatis/builder/xml/mybatis-metadata-1.dtd";
    private static final String MYBATIS_APPLICATION_DTD = "cn/sliew/rtomde/platform/mybatis/builder/xml/mybatis-application-1.dtd";
    private static final String MYBATIS_MAPPER_DTD = "cn/sliew/rtomde/platform/mybatis/builder/xml/mybatis-mapper-1.dtd";

    /**
     * Converts a public DTD into a local one.
     *
     * @param publicId The public id that is what comes after "PUBLIC"
     * @param systemId The system id that is what comes after the public id.
     * @return The InputSource for the DTD
     * @throws SAXException If anything goes wrong
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        try {
            if (systemId != null) {
                String lowerCaseSystemId = systemId.toLowerCase(Locale.ENGLISH);
                if (lowerCaseSystemId.contains(MYBATIS_PLATFORM_SYSTEM)) {
                    return getInputSource(MYBATIS_PLATFORM_DTD, publicId, systemId);
                } else if (lowerCaseSystemId.contains(MYBATIS_APPLICATION_SYSTEM)) {
                    return getInputSource(MYBATIS_APPLICATION_DTD, publicId, systemId);
                } else if (lowerCaseSystemId.contains(MYBATIS_MAPPER_SYSTEM)) {
                    return getInputSource(MYBATIS_MAPPER_DTD, publicId, systemId);
                }
            }
            return null;
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
    }

    private InputSource getInputSource(String path, String publicId, String systemId) {
        InputSource source = null;
        if (path != null) {
            try {
                InputStream in = Resources.getResourceAsStream(path);
                source = new InputSource(in);
                source.setPublicId(publicId);
                source.setSystemId(systemId);
            } catch (IOException e) {
                // ignore, null is ok
            }
        }
        return source;
    }

}
