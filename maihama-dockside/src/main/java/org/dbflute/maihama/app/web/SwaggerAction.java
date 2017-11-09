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
package org.dbflute.maihama.app.web;

import org.dbflute.maihama.app.web.base.DocksideBaseAction;
import org.dbflute.saflute.web.action.response.JsonResponse;
import org.lastaflute.doc.SwaggerGenerator;
import org.lastaflute.doc.agent.SwaggerAgent;
import org.lastaflute.doc.web.LaActionSwaggerable;
import org.seasar.struts.annotation.Execute;

/**
 * @author jflute
 */
public class SwaggerAction extends DocksideBaseAction implements LaActionSwaggerable {

    @Execute(validator = false)
    public String index() {
        verifySwaggerAllowed();
        return new SwaggerAgent(requestManager).prepareSwaggerUiResponse("/swagger/json");
    }

    @Execute(validator = false)
    public JsonResponse json() {
        verifySwaggerAllowed();
        return new JsonResponse(new SwaggerGenerator().generateSwaggerMap());
    }

    private void verifySwaggerAllowed() { // also check in ActionAdjustmentProvider
        verifyOrClientError("Swagger is not enabled.", maihamaConfig.isSwaggerEnabled());
    }
}
