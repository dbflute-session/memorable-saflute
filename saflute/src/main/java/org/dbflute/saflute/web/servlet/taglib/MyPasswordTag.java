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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.DynamicAttributes;

import org.apache.struts.taglib.html.PasswordTag;
import org.dbflute.saflute.web.servlet.taglib.base.DynamicTagAttribute;
import org.dbflute.saflute.web.servlet.taglib.base.TaglibLogic;

/**
 * The extension of Struts html password tag. <br>
 * The original attributes are added e.g. placeholder.
 * @author jflute
 */
public class MyPasswordTag extends PasswordTag implements DynamicAttributes {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String placeholder;
    protected String autocomplete;
    protected final Set<DynamicTagAttribute> dynamicAttributes = new LinkedHashSet<DynamicTagAttribute>();

    // ===================================================================================
    //                                                                             Prepare 
    //                                                                             =======
    @Override
    protected void prepareOtherAttributes(StringBuffer handlers) {
        // you can add original attributes here
        prepareAttribute(handlers, "placeholder", resolvePlaceholderResource(getPlaceholder()));
        prepareAttribute(handlers, "autocomplete", resolveAutocompleteResource(getAutocomplete()));

        handlers.append(buildDynamicAttributeExp());
    }

    protected String resolvePlaceholderResource(String placeholder) {
        return createTablibLogic().resolvePlaceholderResource(pageContext, placeholder, getProperty());
    }

    protected String resolveAutocompleteResource(String label) {
        return createTablibLogic().resolveAutocompleteResource(pageContext, label, getProperty());
    }

    // ===================================================================================
    //                                                                   Dynamic Attribute
    //                                                                   =================
    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        createTablibLogic().addDynamicAttribute(dynamicAttributes, localName, value);
    }

    protected String buildDynamicAttributeExp() {
        return createTablibLogic().buildDynamicAttributeExp(dynamicAttributes);
    }

    // ===================================================================================
    //                                                                        Taglib Logic
    //                                                                        ============
    protected TaglibLogic createTablibLogic() {
        return new TaglibLogic();
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        placeholder = null;
        autocomplete = null;
        dynamicAttributes.clear();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }
}
