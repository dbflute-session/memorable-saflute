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
package org.dbflute.maihama.app.base;

import javax.annotation.Resource;

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.maihama.dbflute.allcommon.CDef;
import org.dbflute.maihama.projectfw.web.action.MaihamaMessages;
import org.dbflute.saflute.core.time.TimeManager;
import org.dbflute.saflute.web.action.validator.TypicalBaseValidator;

/**
 * @param <MESSAGES> The type of action messages.
 * @author jflute
 */
public abstract class MaihamaBaseValidator<MESSAGES extends MaihamaMessages> extends TypicalBaseValidator<MESSAGES> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @Resource
    protected TimeManager timeManager;

    // ===================================================================================
    //                                                                      Basic Required
    //                                                                      ==============
    @Override
    protected void setupErrorsRequired(MESSAGES messages, String property, String arg0) {
        messages.addErrorsRequired(property, arg0);
    }

    // ===================================================================================
    //                                                                   Length Validation
    //                                                                   =================
    @Override
    protected void setupErrorsMinLength(MESSAGES messages, String property, String itemName, String minLength) {
        messages.addErrorsMinlength(property, itemName, minLength);
    };

    @Override
    protected void setupErrorsOverMaxLength(MESSAGES messages, String property, String itemName, String maxLength) {
        messages.addErrorsMaxlength(property, itemName, maxLength);
    };

    // ===================================================================================
    //                                                                   Number Validation
    //                                                                   =================
    @Override
    protected void setupErrorsNumber(MESSAGES messages, String property, String arg0) {
        messages.addErrorsNumber(property, arg0);
    }

    @Override
    protected void setupErrorsInteger(MESSAGES messages, String property, String arg0) {
        messages.addErrorsInteger(property, arg0);
    }

    // ===================================================================================
    //                                                           Classification Validation
    //                                                           =========================
    @Override
    protected ClassificationMeta toClassificationMeta(Class<? extends Classification> cdefType) {
        return CDef.DefMeta.valueOf(cdefType.getSimpleName());
    }

    @Override
    protected void setupErrorsInvalidClassification(MESSAGES messages, String property, String itemName) {
        messages.addErrorsInvalid(property, itemName);
    }
}
