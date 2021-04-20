package cn.sliew.rtomde.platform.jdbc.script;

import java.io.IOException;

public interface ScriptSourceProvider {

    ScriptSource create(ScriptContext context) throws IOException;

}
