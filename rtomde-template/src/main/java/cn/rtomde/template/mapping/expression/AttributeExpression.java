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

import io.cloudevents.CloudEvent;

/**
 * Defines a type of expression language statement that can be applied
 * parametrized by various attributes and properties as specified in each of
 * the method calls.
 */
public interface AttributeExpression {

    /**
     * @return Evaluates the expression without any additional attributes.
     * @throws ExpressionException if unable to evaluate
     */
    Object evaluate() throws ExpressionException;

    /**
     * Evaluates the expression providing access to additional variables.
     *
     * @param event to evaluate
     * @return evaluated value
     * @throws ExpressionException if failure evaluating
     */
    Object evaluate(CloudEvent event) throws ExpressionException;
}
