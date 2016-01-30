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
package org.dbflute.saflute.web.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dbflute.saflute.core.direction.exception.FwRequiredAssistNotFoundException;
import org.dbflute.saflute.web.action.api.ApiResultProvider;
import org.dbflute.saflute.web.action.processor.ActionAdjustmentProvider;
import org.dbflute.saflute.web.action.response.ActionResponseHandler;

/**
 * @author jflute
 */
public class OptionalActionDirection {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                            Adjustment
    //                                            ----------
    protected ActionAdjustmentProvider actionAdjustmentProvider;

    // -----------------------------------------------------
    //                                               Message
    //                                               -------
    protected String domainMessageName;
    protected final List<String> extendsMessageNameList = new ArrayList<String>(4);

    // -----------------------------------------------------
    //                                              API Call
    //                                              --------
    protected ApiResultProvider apiResultProvider;

    // -----------------------------------------------------
    //                                       Action Response
    //                                       ---------------
    protected ActionResponseHandler actionResponseHandler;

    // ===================================================================================
    //                                                                     Direct Property
    //                                                                     ===============
    // -----------------------------------------------------
    //                                            Adjustment
    //                                            ----------
    public void directAdjustment(ActionAdjustmentProvider actionAdjustmentProvider) {
        this.actionAdjustmentProvider = actionAdjustmentProvider;
    }

    // -----------------------------------------------------
    //                                               Message
    //                                               -------
    public void directMessage(String domainMessageName, String... extendsMessageNames) {
        this.domainMessageName = domainMessageName;
        if (extendsMessageNames != null && extendsMessageNames.length > 0) {
            this.extendsMessageNameList.addAll(Arrays.asList(extendsMessageNames));
        }
    }

    // -----------------------------------------------------
    //                                              API Call
    //                                              --------
    public void directApiCall(ApiResultProvider apiResultProvider) {
        this.apiResultProvider = apiResultProvider;
    }

    // -----------------------------------------------------
    //                                       Action Response
    //                                       ---------------
    public void directActionResponse(ActionResponseHandler actionResponseHandler) {
        this.actionResponseHandler = actionResponseHandler;
    }

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    // -----------------------------------------------------
    //                                            Adjustment
    //                                            ----------
    public ActionAdjustmentProvider assistActionAdjustmentProvider() {
        if (actionAdjustmentProvider == null) {
            String msg = "Not found the provider of action adjustment.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return actionAdjustmentProvider;
    }

    // -----------------------------------------------------
    //                                               Message
    //                                               -------
    public String assistDomainMessageName() {
        if (domainMessageName == null) {
            String msg = "Not found the (file without extension) name for domain message.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return domainMessageName;
    }

    public List<String> assistExtendsMessageNameList() {
        return extendsMessageNameList;
    }

    // -----------------------------------------------------
    //                                              API Call
    //                                              --------
    public ApiResultProvider assistApiResultProvider() {
        return apiResultProvider; // not required for compatibility
    }

    // -----------------------------------------------------
    //                                       Action Response
    //                                       ---------------
    public ActionResponseHandler assistActionResponseHandler() {
        return actionResponseHandler; // not required for option
    }
}
