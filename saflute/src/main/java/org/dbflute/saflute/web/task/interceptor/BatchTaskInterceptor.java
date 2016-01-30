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

import java.lang.annotation.Annotation;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.struts.action.ActionForward;
import org.dbflute.saflute.core.interceptor.ControllableBaseInterceptor;
import org.dbflute.saflute.core.magic.ThreadCacheContext;
import org.dbflute.saflute.web.task.callback.TaskCallback;
import org.dbflute.saflute.web.task.callback.TaskExecuteMeta;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class BatchTaskInterceptor extends ControllableBaseInterceptor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                              Invoke
    //                                                                              ======
    protected Object doAllowedInvoke(MethodInvocation invocation) throws Throwable {
        try {
            ThreadCacheContext.initialize(); // and also used by nested invoke check
            return processExecute(invocation);
        } finally {
            ThreadCacheContext.clear();
        }
    }

    protected Object processExecute(MethodInvocation invocation) throws Throwable {
        final String methodName = invocation.getMethod().getName();
        final Object task = invocation.getThis();
        final boolean doMethod = methodName.startsWith("do");

        TaskCallback callback = null;
        TaskExecuteMeta meta = null;
        if (doMethod && task instanceof TaskCallback) {
            callback = (TaskCallback) task;
            meta = new TaskExecuteMeta(invocation);
        }

        processCallbackBefore(callback, meta);
        try {
            final Object result = doActuallyExecute(invocation, doMethod);
            processCallbackOnSuccess(callback, meta);
            return result;
        } catch (RuntimeException e) {
            if (meta != null) {
                meta.setFailureCause(e);
            }
            if (processCallbackOnFailure(callback, meta)) {
                return null;
            }
            throw e;
        } finally {
            processCallbackFinally(callback, meta);
        }
    }

    protected Object doActuallyExecute(MethodInvocation invocation, boolean doMethod) throws Throwable {
        return invocation.proceed();
    }

    // ===================================================================================
    //                                                                    Callback Process
    //                                                                    ================
    // -----------------------------------------------------
    //                                                Before
    //                                                ------
    protected void processCallbackBefore(TaskCallback callback, TaskExecuteMeta executeMeta) {
        if (callback == null) {
            return;
        }
        godHandActionPrologue(callback, executeMeta);
        godHandBefore(callback, executeMeta);
        callbackBefore(callback, executeMeta);
    }

    protected void godHandActionPrologue(TaskCallback callback, TaskExecuteMeta executeMeta) {
        callback.godHandActionPrologue(executeMeta);
    }

    protected void godHandBefore(TaskCallback callback, TaskExecuteMeta executeMeta) {
        callback.godHandBefore(executeMeta);
    }

    protected void callbackBefore(TaskCallback callback, TaskExecuteMeta executeMeta) {
        callback.callbackBefore(executeMeta);
    }

    // -----------------------------------------------------
    //                                            on Success
    //                                            ----------
    protected ActionForward processCallbackOnSuccess(TaskCallback callback, TaskExecuteMeta meta) {
        return null; // no callback for now 
    }

    // -----------------------------------------------------
    //                                            on Failure
    //                                            ----------
    protected boolean processCallbackOnFailure(TaskCallback callback, TaskExecuteMeta meta) {
        if (callback == null) {
            return false;
        }
        // only monologue for now
        return godHandExceptionMonologue(callback, meta);
    }

    protected boolean godHandExceptionMonologue(TaskCallback callback, TaskExecuteMeta meta) {
        return callback.godHandExceptionMonologue(meta);
    }

    // -----------------------------------------------------
    //                                               Finally
    //                                               -------
    protected void processCallbackFinally(TaskCallback callback, TaskExecuteMeta meta) {
        if (callback == null) {
            return;
        }
        try {
            callback.callbackFinally(meta);
        } finally {
            try {
                callback.godHandFinally(meta);
            } finally {
                callback.godHandActionEpilogue(meta);
            }
        }
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    protected boolean isNestedInvoke(MethodInvocation invocation) {
        return ThreadCacheContext.exists();
    }

    protected Class<? extends Annotation> getBasePointcutAnnotationType() {
        return null;
    }

    protected List<Class<? extends Annotation>> getHasAnyPointcutAnnotationTypeList() {
        return DfCollectionUtil.emptyList();
    }
}
