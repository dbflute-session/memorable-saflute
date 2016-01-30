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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.dbflute.maihama.app.logic.OneWayCryptoLogic;
import org.dbflute.maihama.projectfw.web.login.MaihamaUserBaseBean;
import org.dbflute.saflute.web.action.login.LoginHandlingResource;
import org.dbflute.saflute.web.action.login.TypicalLoginBaseLogic;

/**
 * @param <USER_BEAN> The type of user bean.
 * @param <USER_ENTITY> The type of user entity or model.
 * @author jflute
 */
public abstract class MaihamaLoginBaseLogic<USER_BEAN extends MaihamaUserBaseBean, USER_ENTITY> extends
        TypicalLoginBaseLogic<USER_BEAN, USER_ENTITY> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected OneWayCryptoLogic oneWayCryptoLogic;

    // ===================================================================================
    //                                                                            Password
    //                                                                            ========
    @Override
    protected String encryptPassword(String plainPassword) {
        return oneWayCryptoLogic.encrypt(plainPassword); // not use primary cipher for one-way
    }

    // ===================================================================================
    //                                                                Action Determination
    //                                                                ====================
    /**
     * {@inheritDoc}
     */
    public boolean isLoginRequiredAction(LoginHandlingResource resource) {
        final Class<?> actionClass = resource.getActionClass();
        final Method actionMethod = resource.getActionMethod();
        return hasAnnotation(actionClass, actionMethod, getLoginRequiredAnnotationType());
    }

    protected abstract Class<? extends Annotation> getLoginRequiredAnnotationType();

    /**
     * {@inheritDoc}
     */
    public boolean isPerformLoginAction(LoginHandlingResource resource) {
        return hasAnnotationOnMethod(resource.getActionMethod(), getPerformLoginAnnotationType());
    }

    protected abstract Class<? extends Annotation> getPerformLoginAnnotationType();

    // ===================================================================================
    //                                                                 Annotation Handling
    //                                                                 ===================
    protected boolean hasAnnotation(Class<?> targetClass, Method targetMethod, Class<? extends Annotation> annoType) {
        if (hasAnnotationOnClass(targetClass, annoType)) {
            return true;
        }
        if (hasAnnotationOnMethod(targetMethod, annoType)) {
            return true;
        }
        return false;
    }

    protected boolean hasAnnotationOnClass(Class<?> targetClass, Class<? extends Annotation> annoType) {
        return targetClass.getAnnotation(annoType) != null;
    }

    protected boolean hasAnnotationOnMethod(Method targetMethod, Class<? extends Annotation> annoType) {
        return targetMethod.getAnnotation(annoType) != null;
    }
}
