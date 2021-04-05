package cn.sliew.rtomde.service.bootstrap;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.dispatcher.MapperDispatcher;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AkkaHttpRoute extends AllDirectives {

    private final Map<String, Map<JsonNode, JsonNode>> state = new ConcurrentHashMap<>();

    @Autowired
    private MapperDispatcher mapperDispatcher;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    Route fixedRoute() {
        return complete(StatusCodes.OK, "hhh");
    }

    Route dynamicRoute() {
        Route fixedRoute = post(() ->
                pathSingleSlash(() ->
                        entity(Jackson.unmarshaller(MockDefinition.class), mock -> {
                            Map<JsonNode, JsonNode> mappings = new HashMap<>();
                            int size = Math.min(mock.getRequests().size(), mock.getResponses().size());
                            for (int i = 0; i < size; i++) {
                                mappings.put(mock.getRequests().get(i), mock.getResponses().get(i));
                            }
                            state.put(mock.getPath(), mappings);
                            return complete("ok");
                        })
                )
        );

        // dynamic routing based on current state
        Route dynamicRoute = post(() ->
                state.entrySet().stream().map(mock ->
                        path(mock.getKey(), () ->
                                entity(Jackson.unmarshaller(JsonNode.class), input ->
                                        complete(StatusCodes.OK, mock.getValue().get(input), Jackson.marshaller())
                                )
                        )
                ).reduce(reject(), Route::orElse)
        );

        return concat(fixedRoute, dynamicRoute);
    }

    private static class MockDefinition {
        private final String path;
        private final List<JsonNode> requests;
        private final List<JsonNode> responses;

        public MockDefinition(@JsonProperty("path") String path,
                              @JsonProperty("requests") List<JsonNode> requests,
                              @JsonProperty("responses") List<JsonNode> responses) {
            this.path = path;
            this.requests = requests;
            this.responses = responses;
        }

        public String getPath() {
            return path;
        }

        public List<JsonNode> getRequests() {
            return requests;
        }

        public List<JsonNode> getResponses() {
            return responses;
        }
    }

}
