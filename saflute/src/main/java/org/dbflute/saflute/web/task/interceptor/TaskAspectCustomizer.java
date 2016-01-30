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
package org.dbflute.saflute.web.task.interceptor;

import org.dbflute.saflute.core.interceptor.ConcreteDrivenAspectCustomizer;
import org.seasar.framework.aop.Pointcut;

/**
 * @author jflute
 */
public class TaskAspectCustomizer extends ConcreteDrivenAspectCustomizer {

    @Override
    protected Pointcut createDefaultPointcut() {
        return new TaskDefaultPointcut();
    }
}
