/*
 * Copyright 2014-2017 the original author or authors.
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
package org.lastaflute.doc.generator;

import java.io.File;
import java.util.List;

import org.dbflute.optional.OptionalThing;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.engine.GsonJsonEngine;
import org.lastaflute.core.json.engine.RealJsonEngine;
import org.lastaflute.doc.reflector.SourceParserReflector;

/**
 * @author p1us2er0
 * @since 0.6.9 (2075/03/05 Sunday)
 */
public class DocumentGeneratorFactory {

    public ActionDocumentGenerator createActionDocumentGenerator(List<String> srcDirList, int depth,
            OptionalThing<SourceParserReflector> sourceParserReflector) {
        return new ActionDocumentGenerator(srcDirList, depth, sourceParserReflector);
    }

    public String getLastaDocDir() {
        if (new File("./pom.xml").exists()) {
            return "./target/lastadoc/";
        }
        return "./build/lastadoc/";
    }

    public RealJsonEngine createJsonEngine() {
        return new GsonJsonEngine(builder -> {
            builder.serializeNulls().setPrettyPrinting();
        }, op -> {});
        // not to depend on application settings
        //return ContainerUtil.getComponent(JsonManager.class);
    }

    public OptionalThing<JsonMappingOption> getApplicationJsonMappingOption() {
        return OptionalThing.empty();
    }
}
