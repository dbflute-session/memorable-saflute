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
package ${packageName}.unit.mock;

import ${packageName}.projectfw.core.direction.${ProjectName}FwAssistantDirector;

/**
 * @author saflute_template
 */
public class Mock${ProjectName}FwAssistantDirector extends ${ProjectName}FwAssistantDirector {

    @Override
    protected String getDomainConfigFile() {
        return ${PROJECTNAME}_CONFIG_FILE;
    }

    @Override
    protected String[] getExtendsConfigFiles() {
        return new String[] { ${PROJECTNAME}_ENV_FILE };
    }

    @Override
    protected String getDomainMessageName() {
        return ${PROJECTNAME}_MESSAGE_NAME;
    }

    @Override
    protected String[] getExtendsMessageNames() {
        return new String[] {};
    }
}
