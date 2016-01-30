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
import javax.ejb.TransactionAttributeType;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.customizer.TxAttributeCustomizer;
import org.seasar.framework.container.factory.AspectDefFactory;
import org.seasar.framework.util.StringUtil;

/**
 * @author jflute
 */
public abstract class PointTxAttributeBaseCustomizer extends TxAttributeCustomizer {

    @Override
    protected void doCustomize(final ComponentDef componentDef) {
        final Class<?> componentClass = componentDef.getComponentClass();
        final TransactionAttribute classAttr = componentClass.getAnnotation(TransactionAttribute.class);
        final TransactionAttributeType classAttrType = classAttr != null ? classAttr.value() : defaultAttributeType;
        for (final Method method : componentClass.getMethods()) {
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            final TransactionAttribute methodAttr = method.getAnnotation(TransactionAttribute.class);
            if (isOutOfTxMethod(classAttr, methodAttr, method)) {
                continue;
            }
            // action execute or has transaction attribute either class or method here
            final TransactionAttributeType methodAttrType = methodAttr != null ? methodAttr.value() : classAttrType;
            final String interceptorName = txInterceptors.get(methodAttrType);
            if (!StringUtil.isEmpty(interceptorName)) {
                componentDef.addAspectDef(AspectDefFactory.createAspectDef(interceptorName, method));
            }
        }
    }

    protected boolean isOutOfTxMethod(TransactionAttribute classAttr, TransactionAttribute methodAttr, Method method) {
        return classAttr == null && methodAttr == null && !isImplicitTxSupportedMethod(method);
    }

    protected abstract boolean isImplicitTxSupportedMethod(Method method);
}
