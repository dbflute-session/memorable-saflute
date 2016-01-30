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
package org.dbflute.saflute.web.action.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.web.action.processor.ActionExecuteConfig;
import org.dbflute.saflute.web.action.response.ActionResponse;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.util.ModifierUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.struts.annotation.Execute;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.config.S2ValidationConfig;
import org.seasar.struts.customizer.ActionCustomizer;
import org.seasar.struts.exception.DuplicateExecuteMethodAndPropertyRuntimeException;
import org.seasar.struts.exception.ExecuteMethodNotFoundRuntimeException;
import org.seasar.struts.exception.IllegalExecuteMethodRuntimeException;
import org.seasar.struts.exception.IllegalValidateMethodRuntimeException;
import org.seasar.struts.exception.IllegalValidatorOfExecuteMethodRuntimeException;
import org.seasar.struts.exception.MultipleAllSelectedUrlPatternRuntimeException;
import org.seasar.struts.exception.UnmatchValidatorAndValidateRuntimeException;

/**
 * You can get romantic action.
 * @author jflute
 */
public class RomanticActionCustomizer extends ActionCustomizer {

    // ===================================================================================
    //                                                                       Set up Method
    //                                                                       =============
    @Override
    protected void setupMethod(S2ActionMapping actionMapping, Class<?> actionClass) {
        // copied from super and adjust a little bit
        S2ExecuteConfig allSelectedExecuteConfig = null;
        for (Class<?> clazz = actionClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (isOutOfTargetMethod(actionMapping, actionClass, method)) {
                    continue;
                }
                checkActionMethod(actionClass, method);
                checkDuplicateExecute(actionMapping, actionClass, method);
                final S2ExecuteConfig executeConfig = createExecuteConfig();
                setupExecuteConfig(executeConfig, actionMapping, actionClass, method);
                if (executeConfig.isUrlPatternAllSelected()) {
                    if (allSelectedExecuteConfig != null) {
                        throw new MultipleAllSelectedUrlPatternRuntimeException(
                                allSelectedExecuteConfig.getUrlPattern(), executeConfig.getUrlPattern());
                    }
                    allSelectedExecuteConfig = executeConfig;
                } else {
                    actionMapping.addExecuteConfig(executeConfig);
                }
            }
        }
        registerAllSelectedExecuteConfig(actionMapping, allSelectedExecuteConfig);
        checkExecuteConfigSize(actionMapping, actionClass);
    }

    protected boolean isOutOfTargetMethod(S2ActionMapping actionMapping, Class<?> actionClass, Method method) {
        if (!ModifierUtil.isPublic(method)) {
            return true;
        }
        final Execute execute = method.getAnnotation(Execute.class);
        if (execute == null) {
            return true;
        }
        if (actionMapping.getExecuteConfig(method.getName()) != null) {
            return true;
        }
        return false;
    }

    @Override
    protected S2ExecuteConfig createExecuteConfig() {
        return new ActionExecuteConfig();
    }

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    protected void checkActionMethod(Class<?> actionClass, Method method) {
        if (method.getParameterTypes().length > 0 || !isAllowedActionReturn(method)) {
            throw new IllegalExecuteMethodRuntimeException(actionClass, method.getName());
        }
    }

    protected boolean isAllowedActionReturn(Method method) {
        final Class<?> returnType = method.getReturnType();
        return returnType.equals(String.class) || ActionResponse.class.isAssignableFrom(returnType);
    }

    protected void checkDuplicateExecute(S2ActionMapping actionMapping, Class<?> actionClass, Method m) {
        if (actionMapping.getActionFormBeanDesc().hasPropertyDesc(m.getName())) {
            throw new DuplicateExecuteMethodAndPropertyRuntimeException(actionClass, m.getName());
        }
    }

    // ===================================================================================
    //                                                                       ExecuteConfig
    //                                                                       =============
    protected void setupExecuteConfig(S2ExecuteConfig executeConfig, S2ActionMapping actionMapping,
            Class<?> actionClass, Method method) {
        final Execute execute = method.getAnnotation(Execute.class);
        final String input = !StringUtil.isEmpty(execute.input()) ? execute.input() : null;
        executeConfig.setMethod(method);
        executeConfig.setSaveErrors(execute.saveErrors());
        executeConfig.setInput(input);
        doSetupValidationConfig(executeConfig, actionMapping, actionClass, method, execute, input);
        executeConfig.setUrlPattern(execute.urlPattern());
        doSetupRole(executeConfig, execute);
        executeConfig.setStopOnValidationError(execute.stopOnValidationError());
        executeConfig.setRemoveActionForm(execute.removeActionForm());
        doSetupResetMethod(executeConfig, actionMapping, execute);
        executeConfig.setRedirect(execute.redirect());
    }

    // -----------------------------------------------------
    //                                            Validation
    //                                            ----------
    protected void doSetupValidationConfig(S2ExecuteConfig executeConfig, S2ActionMapping actionMapping,
            Class<?> actionClass, Method method, Execute execute, String input) {
        final List<S2ValidationConfig> validationConfigs = new ArrayList<S2ValidationConfig>();
        final String validate = execute.validate();
        boolean validator = false;
        if (!StringUtil.isEmpty(validate)) {
            final BeanDesc actionBeanDesc = actionMapping.getActionBeanDesc();
            final BeanDesc actionFormBeanDesc = actionMapping.getActionFormBeanDesc();
            for (String name : StringUtil.split(validate, ", ")) {
                if (VALIDATOR.equals(name)) {
                    if (!execute.validator()) {
                        throw new UnmatchValidatorAndValidateRuntimeException(actionClass, method.getName());
                    }
                    validationConfigs.add(createValidationConfig());
                    validator = true;
                } else if (actionFormBeanDesc.hasMethod(name)) {
                    final Method validateMethod = actionFormBeanDesc.getMethod(name);
                    checkValidateMethod(actionClass, validateMethod);
                    validationConfigs.add(createValidationConfig(validateMethod));
                } else {
                    final Method validateMethod = actionBeanDesc.getMethod(name);
                    checkValidateMethod(actionClass, validateMethod);
                    validationConfigs.add(createValidationConfig(validateMethod));
                }
            }
        }
        if (!validator && execute.validator()) {
            validationConfigs.add(0, createValidationConfig());
        }
        if (!validationConfigs.isEmpty() && input == null) {
            throw new IllegalValidatorOfExecuteMethodRuntimeException(actionClass, method.getName());
        }
        executeConfig.setValidationConfigs(validationConfigs);
    }

    protected void checkValidateMethod(Class<?> actionClass, Method validateMethod) {
        if (validateMethod.getParameterTypes().length > 0
                || !ActionMessages.class.isAssignableFrom(validateMethod.getReturnType())) {
            throw new IllegalValidateMethodRuntimeException(actionClass, validateMethod.getName());
        }
    }

    protected S2ValidationConfig createValidationConfig() {
        return new S2ValidationConfig();
    }

    protected S2ValidationConfig createValidationConfig(Method validateMethod) {
        return new S2ValidationConfig(validateMethod);
    }

    // -----------------------------------------------------
    //                                                  Role
    //                                                  ----
    protected void doSetupRole(S2ExecuteConfig executeConfig, Execute execute) {
        final String roles = execute.roles().trim();
        if (!StringUtil.isEmpty(roles)) {
            executeConfig.setRoles(StringUtil.split(roles, ", "));
        }
    }

    // -----------------------------------------------------
    //                                          Reset Method
    //                                          ------------
    protected void doSetupResetMethod(S2ExecuteConfig executeConfig, S2ActionMapping actionMapping, Execute execute) {
        final String reset = execute.reset();
        if (!StringUtil.isEmpty(reset)) {
            Method resetMethod = null;
            if ("reset".equals(reset)) {
                resetMethod = actionMapping.getActionFormBeanDesc().getMethodNoException(reset);
            } else {
                resetMethod = actionMapping.getActionFormBeanDesc().getMethod(reset);
            }
            if (resetMethod != null) {
                executeConfig.setResetMethod(resetMethod);
            }
        }
    }

    // ===================================================================================
    //                                                                        Registration
    //                                                                        ============
    protected void registerAllSelectedExecuteConfig(S2ActionMapping actionMapping,
            S2ExecuteConfig allSelectedExecuteConfig) {
        if (allSelectedExecuteConfig != null) {
            actionMapping.addExecuteConfig(allSelectedExecuteConfig);
        }
    }

    protected void checkExecuteConfigSize(S2ActionMapping actionMapping, Class<?> actionClass) {
        if (actionMapping.getExecuteConfigSize() == 0) {
            throw new ExecuteMethodNotFoundRuntimeException(actionClass);
        }
    }
}
