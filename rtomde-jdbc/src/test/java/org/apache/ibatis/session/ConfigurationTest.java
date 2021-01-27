package org.apache.ibatis.session;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;

public class ConfigurationTest {

    @Test
    public void testUrl() throws Exception {
//        URL resourceURL = Resources.getResourceURL("org/apache/ibatis/session/SysUserMapper.xml");
//        InputStream inputStream = resourceURL.openStream();
//
//        System.out.println(inputStream.available());
    }

    @Test
    public void test() throws Exception {
        String resource = "org/apache/ibatis/session/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
            Configuration config = builder.parse();

        }
    }
}
