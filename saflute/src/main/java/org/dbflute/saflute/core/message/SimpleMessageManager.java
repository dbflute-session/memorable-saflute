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
package org.dbflute.saflute.core.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.message.exception.MessageKeyNotFoundException;

/**
 * @author jflute
 */
public class SimpleMessageManager implements MessageManager {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    protected MessageResourceHolder messageResourceHolder;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        // empty for now
    }

    // ===================================================================================
    //                                                                         Get Message
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public String getMessage(Locale locale, String key) {
        final MessageResourceGateway gateway = getMessageResourceGateway();
        final String message = gateway.getMessage(locale, key);
        if (message == null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the message by the key.");
            br.addItem("Key");
            br.addElement(key);
            br.addItem("Locale");
            br.addElement(locale);
            final String msg = br.buildExceptionMessage();
            throw new MessageKeyNotFoundException(msg);
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(Locale locale, String key, Object[] values) {
        final MessageResourceGateway gateway = getMessageResourceGateway();
        final String message = gateway.getMessage(locale, key, values);
        if (message == null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the message by the key.");
            br.addItem("Key");
            br.addElement(key);
            br.addItem("Values");
            br.addElement(values != null ? Arrays.asList(values) : null);
            br.addItem("Locale");
            br.addElement(locale);
            final String msg = br.buildExceptionMessage();
            throw new MessageKeyNotFoundException(msg);
        }
        return message;
    }

    // ===================================================================================
    //                                                                    Resolved Message
    //                                                                    ================
    /**
     * {@inheritDoc}
     */
    public List<String> getMessageList(Locale locale, ActionMessages errors) {
        if (errors == null) {
            String msg = "The argument 'errors' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        final List<String> messageList = new ArrayList<String>();
        if (errors.isEmpty()) {
            return messageList;
        }
        @SuppressWarnings("unchecked")
        final Iterator<ActionMessage> ite = (Iterator<ActionMessage>) errors.get();
        while (ite.hasNext()) {
            final ActionMessage actionMessage = (ActionMessage) ite.next();
            final String messageText = resolveMessageText(locale, actionMessage);
            messageList.add(messageText);
        }
        return messageList;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getPropertyMessageMap(Locale locale, ActionMessages errors) {
        final Map<String, List<String>> propertyMessageMap = new LinkedHashMap<String, List<String>>();
        if (errors.isEmpty()) {
            return propertyMessageMap;
        }
        @SuppressWarnings("unchecked")
        final Iterator<String> properyIte = (Iterator<String>) errors.properties();
        while (properyIte.hasNext()) {
            final String property = properyIte.next();
            List<String> messageList = propertyMessageMap.get(property);
            if (messageList == null) {
                messageList = new ArrayList<String>();
            }
            @SuppressWarnings("unchecked")
            final Iterator<ActionMessage> actionMessageIte = (Iterator<ActionMessage>) errors.get(property);
            while (actionMessageIte.hasNext()) {
                final ActionMessage actionMessage = actionMessageIte.next();
                final String messageText = resolveMessageText(locale, actionMessage);
                messageList.add(messageText);
            }
            propertyMessageMap.put(property, messageList);
        }
        return propertyMessageMap;
    }

    protected String resolveMessageText(Locale locale, ActionMessage actionMessage) {
        final String key = actionMessage.getKey();
        final Object[] values = actionMessage.getValues();
        final String messageText;
        if (actionMessage.isResource()) {
            messageText = getMessage(locale, key, values);
        } else {
            messageText = key;
        }
        return messageText;
    }

    // ===================================================================================
    //                                                                   Message Resources
    //                                                                   =================
    /**
     * {@inheritDoc}
     */
    public MessageResourceGateway getMessageResourceGateway() {
        final MessageResourceGateway gateway = messageResourceHolder.getGateway();
        if (gateway == null) {
            String msg = "Not found the gateway for message resource: holder=" + messageResourceHolder;
            throw new IllegalStateException(msg);
        }
        return gateway;
    }
}
