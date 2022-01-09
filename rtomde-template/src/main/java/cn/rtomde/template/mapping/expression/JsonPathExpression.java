package cn.rtomde.template.mapping.expression;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonCloudEventData;

import java.util.EnumSet;

public class JsonPathExpression implements AttributeExpression {

    private final String jsonPath;
    private final String defaultValue;

    public JsonPathExpression(String jsonPath, String defaultValue) {
        this.jsonPath = jsonPath;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object evaluate() throws ExpressionException {
        return evaluate(null);
    }

    /**
     * todo 属性 + data
     */
    @Override
    public Object evaluate(CloudEvent event) throws ExpressionException {
        if (event == null) {
            return defaultValue;
        }

        JsonCloudEventData jsonCloudEventData = (JsonCloudEventData) event.getData();
        Configuration configuration = Configuration.builder()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .options(EnumSet.noneOf(Option.class))
                .build();

        Object value = JsonPath.using(configuration)
                .parse(jsonCloudEventData.getNode().toString())
                .read(jsonPath);
        return value;
    }

    @Override
    public String toString() {
        return String.format("JsonPath[%s]", jsonPath);
    }
}
