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

import org.apache.struts.Globals;
import org.apache.struts.taglib.html.MessagesTag;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.servlet.session.SessionManager;

/**
 * The extension of Struts html messages tag.
 * @author jflute
 */
public class MyHtmlMessagesTag extends MessagesTag { // 'Html' to differentiate 

    private static final long serialVersionUID = 1L;

    // it defines 'message' attribute is required in '.tld' file.
    // errors is available (not messages) when no 'message' so easy of mistake
    // however don't want to have big differences with Struts default
    // so the small change but getting reasonable safety by jflute (2014/10/26)

    // ===================================================================================
    //                                                                     End Tag Process
    //                                                                     ===============
    @Override
    public int doEndTag() throws JspException {
        clearSessionGlobalMessagesIfNeeds(); // *see comment on errors tag for the details
        return super.doEndTag();
    }

    // ===================================================================================
    //                                                                      Global Message
    //                                                                      ==============
    protected void clearSessionGlobalMessagesIfNeeds() {
        final SessionManager sessionManager = getSessionManager();
        if (name != null && (name.equals(Globals.ERROR_KEY) || name.equals(Globals.MESSAGE_KEY))) { // only when global key
            if (message != null && message.equalsIgnoreCase("true")) {
                sessionManager.clearMessages();
            } else {
                sessionManager.clearErrors();
            }
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
}
