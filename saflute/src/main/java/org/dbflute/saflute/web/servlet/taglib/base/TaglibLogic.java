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
package org.dbflute.saflute.web.servlet.taglib.base;

import java.io.CharArrayWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.struts.Globals;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.Constants;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.message.ObjectiveMessageResources;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.taglib.exception.TaglibFailureException;
import org.dbflute.saflute.web.servlet.taglib.exception.TaglibLabelsResourceNotFoundException;
import org.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public class TaglibLogic {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The key prefix for errors of message resources, which contains dot at last. */
    protected static final String ERRORSS_KEY_PREFIX = ObjectiveMessageResources.MESSAGES_KEY_PREFIX;

    /** The key prefix for labels of message resources, which contains dot at last. */
    protected static final String LABELS_KEY_PREFIX = ObjectiveMessageResources.LABELS_KEY_PREFIX;

    /** The key prefix for messages of message resources, which contains dot at last. */
    protected static final String MESSAGES_KEY_PREFIX = ObjectiveMessageResources.MESSAGES_KEY_PREFIX;

    // ===================================================================================
    //                                                                     Lookup Property
    //                                                                     ===============
    /**
     * @param pageContext The context of page. (NotNull)
     * @param beanName The name of bean. (NullAllowed: if null, uses default bean key)
     * @param property The name of property. (NotNull)
     * @return The value of the property in the bean. (NotNull, EmptyAllowed)
     * @throws JspException
     */
    public Object lookupPropertyValue(PageContext pageContext, String beanName, String property) throws JspException {
        return doLookupProperty(pageContext, beanName != null ? beanName : Constants.BEAN_KEY, property);
    }

    protected Object doLookupProperty(PageContext pageContext, String beanName, String property) throws JspException {
        return TagUtils.getInstance().lookup(pageContext, beanName, property, null);
    }

    // ===================================================================================
    //                                                                    Common Attribute
    //                                                                    ================
    /**
     * @param pageContext The context of page. (NotNull)
     * @param title The value of titles attribute. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of place-holder. (NullAllowed: when the place-holder is null)
     * @throws BrTaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolveTitleResource(PageContext pageContext, String title, String caller) {
        return resolveLabelResource(pageContext, title, caller);
    }

    // ===================================================================================
    //                                                                        Place Holder
    //                                                                        ============
    /**
     * @param pageContext The context of page. (NotNull)
     * @param placeholder The value of place-holder attribute. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of place-holder. (NullAllowed: when the place-holder is null)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolvePlaceholderResource(PageContext pageContext, String placeholder, String caller) {
        return resolveLabelResource(pageContext, placeholder, caller);
    }

    // ===================================================================================
    //                                                                       Auto Complete
    //                                                                       =============
    /**
     * @param pageContext The context of page. (NotNull)
     * @param value The value of autocomplete attribute, 'on' or 'off'. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of autocomplete. (NullAllowed: when the autocomplete is null)
     * @throws BrTaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolveAutocompleteResource(final PageContext pageContext, final String value, final String caller) {
        if (value != null && !value.equals("on") && !value.equals("off")) {
            throwAutocompleteInvalidValueException(value, caller);
        }
        return value;
    }

    protected void throwAutocompleteInvalidValueException(final String value, final String caller) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Invalid value for autocomplete attribute.");
        br.addItem("Requested JSP Path");
        br.addElement(getRequestJspPath());
        br.addItem("Invalid Value");
        br.addElement(value);
        br.addItem("Expected Value");
        br.addElement("'on' or 'off'");
        br.addItem("Target Taglib");
        br.addElement(caller);
        final String msg = br.buildExceptionMessage();
        throw new TaglibFailureException(msg);
    }

    // ===================================================================================
    //                                                                              Submit
    //                                                                              ======
    /**
     * @param pageContext The context of page. (NotNull)
     * @param submitValue The value of value attribute of submit. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of place-holder. (NullAllowed: when the place-holder is null)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolveSubmitValueResource(PageContext pageContext, String submitValue, String caller) {
        return resolveLabelResource(pageContext, submitValue, caller);
    }

    // ===================================================================================
    //                                                                       Index Caption
    //                                                                       =============
    /**
     * @param pageContext The context of page. (NotNull)
     * @param indexCaption The value of index caption. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of index caption. (NullAllowed)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String findIndexCaptionResource(PageContext pageContext, String indexCaption, String caller) {
        return findLabelResourceIfNeeds(pageContext, indexCaption, caller);
    }

    // ===================================================================================
    //                                                                      Label Resource
    //                                                                      ==============
    /**
     * Resolve the label resource by message resources.
     * @param pageContext The context of page. (NotNull)
     * @param label The label value, might be resource key. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of label. (NullAllowed: when the label is null)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String resolveLabelResource(PageContext pageContext, String label, String caller) {
        final String found = findLabelResourceIfNeeds(pageContext, label, caller);
        return found != null ? found : label;
    }

    /**
     * Find the label resource if the key is for label.
     * @param pageContext The context of page. (NotNull)
     * @param resourceKey The resource key of label. (NullAllowed: if null, returns null)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of label. (NullAllowed: when not found)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not found in the resource if the key is for label.
     */
    public String findLabelResourceIfNeeds(PageContext pageContext, String resourceKey, String caller) {
        if (resourceKey == null) {
            return null;
        }
        if (isLabelsResource(resourceKey)) {
            final List<String> keyList = DfStringUtil.splitListTrimmed(resourceKey, "|");
            final StringBuilder sb = new StringBuilder();
            for (String key : keyList) {
                final String resolved = findMessage(pageContext, key, null);
                if (resolved == null) {
                    throwLabelsResourceNotFoundException(key, caller);
                }
                sb.append(resolved);
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * Is the resource for label?
     * @param resouce The value of resource. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isLabelsResource(String resouce) {
        return resouce.startsWith(LABELS_KEY_PREFIX) || resouce.startsWith(MESSAGES_KEY_PREFIX);
    }

    /**
     * Find the label resource with checking the resource is for label or not.
     * @param pageContext The context of page. (NotNull)
     * @param resourceKey The resource key of label. (NotNull: if null, exception)
     * @param caller The caller expression for exception message. (NotNull)
     * @return The resolved value of label. (NotNull: exception when not found)
     * @throws TaglibLabelsResourceNotFoundException When the resource key is not for label or null.
     */
    public String findLabelResourceChecked(PageContext pageContext, String resourceKey, String caller) {
        final String resource = findLabelResourceIfNeeds(pageContext, resourceKey, caller);
        if (resource == null) {
            throwLabelsResourceNotFoundException(resourceKey, caller);
        }
        return resource;
    }

    protected String findMessage(PageContext pageContext, String key, Object[] args) {
        try {
            return TagUtils.getInstance().message(pageContext, Globals.MESSAGES_KEY, Globals.LOCALE_KEY, key, args);
        } catch (JspException e) {
            String msg = "Failed to find the message of resource by the key: " + key;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void throwLabelsResourceNotFoundException(String resourceKey, String caller) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the resource for label by the key.");
        br.addItem("Requested JSP Path");
        br.addElement(getRequestJspPath());
        br.addItem("Resource Key");
        br.addElement(resourceKey);
        br.addItem("Target Taglib");
        br.addElement(caller);
        final String msg = br.buildExceptionMessage();
        throw new TaglibLabelsResourceNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                  Requested JSP Path
    //                                                                  ==================
    public String getRequestJspPath() {
        return getRequestManager().getRequestPath();
    }

    // ===================================================================================
    //                                                                   Dynamic Attribute
    //                                                                   =================
    public void addDynamicAttribute(Set<DynamicTagAttribute> dynamicAttributes, String key, Object value) {
        if (key != null && key.trim().length() > 0) {
            dynamicAttributes.add(createDynamicTagAttribute(key, value));
        }
    }

    protected DynamicTagAttribute createDynamicTagAttribute(String key, Object value) {
        return new DynamicTagAttribute(key, value != null ? value.toString() : null);
    }

    public String buildDynamicAttributeExp(Set<DynamicTagAttribute> dynamicAttributes) {
        final StringBuilder sb = new StringBuilder();
        for (DynamicTagAttribute attribute : dynamicAttributes) {
            final Object val = attribute.getValue();
            if (val == null) {
                sb.append(" " + attribute.getKey());
            } else {
                sb.append(" " + attribute.getKey() + "=\"");
                sb.append(escapeInnerDoubleQuote(attribute.getValue().toString()) + "\"");
            }
        }
        return sb.toString();
    }

    protected String escapeInnerDoubleQuote(String str) {
        if (str == null || str.trim().length() == 0) {
            return str;
        }
        final String filtered;
        if (str.contains("\\") || str.contains("\"")) {
            final CharArrayWriter caw = new CharArrayWriter(128);
            final char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\\') {
                    caw.append(chars[i]);
                } else if (chars[i] == '"') {
                    caw.append('\\');
                }
                caw.append(chars[i]);
            }
            filtered = caw.toString();
        } else {
            filtered = str;
        }
        return filtered;
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected RequestManager getRequestManager() {
        return getComponent(RequestManager.class);
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return ContainerUtil.getComponent(type);
    }
}
