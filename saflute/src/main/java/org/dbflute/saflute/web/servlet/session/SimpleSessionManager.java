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
package org.dbflute.saflute.web.servlet.session;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.seasar.struts.util.RequestUtil;

/**
 * The simple implementation of session manager. <br>
 * This class is basically defined at DI setting file.
 * @author jflute
 */
public class SimpleSessionManager implements SessionManager {

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public void initialize() {
        // empty for now
    }

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <ATTRIBUTE> ATTRIBUTE getAttribute(Class<ATTRIBUTE> type) {
        assertObjectNotNull("type", type);
        final HttpSession session = getSessionExisting();
        return session != null ? (ATTRIBUTE) session.getAttribute(type.getName()) : null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <ATTRIBUTE> ATTRIBUTE getAttribute(String key) {
        assertObjectNotNull("key", key);
        final HttpSession session = getSessionExisting();
        return session != null ? (ATTRIBUTE) session.getAttribute(key) : null;
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(Object value) {
        assertObjectNotNull("value", value);
        checkTypedAttributeSettingMistake(value);
        getSessionOrCreated().setAttribute(value.getClass().getName(), value);
    }

    protected void checkTypedAttributeSettingMistake(Object value) {
        if (value instanceof String) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The value for typed attribute was simple string type.");
            br.addItem("Advice");
            br.addElement("The value should not be string.");
            br.addElement("Do you forget value setting for the string key?");
            br.addElement("The typed attribute setting cannot accept string");
            br.addElement("to suppress setting mistake like this:");
            br.addElement("  (x):");
            br.addElement("    sessionManager.setAttribute(\"foo.bar\")");
            br.addElement("  (o):");
            br.addElement("    sessionManager.setAttribute(\"foo.bar\", value)");
            br.addElement("  (o):");
            br.addElement("    sessionManager.setAttribute(bean)");
            br.addItem("Specified Value");
            br.addElement(value != null ? value.getClass().getName() : null);
            br.addElement(value);
            final String msg = br.buildExceptionMessage();
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(String key, Object value) {
        assertObjectNotNull("key", key);
        assertObjectNotNull("value", value);
        getSessionOrCreated().setAttribute(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Class<?> type) {
        assertObjectNotNull("type", type);
        final HttpSession session = getSessionExisting();
        if (session != null) {
            session.removeAttribute(type.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String key) {
        assertObjectNotNull("key", key);
        final HttpSession session = getSessionExisting();
        if (session != null) {
            session.removeAttribute(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void invalidate() {
        HttpSession session = getSessionExisting();
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void regenerateSessionId() {
        final HttpSession session = getSessionExisting();
        if (session == null) {
            return;
        }
        final Map<String, Object> savedSessionMap = extractSavedSessionMap(session);
        invalidate(); // regenerate ID
        for (Entry<String, Object> entry : savedSessionMap.entrySet()) {
            setAttribute(entry.getKey(), entry.getValue()); // inherit existing attributes
        }
    }

    protected Map<String, Object> extractSavedSessionMap(HttpSession session) {
        final Enumeration<String> attributeNames = session.getAttributeNames();
        final Map<String, Object> savedSessionMap = new LinkedHashMap<String, Object>();
        while (attributeNames.hasMoreElements()) { // save existing attributes temporarily
            final String nextElement = attributeNames.nextElement();
            savedSessionMap.put(nextElement, getAttribute(nextElement));
        }
        return savedSessionMap;
    }

    // ===================================================================================
    //                                                                    Message Handling
    //                                                                    ================
    // -----------------------------------------------------
    //                                                Errors
    //                                                ------
    /**
     * {@inheritDoc}
     */
    public void saveErrors(String messageKey, Object... args) {
        assertObjectNotNull("messageKey", messageKey);
        doSaveErrors(prepareActionMessages(messageKey, args));
    }

    /**
     * {@inheritDoc}
     */
    public void saveErrors(ActionMessages errors) {
        assertObjectNotNull("errors", errors);
        doSaveErrors(errors);
    }

    protected void doSaveErrors(ActionMessages errors) {
        // you cannot use ActionMessagesUtil because it uses session directly
        // (you should use this.remove() and this.setAttribute() for external session) 
        if (errors == null || errors.isEmpty()) { // same specification as ActionMessagesUtil
            remove(Globals.ERROR_KEY);
            return;
        }
        setAttribute(Globals.ERROR_KEY, errors);
    }

    /**
     * {@inheritDoc}
     */
    public void addErrors(String messageKey, Object... args) {
        assertObjectNotNull("messageKey", messageKey);
        doAddErrors(prepareActionMessages(messageKey, args));
    }

    protected void doAddErrors(ActionMessages errors) {
        if (errors == null) {
            return;
        }
        ActionMessages existingErrors = (ActionMessages) getErrors();
        if (existingErrors == null) {
            existingErrors = new ActionMessages();
        }
        existingErrors.add(errors);
        doSaveErrors(existingErrors);
    }

    protected ActionMessages prepareActionMessages(String messageKey, Object[] args) {
        final ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(messageKey, args));
        return messages;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasErrors() {
        final ActionMessages errors = (ActionMessages) getSessionOrCreated().getAttribute(Globals.ERROR_KEY);
        return errors != null && !errors.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public ActionMessages getErrors() {
        return getAttribute(Globals.ERROR_KEY);
    }

    /**
     * {@inheritDoc}
     */
    public void clearErrors() {
        remove(Globals.ERROR_KEY);
    }

    // -----------------------------------------------------
    //                                              Messages
    //                                              --------
    /**
     * {@inheritDoc}
     */
    public void saveMessages(String messageKey, Object... args) {
        assertObjectNotNull("messageKey", messageKey);
        doSaveMessages(prepareActionMessages(messageKey, args));
    }

    /**
     * {@inheritDoc}
     */
    public void saveMessages(ActionMessages messages) {
        assertObjectNotNull("messages", messages);
        doSaveMessages(messages);
    }

    protected void doSaveMessages(ActionMessages messages) {
        // you cannot use ActionMessagesUtil because it uses session directly
        // (you should use this.remove() and this.setAttribute() for external session) 
        if (messages == null || messages.isEmpty()) { // same specification as ActionMessagesUtil
            remove(Globals.MESSAGE_KEY);
            return;
        }
        setAttribute(Globals.MESSAGE_KEY, messages);
    }

    /**
     * {@inheritDoc}
     */
    public void addMessages(String messageKey, Object... args) {
        assertObjectNotNull("messageKey", messageKey);
        doAddMessages(prepareActionMessages(messageKey, args));
    }

    protected void doAddMessages(ActionMessages messages) {
        if (messages == null) {
            return;
        }
        ActionMessages existingMessages = (ActionMessages) getMessages();
        if (existingMessages == null) {
            existingMessages = new ActionMessages();
        }
        existingMessages.add(messages);
        doSaveMessages(existingMessages);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasMessages() {
        final ActionMessages messages = (ActionMessages) getSessionOrCreated().getAttribute(Globals.MESSAGE_KEY);
        return messages != null && !messages.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public ActionMessages getMessages() {
        return getAttribute(Globals.MESSAGE_KEY);
    }

    /**
     * {@inheritDoc}
     */
    public void clearMessages() {
        remove(Globals.MESSAGE_KEY);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected HttpServletRequest getRequest() {
        return RequestUtil.getRequest();
    }

    protected HttpSession getSessionOrCreated() {
        return getRequest().getSession(true);
    }

    protected HttpSession getSessionExisting() {
        final HttpServletRequest request = getRequest(); // null allowed when e.g. asynchronous process
        return request != null ? request.getSession(false) : null;
    }

    protected void assertObjectNotNull(String variableName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
