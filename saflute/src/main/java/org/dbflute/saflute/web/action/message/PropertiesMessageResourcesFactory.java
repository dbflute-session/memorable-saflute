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
package org.dbflute.saflute.web.action.message;

import org.apache.struts.util.MessageResources;
import org.seasar.struts.util.S2PropertyMessageResourcesFactory;

/**
 * @author jflute
 */
public class PropertiesMessageResourcesFactory extends S2PropertyMessageResourcesFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public MessageResources createResources(String config) {
        return new ObjectiveMessageResources(this, config);
    }
}
