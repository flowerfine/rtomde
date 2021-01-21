package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.resource.Resources;
import cn.sliew.rtomde.common.xml.XPathParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;

public class XMLMapperBuilderTest {

    @Test
    public void testParse() throws Exception {
        InputStream inputStream = Resources.getResourceAsStream("PostMapper.xml");
        XPathParser xPathParser = new XPathParser(inputStream, true, System.getProperties(), new XMLMapperEntityResolver());
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(xPathParser, "PostMapper.xml", Collections.emptyMap());

    }

}
