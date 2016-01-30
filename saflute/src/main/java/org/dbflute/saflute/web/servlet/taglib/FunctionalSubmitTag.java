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
package org.dbflute.saflute.web.servlet.taglib;

import org.dbflute.saflute.web.servlet.taglib.base.TaglibLogic;
import org.seasar.struts.taglib.S2SubmitTag;

/**
 * @author jflute
 */
public class FunctionalSubmitTag extends S2SubmitTag {

    private static final long serialVersionUID = 1L;

    @Override
    public void setValue(String value) {
        super.setValue(resolveSubmitValueResource(value));
    }

    protected String resolveSubmitValueResource(String submitValue) {
        return createTablibLogic().resolveSubmitValueResource(pageContext, submitValue, getProperty());
    }

    protected TaglibLogic createTablibLogic() {
        return new TaglibLogic();
    }
}
