package cn.sliew.rtomde.platform.jdbc.script;

/**
 * present an script snippet which can construct sql by provided {@code ScriptingException}.
 */
public interface ScriptSource {

    /**
     * access script source.
     *
     * @return string script source without binding
     * @throws ScriptingException convert to script failure could result in exception
     */
    String getScriptAsString() throws ScriptingException;

    /**
     * construct sql by provided {@code parameterObject}
     *
     * @param parameterObject parameter object used for placeholders、conditional dynamic sql etc
     * @return script bound result
     */
    ScriptBoundResult bind(Object parameterObject);
}
