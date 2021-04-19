package cn.sliew.rtomde.platform.function;

public interface StatefulFunctionProvider {

    StatefulFunction functionOfType(FunctionType type);
}