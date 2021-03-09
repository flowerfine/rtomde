package cn.sliew.rtomde.container.core.execute;

import cn.sliew.rtomde.container.core.ExecFunction;

import java.util.Optional;

public class MybatisTemplateExec extends ExecFunction {

    /**
     * An entrypoint (typically name of 'main' function)
     */
    private final Optional<String> entryPoint;

    public MybatisTemplateExec(ExecMode mode, String codeUrl, boolean deprecated, Optional<String> entryPoint) {
        super(mode, codeUrl, deprecated);
        this.entryPoint = entryPoint;
    }
}
