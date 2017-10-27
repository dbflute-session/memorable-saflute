/*
 * Copyright 2015-2017 the original author or authors.
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

import java.util.Arrays;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.dbflute.remoteapi.FlutyRemoteApi;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.FlutyRemoteBehavior;
import org.dbflute.saflute.core.direction.AccessibleConfig;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.Srl;
import org.seasar.framework.convention.NamingConvention;

/**
 * The base class of behavior for remote API.
 * @author awane
 * @author jflute
 * @author inoue
 */
public abstract class LastaRemoteBehavior extends FlutyRemoteBehavior {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @Resource
    private NamingConvention namingConvention;
    @Resource
    private AccessibleConfig config;

    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final RequestManager requestManager; // not null, injected via constructor of concrete class

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaRemoteBehavior(RequestManager requestManager) {
        this.requestManager = requestManager;
        ((LastaRemoteApi) remoteApi).acceptRequestManager(requestManager); // for constructor headache
    }

    @Override
    protected String getUserAgentServiceName() { // in callback so you can use DI components
        return Arrays.stream(namingConvention.getRootPackageNames()).filter(name -> name.contains(".app")).map(name -> {
            return DfStringUtil.substringLastRear(DfStringUtil.substringFirstFront(name, ".app"), ".");
        }).findFirst().orElse(null);
    }

    @Override
    protected String getUserAgentAppName() { // in callback so you can use DI components
        String title = config.get("domain.title"); // instead of domain name
        return title != null ? Srl.initUncap(title) : null;
    }

    @Override
    protected FlutyRemoteApi newRemoteApi(Consumer<FlutyRemoteApiRule> ruleSetupper, Object callerExp) {
        return new LastaRemoteApi(ruleSetupper, callerExp); // in constructor so you cannot use DI components
    }
}
