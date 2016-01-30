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
import java.util.List;

import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.config.S2ValidationConfig;
import org.seasar.struts.enums.SaveType;

/**
 * @author jflute
 */
public class ExecuteConfigTeller {

    protected final S2ExecuteConfig executeConfig;

    public ExecuteConfigTeller(S2ExecuteConfig executeConfig) {
        this.executeConfig = executeConfig;
    }

    public Method getMethod() {
        return executeConfig.getMethod();
    }

    public boolean isValidator() {
        return executeConfig.isValidator();
    }

    public List<S2ValidationConfig> getValidationConfigs() {
        return executeConfig.getValidationConfigs();
    }

    public SaveType getSaveErrors() {
        return executeConfig.getSaveErrors();
    }

    public String getInput() {
        return executeConfig.getInput();
    }

    public String getUrlPattern() {
        return executeConfig.getUrlPattern();
    }

    public boolean isUrlPatternAllSelected() {
        return executeConfig.isUrlPatternAllSelected();
    }

    public String[] getRoles() {
        return executeConfig.getRoles();
    }

    public Method getResetMethod() {
        return executeConfig.getResetMethod();
    }

    public boolean isStopOnValidationError() {
        return executeConfig.isStopOnValidationError();
    }

    public boolean isRemoveActionForm() {
        return executeConfig.isRemoveActionForm();
    }

    public boolean isRedirect() {
        return executeConfig.isRedirect();
    }
}
