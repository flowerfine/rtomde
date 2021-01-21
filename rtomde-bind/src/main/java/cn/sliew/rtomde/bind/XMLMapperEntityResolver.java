package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.resource.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Offline entity resolver for the Rtomde DTDs.
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final String RTOMDE_CONFIG_SYSTEM = "rtomde-1-config.dtd";
    private static final String RTOMDE_MAPPER_SYSTEM = "rtomde-1-mapper.dtd";

    private static final String RTOMDE_CONFIG_DTD = "cn/sliew/rtomde/builder/xml/rtomde-1-config.dtd";
    private static final String RTOMDE_MAPPER_DTD = "cn/sliew/rtomde/builder/xml/rtomde-1-mapper.dtd";

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
                if (lowerCaseSystemId.contains(RTOMDE_CONFIG_SYSTEM)) {
                    return getInputSource(RTOMDE_CONFIG_DTD, publicId, systemId);
                } else if (lowerCaseSystemId.contains(RTOMDE_MAPPER_SYSTEM)) {
                    return getInputSource(RTOMDE_MAPPER_DTD, publicId, systemId);
                }
            }
            return null;
        } catch (Exception e) {
            throw new SAXException(e);
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
