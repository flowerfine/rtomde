package cn.sliew.rtomde.container.core;

import cn.sliew.rtomde.container.core.execute.ExecMode;

/**
 * 代码存在于文件系统当中，当执行时需要执行拉取操作。
 * 在代码的文件系统url中包含了代码的版本管理功能。
 *{
 *   mode  : one of supported exec runtimes,
 *   code  : code to execute if kind is supported,
 *   main  : name of the entry point function, when using a non-default value (for Java, the name of the main class)"
 *}
 * todo compute exectable function size to prevent too large code.
 */
public abstract class ExecFunction {

    private final ExecMode mode;

    private final String codeUrl;

    private final boolean deprecated;

    public ExecFunction(ExecMode mode, String codeUrl, boolean deprecated) {
        this.mode = mode;
        this.codeUrl = codeUrl;
        this.deprecated = deprecated;
    }

    public ExecMode getMode() {
        return mode;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public boolean isDeprecated() {
        return deprecated;
    }
}
