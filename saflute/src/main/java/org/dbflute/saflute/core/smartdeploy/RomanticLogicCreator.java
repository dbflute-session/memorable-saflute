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
package org.dbflute.saflute.core.smartdeploy;

import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.creator.LogicCreator;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author jflute
 */
public class RomanticLogicCreator extends LogicCreator {

    public RomanticLogicCreator(NamingConvention namingConvention) {
        super(namingConvention);
    }

    @SuppressWarnings("rawtypes")
    public ComponentDef createComponentDef(Class componentClass) {
        final ComponentDef dispatched = dispatchByEnv(componentClass);
        if (dispatched != null) {
            return dispatched;
        }
        return super.createComponentDef(componentClass);
    }

    protected ComponentDef dispatchByEnv(Class<?> componentClass) {
        if (!ComponentEnvDispatcher.canDispatch(componentClass)) { // check before for performance
            return null;
        }
        final ComponentEnvDispatcher envDispatcher = createEnvDispatcher();
        return envDispatcher.dispatch(componentClass);
    }

    protected ComponentEnvDispatcher createEnvDispatcher() {
        return new ComponentEnvDispatcher(getNamingConvention(), getInstanceDef(), getAutoBindingDef(),
                isExternalBinding(), getCustomizer());
    }
}
