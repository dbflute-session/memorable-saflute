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
package org.dbflute.saflute.core.interceptor;

import org.seasar.framework.container.customizer.AspectCustomizer;
import org.seasar.framework.container.customizer.CustomizerChain;

/**
 * @author jflute
 */
public class ConcreteDrivenCustomizerChain extends CustomizerChain {

    @Override
    public void addAspectCustomizer(final String interceptorName) {
        final AspectCustomizer customizer = newAspectCustomizer();
        customizer.setInterceptorName(interceptorName);
        addCustomizer(customizer);
    }

    @Override
    public void addAspectCustomizer(final String interceptorName, final String pointcut) {
        final AspectCustomizer customizer = newAspectCustomizer();
        customizer.setInterceptorName(interceptorName);
        customizer.setPointcut(pointcut);
        addCustomizer(customizer);
    }

    @Override
    public void addAspectCustomizer(final String interceptorName, final boolean useLookupAdapter) {
        final AspectCustomizer customizer = newAspectCustomizer();
        customizer.setInterceptorName(interceptorName);
        customizer.setUseLookupAdapter(useLookupAdapter);
        addCustomizer(customizer);
    }

    @Override
    public void addAspectCustomizer(final String interceptorName, final String pointcut, final boolean useLookupAdapter) {
        final AspectCustomizer customizer = newAspectCustomizer();
        customizer.setInterceptorName(interceptorName);
        customizer.setPointcut(pointcut);
        customizer.setUseLookupAdapter(useLookupAdapter);
        addCustomizer(customizer);
    }

    protected AspectCustomizer newAspectCustomizer() {
        return new ConcreteDrivenAspectCustomizer();
    }
}
