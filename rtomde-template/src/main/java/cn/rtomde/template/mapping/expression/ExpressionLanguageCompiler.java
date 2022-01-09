/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.rtomde.template.mapping.expression;

public interface ExpressionLanguageCompiler {

    /**
     * Compiles the given Attribute Expression string into an
     * AttributeExpression that can be evaluated
     *
     * @param expression the Attribute Expression to be compiled
     * @return expression that can be evaluated
     * @throws ExpressionException if the given expression is not valid
     */
    AttributeExpression compile(String expression) throws ExpressionException;

    /**
     * Indicates whether or not the given string is a valid Attribute
     * Expression.
     *
     * @param expression to validate
     * @return if is value or not
     */
    boolean isValidExpression(String expression);

    /**
     * Attempts to validate the given expression and returns <code>null</code>
     * if the expression is syntactically valid or a String indicating why the
     * expression is invalid otherwise.
     *
     * @param expression to validate
     *                   to surround the Expression, otherwise the expression must be exactly
     *                   equal to a valid Expression. E.g., <code>/${path}</code> is valid if and
     *                   only if <code>allowSurroundingCharacters</code> is true
     * @return a String indicating the reason that the expression is not
     * syntactically correct, or <code>null</code> if the expression is
     * syntactically correct
     */
    String validateExpression(String expression);
}
