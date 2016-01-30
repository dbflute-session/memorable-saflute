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
package org.dbflute.saflute.web.task.callback;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class TaskExecuteMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MethodInvocation methodInvocation;
    protected RuntimeException failureCause;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TaskExecuteMeta(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    // ===================================================================================
    //                                                                      Basic Resource
    //                                                                      ==============
    /**
     * Get the method object of task execute.
     * @return The method object from execute configuration. (NotNull)
     */
    public Method getTaskMethod() {
        return methodInvocation.getMethod();
    }

    // ===================================================================================
    //                                                                      Execute Status
    //                                                                      ==============
    /**
     * Does it have exception as failure cause?
     * @return The determination, true or false.
     */
    public boolean hasFailureCause() {
        return failureCause != null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return getTaskMethod().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getTaskMethod().equals(obj);
    }

    @Override
    public String toString() {
        return "{" + buildToStringContents() + "}";
    }

    protected String buildToStringContents() {
        final Method method = getTaskMethod();
        final String invoke = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        final String failure = failureCause != null ? DfTypeUtil.toClassTitle(failureCause) : null;
        return invoke + ":" + failure;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the teller of the execute configuration for the action execute.
     * @return The teller interface of the execute configuration. (NotNull)
     */
    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    /**
     * Get the exception as failure cause thrown by action execute.
     * @return The exception as failure cause. (NullAllowed: when before execute or on success)
     */
    public RuntimeException getFailureCause() {
        return failureCause;
    }

    public void setFailureCause(RuntimeException failureCause) {
        this.failureCause = failureCause;
    }
}
