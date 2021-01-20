package cn.sliew.rtomde.bind;

public interface ParameterHandler {

    /**
     * json object
     */
    Object getParameterObject();

    void setParameters(QueryStatement statement);
}
