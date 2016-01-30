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

import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.web.servlet.taglib.MyErrorsTag;
import org.dbflute.saflute.web.servlet.taglib.MyHtmlMessagesTag;

/**
 * The manager of session. (session facade)
 * @author jflute
 */
public interface SessionManager {

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    /**
     * Get the attribute value of session by the value's type.
     * @param <ATTRIBUTE> The type of attribute object.
     * @param type The type of attribute saved in session. (NotNull)
     * @return The attribute object for the type. (NullAllowed: when not found)
     */
    <ATTRIBUTE> ATTRIBUTE getAttribute(Class<ATTRIBUTE> type);

    /**
     * Get the attribute value of session by the key.
     * @param <ATTRIBUTE> The type of attribute object.
     * @param key The string key of attribute saved in session. (NotNull)
     * @return The attribute object for the key. (NullAllowed: when not found)
     */
    <ATTRIBUTE> ATTRIBUTE getAttribute(String key);

    /**
     * Set the attribute value to session by the value's type. <br>
     * You should not set string object to suppress mistake. <br>
     * However you should not use this when the object might be extended. <br>
     * (Then the key is changed to sub-class type so you might have mistakes...)
     * @param value The attribute value added to session. (NotNull)
     */
    void setAttribute(Object value);

    /**
     * Set the attribute value to session by your original key.
     * @param key The key of the attribute. (NotNull)
     * @param value The attribute value added to session. (NotNull)
     */
    void setAttribute(String key, Object value);

    /**
     * Remove the attribute value by the value's type.
     * @param type The type of removed object. (NotNull)
     */
    void remove(Class<?> type);

    /**
     * Remove the attribute value by the key.
     * @param key The string key of attribute saved in session. (NotNull)
     */
    void remove(String key);

    /**
     * Invalidate session.
     */
    void invalidate();

    /**
     * Regenerate session ID for security. <br>
     * call invalidate() but it inherits existing session attributes.
     */
    void regenerateSessionId();

    // ===================================================================================
    //                                                                    Message Handling
    //                                                                    ================
    // -----------------------------------------------------
    //                                                Errors
    //                                                ------
    /**
     * Save message as (global) action errors. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyErrorsTag}.
     * @param messageKey The message key to be saved. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    void saveErrors(String messageKey, Object... args);

    /**
     * Save message as (global) action errors. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyErrorsTag}.
     * @param errors The action message for errors. (NotNull, EmptyAllowed: removes existing errors)
     */
    void saveErrors(ActionMessages errors);

    /**
     * Add message as (global) action errors to rear of existing messages. <br>
     * This message will be deleted immediately after display if you use {@link MyErrorsTag}.
     * @param messageKey The message key to be added. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    void addErrors(String messageKey, Object... args);

    /**
     * Does it have messages as (global or specified property) action errors at least one?
     * @return The determination, true or false.
     */
    boolean hasErrors();

    /**
     * Get action message from (global) action errors.
     * @return The object for action message. (NullAllowed: if no errors)
     */
    ActionMessages getErrors();

    /**
     * Clear (global) action errors from session.
     */
    void clearErrors();

    // -----------------------------------------------------
    //                                              Messages
    //                                              --------
    /**
     * Save message as (global) action messages. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyHtmlMessagesTag}.
     * @param messageKey The message key to be saved. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    void saveMessages(String messageKey, Object... args);

    /**
     * Save message as (global) action messages. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyHtmlMessagesTag}.
     * @param messages The action message for messages. (NotNull, EmptyAllowed: removes existing messages)
     */
    void saveMessages(ActionMessages messages);

    /**
     * Add message as (global) action messages to rear of existing messages. <br>
     * This message will be deleted immediately after display if you use {@link MyHtmlMessagesTag}.
     * @param messageKey The message key to be added. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    void addMessages(String messageKey, Object... args);

    /**
     * Does it have messages as (global or specified property) action messages at least one?
     * @return The determination, true or false.
     */
    boolean hasMessages();

    /**
     * Get action message from (global) action messages.
     * @return The object for action message. (NullAllowed: if no messages)
     */
    ActionMessages getMessages();

    /**
     * Clear (global) action messages from session.
     */
    void clearMessages();
}
