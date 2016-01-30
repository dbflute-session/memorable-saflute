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
package org.dbflute.maihama.unit;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.struts.Globals;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;
import org.apache.struts.validator.ValidatorPlugIn;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.saflute.core.magic.ThreadCacheContext;
import org.dbflute.saflute.core.magic.TransactionTimeContext;
import org.dbflute.saflute.core.message.MessageResourceHolder;
import org.dbflute.saflute.core.time.TimeManager;
import org.dbflute.saflute.web.action.message.PropertiesMessageResourcesFactory;
import org.dbflute.saflute.web.action.message.StrutsMessageResourceGateway;
import org.dbflute.utflute.seasar.ContainerTestCase;
import org.seasar.struts.config.S2ModuleConfigFactory;
import org.seasar.struts.validator.S2ValidatorResources;

/**
 * Use like this:
 * <pre>
 * YourTest extends {@link UnitDocksideContainerTestCase} {
 * 
 *     public void test_yourMethod() {
 *         <span style="color: #3F7E5E">// ## Arrange ##</span>
 *         YourAction action = new YourAction();
 *         <span style="color: #FD4747">inject</span>(action);
 * 
 *         <span style="color: #3F7E5E">// ## Act ##</span>
 *         action.submit();
 * 
 *         <span style="color: #3F7E5E">// ## Assert ##</span>
 *         assertTrue(action...);
 *     }
 * }
 * </pre>
 * @author jflute
 */
public abstract class UnitDocksideContainerTestCase extends ContainerTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected FwAssistantDirector assistantDirector;

    @Resource
    protected TimeManager timeManager;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    public void setUp() throws Exception {
        super.setUp();
        initializeSAStruts();
        initializeThreadCacheContext();
        initializeTransactionTime();
        initializeAssistantDirector();
    }

    protected void initializeSAStruts() {
        final ModuleConfig config = new S2ModuleConfigFactory().createModuleConfig("/");
        final ServletContext context = getMockRequest().getServletContext();
        context.setAttribute(Globals.MODULE_KEY, config);

        final MessageResources messages = new PropertiesMessageResourcesFactory().createResources("dummy");
        context.setAttribute(Globals.MESSAGES_KEY, messages);
        final MessageResourceHolder holder = getComponent(MessageResourceHolder.class);
        if (holder.getGateway() == null) {
            holder.acceptGateway(new StrutsMessageResourceGateway(messages));
        }

        context.setAttribute(ValidatorPlugIn.VALIDATOR_KEY, new S2ValidatorResources());
    }

    protected void initializeThreadCacheContext() {
        ThreadCacheContext.initialize();
    }

    protected void initializeTransactionTime() {
        // because of non-UserTransaction transaction in UTFlute
        final Date transactionTime = timeManager.getFlashDate();
        TransactionTimeContext.setTransactionTime(transactionTime);
    }

    protected void initializeAssistantDirector() {
        OptionalCoreDirection direction = assistantDirector.assistOptionalCoreDirection();
        direction.assistBootProcessCallback().callback(assistantDirector);
    }

    @Override
    public void tearDown() throws Exception {
        TransactionTimeContext.clear();
        ThreadCacheContext.clear();
        super.tearDown();
    }
}
