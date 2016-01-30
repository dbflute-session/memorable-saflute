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
package org.dbflute.saflute.web.servlet;

import org.dbflute.saflute.core.direction.exception.FwRequiredAssistNotFoundException;
import org.dbflute.saflute.web.servlet.cookie.CookieResourceProvider;
import org.dbflute.saflute.web.servlet.request.ResponseHandlingProvider;
import org.dbflute.saflute.web.servlet.request.UserLocaleProcessProvider;
import org.dbflute.saflute.web.servlet.request.UserTimeZoneProcessProvider;

/**
 * @author jflute
 */
public class OptionalServletDirection {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected UserLocaleProcessProvider userLocaleProcessProvider;
    protected UserTimeZoneProcessProvider userTimeZoneProcessProvider;
    protected CookieResourceProvider cookieResourceProvider;
    protected ResponseHandlingProvider responseHandlingProvider;

    // ===================================================================================
    //                                                                     Direct Property
    //                                                                     ===============
    public void directRequest(UserLocaleProcessProvider userLocaleProcessProvider, UserTimeZoneProcessProvider userTimeZoneProcessProvider) {
        this.userLocaleProcessProvider = userLocaleProcessProvider;
        this.userTimeZoneProcessProvider = userTimeZoneProcessProvider;
    }

    public void directCookie(CookieResourceProvider cookieResourceProvider) {
        this.cookieResourceProvider = cookieResourceProvider;
    }

    public void directResponse(ResponseHandlingProvider responseHandlingProvider) {
        this.responseHandlingProvider = responseHandlingProvider;
    }

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    public UserLocaleProcessProvider assistUserLocaleProcessProvider() {
        if (userLocaleProcessProvider == null) {
            String msg = "Not found the provider for user locale process in request.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return userLocaleProcessProvider;
    }

    public UserTimeZoneProcessProvider assistUserTimeZoneProcessProvider() {
        if (userTimeZoneProcessProvider == null) {
            String msg = "Not found the provider for user time-zone process in request.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return userTimeZoneProcessProvider;
    }

    public CookieResourceProvider assistCookieResourceProvider() {
        if (cookieResourceProvider == null) {
            String msg = "Not found the provider for cookie resource.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return cookieResourceProvider;
    }

    public ResponseHandlingProvider assistResponseHandlingProvider() {
        return responseHandlingProvider; // not required for compatibility
    }
}
