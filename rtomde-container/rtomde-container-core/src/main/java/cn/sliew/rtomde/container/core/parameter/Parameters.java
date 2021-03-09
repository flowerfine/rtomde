package cn.sliew.rtomde.container.core.parameter;

import java.util.Map;

public class Parameters {

    private final Map<ParameterName, ParameterValue> params;

    public Parameters(Map<ParameterName, ParameterValue> params) {
        this.params = params;
    }
}
