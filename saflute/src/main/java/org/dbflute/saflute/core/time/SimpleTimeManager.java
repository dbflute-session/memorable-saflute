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
package org.dbflute.saflute.core.time;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.helper.HandyDate;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.OptionalCoreDirection;
import org.dbflute.saflute.core.direction.exception.FwRequiredAssistNotFoundException;
import org.dbflute.saflute.core.magic.TransactionTimeContext;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class SimpleTimeManager implements TimeManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimpleTimeManager.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The handler of business time. (NotNull: after initialization) */
    protected BusinessTimeHandler businessTimeHandler;

    /** The provider of time resource for development. (NotNull: only when development) */
    protected TimeResourceProvider developmentProvider;

    /** Does it ignore transaction time when the time manager returns current date? (not used if development) */
    protected boolean currentIgnoreTransaction;

    /** if {@link adjustAbsoluteMode} is true, absolute milliseconds, else relative milliseconds. (not used if development) */
    protected long adjustTimeMillis;

    /** Is it absolute time mode when using {@link adjustTimeMillis}? (not used if development) */
    protected boolean adjustAbsoluteMode;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalCoreDirection direction = assistOptionalCoreDirection();
        final TimeResourceProvider provider = direction.assistTimeResourceProvider();
        businessTimeHandler = provider.provideBusinessTimeHandler(this);
        if (businessTimeHandler == null) {
            String msg = "The provider returned null business-time handler: " + provider;
            throw new FwRequiredAssistNotFoundException(msg);
        }
        if (direction.isDevelopmentHere()) {
            developmentProvider = provider;
        } else {
            currentIgnoreTransaction = provider.isCurrentIgnoreTransaction();
            adjustTimeMillis = provider.provideAdjustTimeMillis();
            adjustAbsoluteMode = provider.isAdjustAbsoluteMode();
        }
        showBootLogging();
    }

    protected OptionalCoreDirection assistOptionalCoreDirection() {
        return assistantDirector.assistOptionalCoreDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Time Manager]");
            LOG.info(" businessTimeHandler: " + DfTypeUtil.toClassTitle(businessTimeHandler));
            if (developmentProvider != null) { // in development
                LOG.info(" developmentProvider: " + DfTypeUtil.toClassTitle(developmentProvider));
            } else {
                LOG.info(" currentIgnoreTransaction: " + currentIgnoreTransaction);
                LOG.info(" adjustTimeMillis: " + adjustTimeMillis);
                LOG.info(" adjustAbsoluteMode: " + adjustAbsoluteMode);
            }
        }
    }

    // ===================================================================================
    //                                                                             Current
    //                                                                             =======
    // don't use business-time handler in current-time process
    // the handler uses these processes...
    /**
     * {@inheritDoc}
     */
    public LocalDate getCurrentLocalDate() {
        return DfTypeUtil.toLocalDate(getCurrentDate(), getBusinessTimeZone());
    }

    /**
     * {@inheritDoc}
     */
    public LocalDateTime getCurrentLocalDateTime() {
        return DfTypeUtil.toLocalDateTime(getCurrentDate(), getBusinessTimeZone());
    }

    /**
     * {@inheritDoc}
     */
    public Date getCurrentDate() {
        if (TransactionTimeContext.exists()) {
            final Date transactionTime = TransactionTimeContext.getTransactionTime();
            return new Date(transactionTime.getTime());
        }
        return getFlashDate();
    }

    /**
     * {@inheritDoc}
     */
    public HandyDate getCurrentHandyDate() {
        return new HandyDate(getCurrentDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentMillis() {
        return getCurrentDate().getTime();
    }

    /**
     * {@inheritDoc}
     */
    public Timestamp getCurrentTimestamp() {
        if (TransactionTimeContext.exists()) {
            final Date transactionTime = TransactionTimeContext.getTransactionTime();
            return new Timestamp(transactionTime.getTime());
        }
        return new Timestamp(currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    public Date getFlashDate() {
        return new Date(currentTimeMillis());
    }

    // -----------------------------------------------------
    //                                         Adjusted Time
    //                                         -------------
    protected long currentTimeMillis() {
        if (developmentProvider != null) {
            final boolean dynamicAbsolute = developmentProvider.isAdjustAbsoluteMode();
            final long dynamicAdjust = developmentProvider.provideAdjustTimeMillis();
            return doCurrentTimeMillis(dynamicAbsolute, dynamicAdjust);
        } else {
            return doCurrentTimeMillis(adjustAbsoluteMode, adjustTimeMillis);
        }
    }

    protected long doCurrentTimeMillis(boolean absolute, long adjust) {
        if (absolute) {
            return adjust;
        } else {
            return System.currentTimeMillis() + adjust;
        }
    }

    // ===================================================================================
    //                                                                       Business Date
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public boolean isBusinessDate(Date targetDate) {
        assertBusinessTimeHandler();
        return businessTimeHandler.isBusinessDate(targetDate);
    }

    /**
     * {@inheritDoc}
     */
    public Date getNextBusinessDate(Date baseDate, int addedDay) {
        assertBusinessTimeHandler();
        return businessTimeHandler.getNextBusinessDate(baseDate, addedDay);
    }

    // ===================================================================================
    //                                                                   Business TimeZone
    //                                                                   =================
    /**
     * {@inheritDoc}
     */
    public TimeZone getBusinessTimeZone() {
        return businessTimeHandler.getBusinessTimeZone();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertBusinessTimeHandler() {
        if (businessTimeHandler == null) {
            String msg = "Not found the business-time handler in time manager.";
            throw new IllegalStateException(msg);
        }
    }
}
