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
package org.dbflute.saflute.web.action.api;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;

/**
 * The provider of API result.
 * @author jflute
 */
public interface ApiResultProvider {

    /**
     * Prepare API result when login required failure.
     * @param errors The action message for errors, but basically no errors. (NullAllowed)
     * @param executeMeta The meta of action execute for the current request. (NotNull)
     * @return The new-created API result object, which is converted to JSON. (NotNull)
     */
    ApiResult prepareLoginRequiredFailureResult(ActionMessages errors, ActionExecuteMeta executeMeta);

    /**
     * Prepare API result when login-perform redirect.
     * @param errors The action message for errors, but basically no errors. (NullAllowed)
     * @param executeMeta The meta of action execute for the current request. (NotNull)
     * @return The new-created API result object, which is converted to JSON. (NotNull)
     */
    ApiResult prepareLoginPerformRedirectResult(ActionMessages errors, ActionExecuteMeta executeMeta);

    /**
     * Prepare API result when validation error.
     * @param errors The action message for validation errors. (NotNull, NotEmpty)
     * @param executeMeta The meta of action execute for the current request. (NotNull)
     * @return The new-created API result object, which is converted to JSON. (NotNull)
     */
    ApiResult prepareValidationErrorResult(ActionMessages errors, ActionExecuteMeta executeMeta);

    /**
     * Prepare API result when application exception.
     * @param errors The action message for validation errors. (basically NotNull: but might be null in minor case)
     * @param executeMeta The meta of action execute for the current request. (NotNull)
     * @param cause The exception thrown by (basically) action execute, might be translated. (NotNull)
     * @return The new-created API result object, which is converted to JSON. (NotNull)
     */
    ApiResult prepareApplicationExceptionResult(ActionMessages errors, ActionExecuteMeta executeMeta,
            RuntimeException cause);

    /**
     * Prepare API result when system exception. (Not Required)
     * @param response The HTTP response that is not committed yet. (NotNull)
     * @param executeMeta The meta of action execute for the current request. (NotNull)
     * @param cause The exception thrown by (basically) action execute, might be translated. (NotNull)
     * @return The new-created API result object, which is converted to JSON. (NullAllowed: if null, default handling about it)
     */
    ApiResult prepareSystemExceptionResult(HttpServletResponse response, ActionExecuteMeta executeMeta, Throwable cause);
}
