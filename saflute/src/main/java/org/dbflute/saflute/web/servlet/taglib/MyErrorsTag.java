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

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.ErrorsTag;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.servlet.session.SessionManager;

/**
 * The extension of Struts html errors tag.
 * @author jflute
 */
public class MyErrorsTag extends ErrorsTag {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The style class for the errors tag. (NullAllowed) */
    protected String styleClass;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MyErrorsTag() {
        super();
    }

    // ===================================================================================
    //                                                                        Tag Override
    //                                                                        ============
    @Override
    public int doStartTag() throws JspException { // copied from super's and customize it
        // Were any error messages specified?
        TagUtils tagUtils = TagUtils.getInstance();
        ActionMessages errors = extractActionErrors(tagUtils);
        if ((errors == null) || errors.isEmpty()) {
            return EVAL_BODY_INCLUDE;
        }

        // Render the error messages appropriately
        String tagExp = buildTagExp(errors, tagUtils);
        writeTag(tagUtils, tagExp);

        // clear temporary session message here
        // you can give message to next action when redirect safely
        //
        // time passed!
        // we then heard a voice coming out of nowhere...
        // errors on session are removed at next request at RequestProcessor#processCachedMessages()
        // so you don't need to remove them here
        //
        // time passed!
        // SessionManager may have external session so it needs to remove by session manager
        // also in other cases, it should not depend on Struts session handling
        clearSessionGlobalErrorsIfNeeds();

        return EVAL_BODY_INCLUDE;
    }

    protected ActionMessages extractActionErrors(TagUtils tagUtils) throws JspException {
        ActionMessages errors = null;
        try {
            errors = tagUtils.getActionMessages(pageContext, name);
        } catch (JspException e) {
            tagUtils.saveException(pageContext, e);
            throw e;
        }
        return errors;
    }

    protected String buildTagExp(ActionMessages errors, TagUtils tagUtils) throws JspException {
        boolean headerPresent = tagUtils.present(pageContext, bundle, locale, getHeader());
        boolean footerPresent = tagUtils.present(pageContext, bundle, locale, getFooter());
        boolean prefixPresent = tagUtils.present(pageContext, bundle, locale, getPrefix());
        boolean suffixPresent = tagUtils.present(pageContext, bundle, locale, getSuffix());
        final StringBuilder results = new StringBuilder();
        boolean headerDone = false;
        @SuppressWarnings("rawtypes")
        final Iterator reports = (property == null) ? errors.get() : errors.get(property);
        while (reports.hasNext()) {
            final ActionMessage report = (ActionMessage) reports.next();
            if (!headerDone) {
                setupHeader(tagUtils, headerPresent, results);
                headerDone = true;
            }
            final String mainMessage = prepareMainMessage(tagUtils, report);
            if (!isSuppressMessageLine(tagUtils, results, report, mainMessage)) {
                if (prefixPresent) {
                    setupPrefix(tagUtils, results, mainMessage);
                }
                setupMessage(tagUtils, results, report, mainMessage);
                if (suffixPresent) {
                    setupSuffix(tagUtils, results, mainMessage);
                }
            }
        }
        if (headerDone && footerPresent) {
            setupFooter(tagUtils, results);
        }
        return results.toString();
    }

    protected String prepareMainMessage(TagUtils tagUtils, ActionMessage report) throws JspException {
        final String message;
        if (report.isResource()) {
            message = tagUtils.message(pageContext, bundle, locale, report.getKey(), report.getValues());
        } else {
            message = report.getKey();
        }
        return message;
    }

    protected boolean isSuppressMessageLine(TagUtils tagUtils, StringBuilder results, ActionMessage report, String mainMessage) {
        return false; // you can add original rule by overriding
    }

    // -----------------------------------------------------
    //                                           Setup Parts
    //                                           -----------
    protected void setupHeader(TagUtils tagUtils, boolean headerPresent, StringBuilder results) throws JspException {
        if (styleClass != null) {
            results.append("<ul class=\"" + styleClass + "\">");
        } else if (headerPresent) {
            String header = tagUtils.message(pageContext, bundle, locale, getHeader());
            results.append(header);
        }
    }

    protected void setupPrefix(TagUtils tagUtils, StringBuilder results, String mainMessage) throws JspException {
        String prefix = tagUtils.message(pageContext, bundle, locale, getPrefix());
        results.append(prefix);
    }

    protected void setupMessage(TagUtils tagUtils, StringBuilder results, ActionMessage report, String mainMessage) throws JspException {
        if (mainMessage != null) {
            results.append(mainMessage);
        }
    }

    protected void setupSuffix(TagUtils tagUtils, StringBuilder results, String mainMessage) throws JspException {
        String suffix = tagUtils.message(pageContext, bundle, locale, getSuffix());
        results.append(suffix);
    }

    protected void setupFooter(TagUtils tagUtils, StringBuilder results) throws JspException {
        String footer = tagUtils.message(pageContext, bundle, locale, getFooter());
        results.append(footer);
    }

    // -----------------------------------------------------
    //                                                 Write
    //                                                 -----
    protected void writeTag(TagUtils tagUtils, String result) throws JspException {
        tagUtils.write(pageContext, result);
    }

    // ===================================================================================
    //                                                                             Release
    //                                                                             =======
    @Override
    public void release() {
        super.release();
        styleClass = null;
    }

    // ===================================================================================
    //                                                                      Global Message
    //                                                                      ==============
    protected void clearSessionGlobalErrorsIfNeeds() {
        final SessionManager sessionManager = getSessionManager();
        if (name != null && name.equals(Globals.ERROR_KEY)) { // only when global key
            sessionManager.clearErrors();
        }
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected SessionManager getSessionManager() {
        return getComponent(SessionManager.class);
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return ContainerUtil.getComponent(type);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Set the style class for the errors tag。
     * @param styleClass The string for style class. (NotNull)
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}
