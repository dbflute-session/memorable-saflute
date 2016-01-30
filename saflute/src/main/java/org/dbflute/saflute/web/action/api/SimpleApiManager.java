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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.json.JsonManager;
import org.dbflute.saflute.web.action.OptionalActionDirection;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.dbflute.saflute.web.servlet.request.ResponseManager;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SimpleApiManager implements ApiManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimpleApiManager.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The manager of JSON. (NotNull: after initialization) */
    @Resource
    protected JsonManager jsonManager;

    /** The manager of response. (NotNull: after initialization) */
    @Resource
    protected ResponseManager responseManager;

    /** The provider of API result. (NotNull: after initialization) */
    protected ApiResultProvider apiResultProvider;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalActionDirection direction = assistOptionalActionDirection();
        final ApiResultProvider assistedProvider = direction.assistApiResultProvider();
        if (assistedProvider != null) {
            apiResultProvider = assistedProvider;
        } else {
            apiResultProvider = new UnsupportedApiResultProvider();
        }
        showBootLogging();
    }

    protected OptionalActionDirection assistOptionalActionDirection() {
        return assistantDirector.assistOptionalActionDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[API Manager]");
            LOG.info(" apiResultProvider: " + DfTypeUtil.toClassTitle(apiResultProvider));
        }
    }

    // ===================================================================================
    //                                                                       Create Result
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public ApiResult prepareLoginRequiredFailureResult(ActionMessages errors, ActionExecuteMeta meta) {
        return apiResultProvider.prepareLoginRequiredFailureResult(errors, meta);
    }

    /**
     * {@inheritDoc}
     */
    public ApiResult prepareLoginPerformRedirectResult(ActionMessages errors, ActionExecuteMeta meta) {
        return apiResultProvider.prepareLoginPerformRedirectResult(errors, meta);
    }

    /**
     * {@inheritDoc}
     */
    public ApiResult prepareValidationErrorResult(ActionMessages errors, ActionExecuteMeta meta) {
        return apiResultProvider.prepareValidationErrorResult(errors, meta);
    }

    /**
     * {@inheritDoc}
     */
    public ApiResult prepareApplicationExceptionResult(ActionMessages errors, ActionExecuteMeta meta,
            RuntimeException cause) {
        return apiResultProvider.prepareApplicationExceptionResult(errors, meta, cause);
    }

    /**
     * {@inheritDoc}
     */
    public ApiResult prepareSystemExceptionResult(HttpServletResponse response, ActionExecuteMeta executeMeta,
            Throwable cause) {
        return apiResultProvider.prepareSystemExceptionResult(response, executeMeta, cause);
    }

    // ===================================================================================
    //                                                                       Forward Dummy
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public String forwardToApiResolvedDummy() {
        return API_RESOLVED_DUMMY_FORWARD;
    }

    // ===================================================================================
    //                                                                      Write Response
    //                                                                      ==============
    /**
     * {@inheritDoc}
     */
    public void writeJsonResponse(Object result) {
        responseManager.writeAsJson(convertToJson(result));
    }

    /**
     * Convert the source object to JSON string.
     * @param bean The instance of bean to encode. (NotNull)
     * @return The encoded JSON string. (NotNull)
     */
    protected String convertToJson(Object bean) {
        return jsonManager.convertToJson(bean);
    }
}
