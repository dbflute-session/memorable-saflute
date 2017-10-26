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
package org.dbflute.saflute.core.remoteapi;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Consumer;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalThing;
import org.dbflute.remoteapi.FlutyRemoteApi;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.exception.RemoteApiRequestValidationErrorException;
import org.dbflute.remoteapi.exception.RemoteApiResponseValidationErrorException;
import org.dbflute.saflute.core.remoteapi.supplement.Valid;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.lastaflute.core.util.Lato;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.struts.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class SaflutyRemoteApi extends FlutyRemoteApi {

    private static final Logger logger = LoggerFactory.getLogger(SaflutyRemoteApi.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected RequestManager requestManager; // not null after set, for validation and various purpose

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SaflutyRemoteApi(Consumer<FlutyRemoteApiRule> defaultOpLambda, Object callerExp) {
        super(defaultOpLambda, callerExp);
    }

    public void acceptRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    @Override
    protected void validateParam(Type returnType, String urlBase, String actionPath, Object[] pathVariables, Object param,
            FlutyRemoteApiRule rule) {
        final StringBuilder pathSb = new StringBuilder();
        try {
            doValidate(param, pathSb);
        } catch (RemoteApiSimpleValidationErrorException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Validation Error as Param object for the remote API.");
            final String url = urlBase + actionPath + "/" + Arrays.asList(pathVariables);
            setupRequestInfo(br, returnType, url, param);
            setupYourRule(br, rule);
            final String msg = br.buildExceptionMessage();
            if (rule.getValidatorOption().isHandleAsWarnParam()) {
                logger.warn(msg, e);
            } else {
                throw new RemoteApiRequestValidationErrorException(msg, e);
            }
        }
    }

    @Override
    protected void validateReturn(Type returnType, String url, OptionalThing<Object> param, int httpStatus, OptionalThing<String> body,
            Object ret, FlutyRemoteApiRule rule) {
        final StringBuilder pathSb = new StringBuilder();
        try {
            doValidate(ret, pathSb);
        } catch (RemoteApiSimpleValidationErrorException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Validation Error as Return object for the remote API.");
            setupRequestInfo(br, returnType, url, param);
            setupResponseInfo(br, httpStatus, body);
            setupReturnInfo(br, ret);
            setupYourRule(br, rule);
            final String msg = br.buildExceptionMessage();
            if (rule.getValidatorOption().isHandleAsWarnReturn()) {
                logger.warn(msg, e);
            } else {
                throw new RemoteApiResponseValidationErrorException(msg, e);
            }
        }
    }

    protected void doValidate(Object bean, StringBuilder pathSb) {
        final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(bean.getClass());
        final int propertyDescSize = beanDesc.getPropertyDescSize();
        for (int i = 0; i < propertyDescSize; i++) {
            final PropertyDesc propertyDesc = beanDesc.getPropertyDesc(i);
            final Field field = propertyDesc.getField();
            if (field == null) {
                continue; // field property only supported
            }
            final Required requiredAnno = field.getAnnotation(Required.class);
            if (requiredAnno != null) {
                final Object value = propertyDesc.getValue(bean);
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    String msg = "The field is required but no value: value=[" + value + "], field=" + pathSb;
                    throw new RemoteApiSimpleValidationErrorException(msg);
                }
            }
            final Valid validAnno = field.getAnnotation(Valid.class);
            if (validAnno != null) {
                final Object nestedInstance = propertyDesc.getValue(bean);
                if (nestedInstance != null) {
                    doValidate(nestedInstance, pathSb);
                }
            }
        }
    }

    public static class RemoteApiSimpleValidationErrorException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RemoteApiSimpleValidationErrorException(String msg) {
            super(msg);
        }
    }

    // ===================================================================================
    //                                                                      RemoteApi Rule
    //                                                                      ==============
    @Override
    protected FlutyRemoteApiRule newRemoteApiRule() {
        return new SaflutyRemoteApiRule();
    }

    // ===================================================================================
    //                                                                      Error Handling
    //                                                                      ==============
    @Override
    protected String convertBeanToDebugString(Object bean) {
        return Lato.string(bean); // because its toString() may not be overridden
    }
}
