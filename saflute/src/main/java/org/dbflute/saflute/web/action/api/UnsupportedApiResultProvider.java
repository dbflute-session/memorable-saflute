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
 * @author jflute
 */
public class UnsupportedApiResultProvider implements ApiResultProvider {

    public ApiResult prepareLoginRequiredFailureResult(ActionMessages errors, ActionExecuteMeta executeMeta) {
        throw new UnsupportedOperationException("API not supported yet: " + executeMeta);
    }

    public ApiResult prepareLoginPerformRedirectResult(ActionMessages errors, ActionExecuteMeta executeMeta) {
        throw new UnsupportedOperationException("API not supported yet: " + executeMeta);
    }

    public ApiResult prepareValidationErrorResult(ActionMessages errors, ActionExecuteMeta executeMeta) {
        throw new UnsupportedOperationException("API not supported yet: " + executeMeta);
    }

    public ApiResult prepareApplicationExceptionResult(ActionMessages errors, ActionExecuteMeta executeMeta,
            RuntimeException cause) {
        throw new UnsupportedOperationException("API not supported yet: " + executeMeta);
    }

    public ApiResult prepareSystemExceptionResult(HttpServletResponse response, ActionExecuteMeta executeMeta,
            Throwable cause) {
        throw new UnsupportedOperationException("API not supported yet: " + executeMeta);
    }
}
