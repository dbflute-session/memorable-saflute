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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.dbflute.bhv.BehaviorSelector;
import org.dbflute.saflute.core.direction.BootProcessCallback;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.saflute.core.message.MessageResourceHolder;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.message.StrutsMessageResourceGateway;
import org.seasar.framework.container.servlet.S2ContainerServlet;

/**
 * The servlet to manage DI container. <br>
 * This extends Seasar's servlet to cache exception from initialization. <br>
 * If no extension, you cannot search the exception in log files created by logger.
 * @author jflute
 */
public class ContainerManagementServlet extends S2ContainerServlet {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(ContainerManagementServlet.class);

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    @Override
    public void init() {
        try {
            super.init();
        } catch (Throwable e) {
            String msg = "Failed to initialize S2Container.";
            LOG.error(msg, e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException(msg, e);
        }
        try {
            adjustComponent();
        } catch (RuntimeException e) {
            LOG.error("Failed to adjust components.", e);
            throw e;
        }
        final FwAssistantDirector assistantDirector = ContainerUtil.getComponent(FwAssistantDirector.class);
        try {
            callbackProcess(assistantDirector);
        } catch (RuntimeException e) {
            LOG.error("Failed to callback process.", e);
            throw e;
        }
        try {
            prepareMeta(assistantDirector);
        } catch (RuntimeException e) {
            LOG.error("Failed to prepare meta.", e);
            throw e;
        }
        try {
            showBoot(assistantDirector);
        } catch (RuntimeException e) {
            LOG.error("Failed to show boot title.", e);
            throw e;
        }
    }

    // ===================================================================================
    //                                                                    Adjust Component
    //                                                                    ================
    protected void adjustComponent() {
        adjustMessageResources();
    }

    protected void adjustMessageResources() {
        final MessageResources messages = getMessageResources();
        if (messages != null) {
            final MessageResourceHolder holder = ContainerUtil.getComponent(MessageResourceHolder.class);
            holder.acceptGateway(new StrutsMessageResourceGateway(messages));
        }
    }

    protected MessageResources getMessageResources() {
        return (MessageResources) getServletContext().getAttribute(Globals.MESSAGES_KEY);
    }

    // ===================================================================================
    //                                                                    Callback Process
    //                                                                    ================
    protected void callbackProcess(FwAssistantDirector assistantDirector) {
        final OptionalCoreDirection coreDirection = assistantDirector.assistOptionalCoreDirection();
        final BootProcessCallback callback = coreDirection.assistBootProcessCallback();
        if (callback != null) {
            callback.callback(assistantDirector);
        }
    }

    // ===================================================================================
    //                                                                        Prepare Meta
    //                                                                        ============
    protected void prepareMeta(FwAssistantDirector assistantDirector) {
        final OptionalCoreDirection coreDirection = assistantDirector.assistOptionalCoreDirection();
        if (!coreDirection.isDevelopmentHere()) {
            final BehaviorSelector selector = ContainerUtil.getComponent(BehaviorSelector.class);
            selector.initializeConditionBeanMetaData(); // with logging in it
        }
    }

    // ===================================================================================
    //                                                                           Show Boot
    //                                                                           =========
    protected void showBoot(FwAssistantDirector assistantDirector) {
        if (LOG.isInfoEnabled()) {
            final OptionalCoreDirection coreDirection = assistantDirector.assistOptionalCoreDirection();
            final String domainTitle = coreDirection.assistDomainTitle();
            final String environmentTitle = coreDirection.assistEnvironmentTitle();
            LOG.info("_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
            LOG.info(" the system has been initialized:");
            LOG.info("");
            LOG.info("  -> " + domainTitle + " (" + environmentTitle + ")");
            LOG.info("_/_/_/_/_/_/_/_/_/_/");
        }
    }
}
