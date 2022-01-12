package cn.rtomde.template.mapping.template;

import cn.rtomde.template.executor.ExecuteContext;
import cn.rtomde.template.executor.SimpleExecutor;
import cn.rtomde.template.mapping.*;
import cn.rtomde.template.mapping.expression.ExpressionLanguageCompiler;
import cn.rtomde.template.mapping.expression.JsonPathExpressionLanguageCompiler;
import cn.rtomde.template.mapping.template.simple.SimpleTemplateEngine;
import cn.rtomde.template.session.RowBounds;
import cn.rtomde.template.type.IntegerTypeHandler;
import cn.rtomde.template.type.JdbcType;
import cn.rtomde.template.type.StringTypeHandler;
import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.milky.test.MilkyTestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.test.Data;
import io.cloudevents.jackson.JsonCloudEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

class SimpleTemplateEngineTest extends MilkyTestCase {

    private Logger logger;
    private TemplateEngine templateEngine;

    private ExpressionLanguageCompiler expressionLanguageCompiler;

    private ParameterMap parameterMap;
    private ResultMap resultMap;
    private SqlTemplate sqlTemplate;

    private MappedStatement ms;

    @BeforeEach
    private void beforeEach() {
        logger = LoggerFactory.getLogger(SimpleTemplateEngineTest.class);
        templateEngine = new SimpleTemplateEngine();
        expressionLanguageCompiler = new JsonPathExpressionLanguageCompiler();

        ParameterMapping id = ParameterMapping.builder()
                .property("id")
                .javaType(Integer.TYPE)
                .jdbcType(JdbcType.BIGINT)
                .typeHandler(new IntegerTypeHandler())
                .expression(expressionLanguageCompiler.compile("$.id"))
                .build();
        ParameterMapping username = ParameterMapping.builder()
                .property("username")
                .javaType(String.class)
                .jdbcType(JdbcType.VARCHAR)
                .typeHandler(new StringTypeHandler())
                .expression(expressionLanguageCompiler.compile("$.username"))
                .build();
        ParameterMapping password = ParameterMapping.builder()
                .property("password")
                .javaType(String.class)
                .jdbcType(JdbcType.VARCHAR)
                .typeHandler(new StringTypeHandler())
                .expression(expressionLanguageCompiler.compile("$.password"))
                .build();
        parameterMap = ParameterMap.builder()
                .id("UserParamMap")
                .parameterMapping(id, username, password)
                .build();

        this.resultMap = ResultMapHelper.userResultMap();
        TemplateContext context = new TemplateContext(logger, parameterMap);
        this.sqlTemplate = templateEngine.create(context, "select id, username, password from sys_user where id = ${id} or username = #{username}");

        this.ms = MappedStatement.builder()
                .id("selectById")
                .sqlTemplate(sqlTemplate)
                .parameterMap(parameterMap)
                .resultMap(resultMap)
                .sqlCommandType(SqlCommandType.SELECT)
                .statementType(StatementType.PREPARED)
                .statementLog(logger)
                .timeout(3)
                .build();
    }

    @Test
    void testParse() throws SQLException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("id", 1);
        objectNode.put("username", "wangqi");
        objectNode.put("password", "123456");

        CloudEvent event = CloudEventBuilder.v1(Data.V1_MIN)
                .withData("application/json", JsonCloudEventData.wrap(objectNode))
                .build();
        BoundSql boundSql = sqlTemplate.bind(event);

        SimpleExecutor executor = new SimpleExecutor();
        ExecuteContext executeContext = new ExecuteContext(ms, boundSql, RowBounds.DEFAULT);
        List<Object> query = executor.query(executeContext);

        System.out.println(objectMapper.writeValueAsString(query));
    }
}
