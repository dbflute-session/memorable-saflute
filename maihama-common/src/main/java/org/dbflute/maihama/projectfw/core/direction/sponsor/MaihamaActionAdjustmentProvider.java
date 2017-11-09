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
package org.dbflute.maihama.projectfw.core.direction.sponsor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dbflute.maihama.projectfw.core.direction.MaihamaConfig;
import org.dbflute.saflute.web.action.exception.ForcedRequest404NotFoundException;
import org.dbflute.saflute.web.action.processor.ActionAdjustmentProvider;
import org.dbflute.saflute.web.action.processor.ActionMappingWrapper;
import org.dbflute.util.DfTypeUtil;
import org.seasar.struts.config.S2ExecuteConfig;

/**
 * @author jflute
 */
public class MaihamaActionAdjustmentProvider implements ActionAdjustmentProvider {

    private static final int INDEXED_PROPERTY_SIZE_LIMIT = 200; // hard coding for now

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final MaihamaConfig config;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MaihamaActionAdjustmentProvider(MaihamaConfig config) {
        this.config = config;
    }

    public int provideIndexedPropertySizeLimit() {
        return INDEXED_PROPERTY_SIZE_LIMIT;
    }

    public String decodeUrlParameterPropertyValue(Object bean, String name, String value) {
        return null;
    }

    public String filterJspPath(String path, ActionMappingWrapper actionMappingWrapper) {
        return null;
    }

    public List<String> prepareJspRetryWordList(String requestPath, List<String> wordList) {
        return null;
    }

    public boolean isForcedRoutingTarget(HttpServletRequest request, String requestPath) {
        if (isForced404NotFoundRouting(request, requestPath)) {
            throw new ForcedRequest404NotFoundException("Forcedly 404 not found routing: " + requestPath);
        }
        return false;
    }

    public boolean isForcedSuppressRedirectWithSlash(HttpServletRequest request, String requestPath, S2ExecuteConfig executeConfig) {
        return false;
    }

    // ===================================================================================
    //                                                                             Routing
    //                                                                             =======
    public boolean isForced404NotFoundRouting(HttpServletRequest request, String requestPath) {
        if (isSwaggerIllegalAccess(isSwaggerEnabled(), requestPath)) { // e.g. swagger's html, css
            return true; // to suppress direct access to swagger resources at e.g. production
        }
        return false;
    }

    protected boolean isSwaggerEnabled() {
        return config.isSwaggerEnabled();
    }

    protected boolean isSwaggerIllegalAccess(boolean swaggerEnabled, String requestPath) { // used by e.g. isForced404NotFoundRouting()
        return !swaggerEnabled && isSwaggerRequest(requestPath); // e.g. swagger's html, css
    }

    public boolean isSwaggerRequest(String requestPath) {
        return requestPath.startsWith("/webjars/swagger-ui") || requestPath.startsWith("/swagger");
    }

    public String customizeActionMappingRequestPath(String requestPath) {
        return null;
    }

    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{indexedLimit=" + INDEXED_PROPERTY_SIZE_LIMIT + "}";
    }
}
