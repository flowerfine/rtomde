package cn.rtomde.template.mapping.template;

import cn.sliew.milky.test.MilkyTestCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.test.Data;
import io.cloudevents.jackson.JsonCloudEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MustacheTest extends MilkyTestCase {

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * {
     *     "id":1,
     *     "username":"wangqi",
     *     "password":"123456",
     *     "location":{
     *         "country":"中国",
     *         "province":"浙江",
     *         "city":"杭州"
     *     }
     * }
     */
    private CloudEvent event;

    @BeforeEach
    private void beforeEach() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("id", 1);
        objectNode.put("username", "wangqi");
        objectNode.put("password", "123456");
        objectNode.put("enabled", true);
        ObjectNode location = objectNode.putObject("location");
        location.put("country", "中国");
        location.put("province", "浙江");
        location.put("city", "杭州");

        event = CloudEventBuilder.v1(Data.V1_MIN)
                .withData("application/json", JsonCloudEventData.wrap(objectNode))
                .build();
    }

    /**
     * 测试 {{}} 插值表达式
     */
    @Test
    void testSimple() {
        String template = "One, two, {{username}}. Three sir!";
        Template tmpl = Mustache.compiler().compile(template);
        JsonCloudEventData data = (JsonCloudEventData) event.getData();
        Map<String, Object> map = mapper.convertValue(data.getNode(), new TypeReference<Map<String, Object>>() {});
        assertEquals("One, two, wangqi. Three sir!", tmpl.execute(map));
    }

    /**
     * {{#data}} {{/data}}
     * 以#开始、以/结束表示区块，它会根据当前上下文中的键值来对区块进行一次或多次渲染
     */
    @Test
    void testBlock() {
        JsonCloudEventData data = (JsonCloudEventData) event.getData();
        Map<String, Object> map = mapper.convertValue(data.getNode(), new TypeReference<Map<String, Object>>() {});
        String template1 = "{{#location}}国家: {{country}}, 省市: {{province}}, 城市: {{city}}{{/location}}";
        Template tmpl1 = Mustache.compiler().compile(template1);
        assertEquals("国家: 中国, 省市: 浙江, 城市: 杭州", tmpl1.execute(map));

        String template2 = "{{username}}:{{^enabled}}{{password}}{{/enabled}}";
        Template tmpl2 = Mustache.compiler().compile(template2);
        System.out.println(tmpl2.execute(map));

    }

    /**
     * {{^data}} {{/data}}
     */
    @Test
    void testOppositeBlock() {
        JsonCloudEventData data = (JsonCloudEventData) event.getData();
        Map<String, Object> map = mapper.convertValue(data.getNode(), new TypeReference<Map<String, Object>>() {});
        String template = "{{username}}:{{^enabled}}{{password}}{{/enabled}}";
        Template tmpl = Mustache.compiler().compile(template);
        System.out.println(tmpl.execute(map));
    }

    /**
     * {{>partials}}
     */
    @Test
    void testPartials() {
        JsonCloudEventData data = (JsonCloudEventData) event.getData();
        Map<String, Object> map = mapper.convertValue(data.getNode(), new TypeReference<Map<String, Object>>() {});
        Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
            public Reader getTemplate (String name) {
                return new StringReader("");
            }
        });
        String template = "{{username}}:{{^enabled}}{{password}}{{/enabled}}";
        Template tmpl = Mustache.compiler().compile(template);
        System.out.println(tmpl.execute(map));
    }



}
