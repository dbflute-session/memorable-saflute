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
package org.dbflute.saflute.core.exception;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.exception.AccessContextNotFoundException;
import org.dbflute.exception.EntityAlreadyExistsException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.saflute.db.dbcp.ConnectionPoolViewBuilder;
import org.dbflute.saflute.db.dbflute.exception.NonTransactionalUpdateException;

/**
 * @author jflute
 */
public class ExceptionTranslator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(ExceptionTranslator.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The provider of exception translation. (NullAllowed: not required) */
    protected ExceptionTranslationProvider exceptionTranslationProvider;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public synchronized void initialize() {
        final OptionalCoreDirection direction = getOptionalCoreDirection();
        exceptionTranslationProvider = direction.assistExceptionTranslationProvider();
        showBootLogging();
    }

    protected OptionalCoreDirection getOptionalCoreDirection() {
        return assistantDirector.assistOptionalCoreDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Exception Translator]");
            LOG.info(" exceptionTranslationProvider: " + exceptionTranslationProvider);
        }
    }

    // ===================================================================================
    //                                                                           Translate
    //                                                                           =========
    public void translateException(RuntimeException cause) {
        if (exceptionTranslationProvider != null) { // not required
            exceptionTranslationProvider.translateFirst(cause);
        }
        if (cause instanceof AccessContextNotFoundException) {
            throwNonTransactionalUpdateException(cause);
        }
        if (cause instanceof SQLFailureException) {
            warnSQLFailureState((SQLFailureException) cause);
        }
        if (exceptionTranslationProvider != null) { // not required
            exceptionTranslationProvider.translateLast(cause);
        }
    }

    // -----------------------------------------------------
    //                                       Non Transaction
    //                                       ---------------
    protected void throwNonTransactionalUpdateException(RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The update process without transaction was found.");
        br.addItem("Advice");
        br.addElement("Update (contains insert, delete) should be executed in transaction.");
        br.addElement("Check your settings and implementations of the process.");
        final String msg = br.buildExceptionMessage();
        throw new NonTransactionalUpdateException(msg, cause);
    }

    // ===================================================================================
    //                                                                   SQL Failure State
    //                                                                   =================
    protected void warnSQLFailureState(SQLFailureException cause) {
        if (cause instanceof EntityAlreadyExistsException) {
            return; // the exception is obviously application exception so no warning
        }
        try {
            final String msg = buildSQLFailureState(cause);
            LOG.warn(msg); // only warning here, the cause will be caught by logging filter
        } catch (RuntimeException continued) {
            LOG.info("Failed to show warning of SQL failure state: " + Integer.toHexString(cause.hashCode()), continued);
        }
    }

    protected String buildSQLFailureState(SQLFailureException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("SQL Failure State, here!");
        br.addItem("Advice");
        br.addElement("This state is for the SQL failure exception: #" + Integer.toHexString(cause.hashCode()));
        prepareConnectionPoolView(br);
        return br.buildExceptionMessage();
    }

    protected void prepareConnectionPoolView(final ExceptionMessageBuilder br) {
        br.addItem("ConnectionPool View");
        br.addElement(createConnectionPoolViewBuilder().buildView());
    }

    protected ConnectionPoolViewBuilder createConnectionPoolViewBuilder() {
        return new ConnectionPoolViewBuilder();
    }
}
