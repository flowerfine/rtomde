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
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, "PostMapper.xml", Collections.emptyMap());
        xmlMapperBuilder.parse();
    }

}
