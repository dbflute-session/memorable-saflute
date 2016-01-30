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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.taglib.TagUtils;
import org.dbflute.saflute.web.servlet.taglib.base.TaglibLogic;

/**
 * The tag for label resource.
 * <pre>
 * e.g.
 *  html:label key="labels.foo"
 *  html:label key="labels.foo|labels.list"
 * </pre>
 * @author jflute
 */
public class MyLabelTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    protected String key;

    public int doStartTag() throws JspException {
        final String label = findLabelResourceChecked(key);
        TagUtils.getInstance().write(pageContext, label);
        return SKIP_BODY;
    }

    protected String findLabelResourceChecked(String key) {
        final String caller = "label-tag";
        return createTablibLogic().findLabelResourceChecked(pageContext, key, caller);
    }

    protected TaglibLogic createTablibLogic() {
        return new TaglibLogic();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
