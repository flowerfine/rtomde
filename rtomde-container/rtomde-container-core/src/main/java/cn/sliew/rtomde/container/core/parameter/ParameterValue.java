package cn.sliew.rtomde.container.core.parameter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public class ParameterValue {

    private final Optional<JsonNode> value;

    private final boolean init;

    public ParameterValue(Optional<JsonNode> value, boolean init) {
        this.value = value;
        this.init = init;
    }
}
