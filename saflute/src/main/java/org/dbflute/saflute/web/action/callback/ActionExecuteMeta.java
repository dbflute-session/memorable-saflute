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
package org.dbflute.saflute.web.action.callback;

import java.lang.reflect.Method;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.web.action.api.ApiAction;
import org.dbflute.saflute.web.action.response.ApiResponse;
import org.dbflute.util.DfTypeUtil;
import org.seasar.struts.config.S2ExecuteConfig;

/**
 * @author jflute
 */
public class ActionExecuteMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ExecuteConfigTeller executeConfig;
    protected ActionForward executeForward;
    protected RuntimeException failureCause;
    protected ActionMessages validationErrors;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ActionExecuteMeta(S2ExecuteConfig executeConfig) {
        this.executeConfig = new ExecuteConfigTeller(executeConfig);
    }

    // ===================================================================================
    //                                                                      Basic Resource
    //                                                                      ==============
    /**
     * Get the method object of action execute.
     * @return The method object from execute configuration. (NotNull)
     */
    public Method getActionMethod() {
        return executeConfig.getMethod();
    }

    /**
     * Is the action for API request? (contains e.g. JSON response return type)
     * @return The determination, true or false.
     */
    public boolean isApiAction() {
        final Method actionMethod = getActionMethod();
        if (ApiResponse.class.isAssignableFrom(actionMethod.getReturnType())) {
            return true; // if JSON response, this action can be treated as API without the marker interface
        }
        return ApiAction.class.isAssignableFrom(actionMethod.getDeclaringClass());
    }

    // ===================================================================================
    //                                                                      Execute Status
    //                                                                      ==============
    /**
     * Is the result of the action execute, forward to JSP?
     * @return The determination, true or false.
     */
    public boolean isForwardToJsp() {
        if (executeForward == null) { // e.g. exception, AJAX
            return false;
        }
        if (executeForward.getRedirect()) { // to action
            return false;
        }
        final String path = executeForward.getPath();
        return path.endsWith(".jsp");
    }

    /**
     * Is the result of the action execute, redirect?
     * @return The determination, true or false.
     */
    public boolean isRedirectTo() {
        if (executeForward == null) { // e.g. exception, AJAX
            return false;
        }
        return executeForward.getRedirect();
    }

    /**
     * Does it have exception as failure cause?
     * @return The determination, true or false.
     */
    public boolean hasFailureCause() {
        return failureCause != null;
    }

    /**
     * Does it have any validation errors?
     * @return The determination, true or false.
     */
    public boolean hasValidationError() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return getExecuteConfig().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getExecuteConfig().equals(obj);
    }

    @Override
    public String toString() {
        return "{" + buildToStringContents() + "}";
    }

    protected String buildToStringContents() {
        final Method method = getExecuteConfig().getMethod();
        final String invoke = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        final String path = executeForward != null ? executeForward.getPath() : null;
        final String failure = failureCause != null ? DfTypeUtil.toClassTitle(failureCause) : null;
        return invoke + ":" + path + ":" + failure;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the teller of the execute configuration for the action execute.
     * @return The teller interface of the execute configuration. (NotNull)
     */
    public ExecuteConfigTeller getExecuteConfig() {
        return executeConfig;
    }

    /**
     * Get the action forward returned by action execute. <br>
     * It returns valid forward only after success action execute.
     * @return The action forward returned by action execute. (NullAllowed: not null only when success)
     */
    public ActionForward getExecuteForward() {
        return executeForward;
    }

    public void setExecuteForward(ActionForward executeForward) {
        this.executeForward = executeForward;
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

    /**
     * Get the messages as validation error.
     * @return The messages as validation error. (NullAllowed: when no validation error)
     */
    public ActionMessages getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(ActionMessages validationErrors) {
        this.validationErrors = validationErrors;
    }
}
