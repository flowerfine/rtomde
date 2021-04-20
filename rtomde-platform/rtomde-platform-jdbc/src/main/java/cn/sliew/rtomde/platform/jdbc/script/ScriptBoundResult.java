package cn.sliew.rtomde.platform.jdbc.script;

import cn.sliew.rtomde.platform.jdbc.parameter.ParameterMapping;

import java.util.List;
import java.util.Objects;

public class ScriptBoundResult {

    private final String script;
    private final List<ParameterMapping> parameterMappings;
    private final Object parameterObject;

    public ScriptBoundResult(String script, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.script = script;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
    }

    public String getScript() {
        return script;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScriptBoundResult that = (ScriptBoundResult) o;
        return Objects.equals(script, that.script) &&
                Objects.equals(parameterMappings, that.parameterMappings) &&
                Objects.equals(parameterObject, that.parameterObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, parameterMappings, parameterObject);
    }

}
