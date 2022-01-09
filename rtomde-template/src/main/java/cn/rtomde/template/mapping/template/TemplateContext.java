package cn.rtomde.template.mapping.template;

import cn.rtomde.template.mapping.ParameterMap;
import cn.sliew.milky.log.Logger;

public class TemplateContext {

    private Logger logger;
    private ParameterMap parameterMap;

    public TemplateContext(Logger logger, ParameterMap parameterMap) {
        this.logger = logger;
        this.parameterMap = parameterMap;
    }

    public Logger getLogger() {
        return logger;
    }

    public ParameterMap getParameterMap() {
        return parameterMap;
    }
}
