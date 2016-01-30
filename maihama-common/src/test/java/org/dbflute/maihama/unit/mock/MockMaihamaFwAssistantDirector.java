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
package org.dbflute.maihama.unit.mock;

import org.dbflute.maihama.projectfw.core.direction.MaihamaFwAssistantDirector;

/**
 * @author jflute
 */
public class MockMaihamaFwAssistantDirector extends MaihamaFwAssistantDirector {

    @Override
    protected String getDomainConfigFile() {
        return MAIHAMA_CONFIG_FILE;
    }

    @Override
    protected String[] getExtendsConfigFiles() {
        return new String[] { MAIHAMA_ENV_FILE };
    }

    @Override
    protected String getDomainMessageName() {
        return MAIHAMA_MESSAGE_NAME;
    }

    @Override
    protected String[] getExtendsMessageNames() {
        return new String[] {};
    }
}
