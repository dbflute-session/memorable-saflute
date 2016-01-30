/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.saflute.db.jta.interceptor;

import java.lang.reflect.Method;

import javax.ejb.TransactionAttribute;

/**
 * The customizer of transaction attribute by explicitly adding annotation. <br>
 * You can use transaction only when you add {@link TransactionAttribute} to your methods.
 * @author jflute
 */
public class ExplicitTxAttributeCustomizer extends PointTxAttributeBaseCustomizer {

    @Override
    protected boolean isImplicitTxSupportedMethod(Method method) {
        return false;
    }
}
