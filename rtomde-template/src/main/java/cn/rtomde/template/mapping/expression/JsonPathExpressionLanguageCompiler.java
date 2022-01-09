package cn.rtomde.template.mapping.expression;

public class JsonPathExpressionLanguageCompiler implements ExpressionLanguageCompiler {

    @Override
    public AttributeExpression compile(String expression) throws ExpressionException {
        return new JsonPathExpression(expression, null);
    }

    @Override
    public boolean isValidExpression(String expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String validateExpression(String expression) {
        throw new UnsupportedOperationException();
    }
}
