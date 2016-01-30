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
package org.dbflute.saflute.core.util;

import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

/**
 * @author jflute
 */
public class ContainerUtil {

    public static <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return (COMPONENT) SingletonS2Container.getComponent(type);
    }

    @SuppressWarnings("unchecked")
    public static <COMPONENT> COMPONENT[] findAllComponents(Class<COMPONENT> type) {
        return (COMPONENT[]) SingletonS2ContainerFactory.getContainer().findAllComponents(type);
    }
}
