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
package org.dbflute.saflute.web.action.validator;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts.action.ActionMessages;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;

/**
 * @param <MESSAGES> The type of action messages.
 * @author jflute
 */
public abstract class TypicalBaseValidator<MESSAGES extends ActionMessages> {

    // ===================================================================================
    //                                                                      Basic Required
    //                                                                      ==============
    /**
     * @param messages The action messages to save errors. (NotNull)
     * @param value The validated value. (NotNull: if null, validation error)
     * @param property The property name of the value on HTML. (NotNull)
     * @param itemName The item name or label key of validation target. (NotNull)
     * @return Is it validation OK? (true: no error)
     */
    protected boolean validateRequired(MESSAGES messages, String value, String property, String itemName) {
        if (StringUtils.isBlank(value)) {
            setupErrorsRequired(messages, property, itemName);
            return false;
        }
        return true;
    }

    /**
     * @param messages The action messages to save errors. (NotNull)
     * @param value The validated value. (NotNull: if null, validation error)
     * @param property The property name of the value on HTML. (NotNull)
     * @param itemName The item name or label key of validation target. (NotNull)
     * @return Is it validation OK? (true: no error)
     */
    protected boolean validateRequired(MESSAGES messages, Number value, String property, String itemName) {
        if (value == null) {
            setupErrorsRequired(messages, property, itemName);
            return false;
        }
        return true;
    }

    /**
     * @param messages The action messages to save errors. (NotNull)
     * @param valueList The list of validated values. (NotNull, NotEmpty: if null or empty, validation error)
     * @param property The property name of the value on HTML. (NotNull)
     * @param itemName The item name or label key of validation target. (NotNull)
     * @return Is it validation OK? (true: no error)
     */
    protected boolean validateRequired(MESSAGES messages, List<? extends Object> valueList, String property, String itemName) {
        if (valueList == null || valueList.isEmpty()) {
            setupErrorsRequired(messages, property, itemName);
            return false;
        }
        return true;
    }

    /**
     * @param messages The action messages to save errors. (NotNull)
     * @param exists Does it exist? (if false, validation error)
     * @param property The property name of the value on HTML. (NotNull)
     * @param itemName The item name or label key of validation target. (NotNull)
     * @return Is it validation OK? (true: no error)
     */
    protected boolean validateRequired(MESSAGES messages, boolean exists, String property, String itemName) {
        if (!exists) {
            setupErrorsRequired(messages, property, itemName);
            return false;
        }
        return true;
    }

    protected abstract void setupErrorsRequired(MESSAGES messages, String property, String arg0);

    // ===================================================================================
    //                                                                   String Validation
    //                                                                   =================
    protected boolean validateMinLengthIfExists(MESSAGES messages, String inputStr, int minLength, String property, String itemName) {
        if (StringUtils.isNotEmpty(inputStr)) {
            if (inputStr.length() < minLength) {
                setupErrorsMinLength(messages, property, itemName, String.valueOf(minLength));
                return false;
            }
        }
        return true;
    }

    protected boolean validateMaxLengthIfExists(MESSAGES messages, String inputStr, int maxLength, String property, String itemName) {
        if (StringUtils.isNotEmpty(inputStr)) {
            if (inputStr.length() > maxLength) {
                setupErrorsOverMaxLength(messages, property, itemName, String.valueOf(maxLength));
                return false;
            }
        }
        return true;
    }

    // setupErrorsUnderMinLength() correctly (not fixed for compatible)
    protected abstract void setupErrorsMinLength(MESSAGES messages, String property, String itemName, String minLength);

    protected abstract void setupErrorsOverMaxLength(MESSAGES messages, String property, String itemName, String maxLength);

    // ===================================================================================
    //                                                                   Number Validation
    //                                                                   =================
    protected boolean validateIsNumberIfExists(MESSAGES messages, String numStr, String property, String itemName) {
        if (StringUtils.isBlank(numStr)) {
            return true;
        }
        final String filteredStr = filterNumberStr(numStr);
        if (StringUtils.isNotEmpty(filteredStr) && !NumberUtils.isNumber(filteredStr)) {
            setupErrorsNumber(messages, property, itemName);
            return false;
        }
        return true;
    }

    protected boolean validateIsDigitsIfExists(MESSAGES messages, String numStr, String property, String itemName) {
        if (StringUtils.isBlank(numStr)) {
            return true;
        }
        final String filteredStr = filterNumberStr(numStr);
        if (StringUtils.isNotEmpty(filteredStr) && !NumberUtils.isDigits(filteredStr)) {
            setupErrorsInteger(messages, property, itemName);
            return false;
        }
        return true;
    }

    protected boolean validateRequiredIsNumber(MESSAGES messages, String numStr, String property, String itemName) {
        if (!validateRequired(messages, numStr, property, itemName)) {
            return false;
        }
        if (!validateIsNumberIfExists(messages, numStr, property, itemName)) {
            return false;
        }
        return true;
    }

    protected boolean validateRequiredIsDigits(MESSAGES messages, String numStr, String property, String itemName) {
        if (!validateRequired(messages, numStr, property, itemName)) {
            return false;
        }
        if (!validateIsDigitsIfExists(messages, numStr, property, itemName)) {
            return false;
        }
        return true;
    }

    protected abstract void setupErrorsNumber(MESSAGES messages, String property, String arg0);

    protected abstract void setupErrorsInteger(MESSAGES messages, String property, String arg0);

    protected String filterNumberStr(String numStr) {
        return numStr; // you can override
    }

    // ===================================================================================
    //                                                           Classification Validation
    //                                                           =========================
    /**
     * @param messages The action messages to save errors. (NotNull)
     * @param value The validated value for classification. (NullAllowed)
     * @param cdefType The type of classification. (NotNull)
     * @param property The property name of the value on HTML. (NotNull)
     * @param itemName The item name or label key of validation target. (NotNull)
     * @return Is it validation OK? (true: no error)
     */
    protected boolean validateClassificationIfExists(MESSAGES messages, String value, Class<? extends Classification> cdefType,
            String property, String itemName) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        final ClassificationMeta meta = toClassificationMeta(cdefType);
        final Classification code = meta.codeOf(value);
        if (code == null) {
            setupErrorsInvalidClassification(messages, property, itemName);
            return false;
        }
        return true;
    }

    /**
     * @param messages The action messages to save errors. (NotNull)
     * @param valueList The list of validated value for classification. (NullAllowed, EmptyAllowed)
     * @param cdefType The type of classification. (NotNull)
     * @param property The property name of the value on HTML. (NotNull)
     * @param itemName The item name or label key of validation target. (NotNull)
     * @return Is it validation OK? (true: no error)
     */
    protected boolean validateClassificationListIfExists(MESSAGES messages, List<String> valueList,
            Class<? extends Classification> cdefType, String property, String itemName) {
        if (valueList == null || valueList.isEmpty()) {
            return true;
        }
        for (String value : valueList) {
            if (!validateClassificationIfExists(messages, value, cdefType, property, itemName)) {
                return false;
            }
        }
        return true;
    }

    protected abstract ClassificationMeta toClassificationMeta(Class<? extends Classification> cdefType);

    protected abstract void setupErrorsInvalidClassification(MESSAGES messages, String property, String itemName);
}
