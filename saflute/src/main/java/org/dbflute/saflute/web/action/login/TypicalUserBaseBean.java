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
package org.dbflute.saflute.web.action.login;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dbflute.helper.HandyDate;

/**
 * @author jflute
 */
public abstract class TypicalUserBaseBean implements UserBean, SyncCheckable, I18nable, Serializable {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** the latest date of synchronized check. (NullAllowed: no check yet) */
    protected Date lastestSyncCheckDate;

    /** The locale for the user. (NullAllowed) */
    protected Locale userLocale;

    /** The time-zone for the user. (NullAllowed) */
    protected TimeZone userTimeZone;

    // ===================================================================================
    //                                                                               Login
    //                                                                               =====
    /** {@inheritDoc} */
    public boolean isLogin() {
        return getUserId() != null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final Date checkDate = getLastestSyncCheckDate();
        final String checkDisp = checkDate != null ? new HandyDate(checkDate).toDisp("yyyy/MM/dd HH:mm:ss") : null;
        final Locale locale = getUserLocale();
        final TimeZone timeZone = getUserTimeZone();
        final StringBuilder sb = new StringBuilder();
        sb.append("{userId=").append(getUserId());
        setupToStringAdditionalUserInfo(sb);
        if (checkDisp != null) {
            sb.append(", sync=").append(checkDisp);
        }
        if (locale != null) {
            sb.append(", locale=").append(locale);
        }
        if (timeZone != null) {
            sb.append(", timeZone=").append(timeZone);
        }
        sb.append("}");
        return sb.toString();
    }

    protected void setupToStringAdditionalUserInfo(StringBuilder sb) {
    }

    // ===================================================================================
    //                                                                           SyncCheck
    //                                                                           =========
    /** {@inheritDoc} */
    public Date getLastestSyncCheckDate() {
        return lastestSyncCheckDate;
    }

    /** {@inheritDoc} */
    public void setLastestSyncCheckDate(Date checkDate) {
        lastestSyncCheckDate = checkDate;
    }

    // ===================================================================================
    //                                                                       i18n Handling
    //                                                                       =============
    /** {@inheritDoc} */
    public Locale getUserLocale() {
        return userLocale;
    }

    /** {@inheritDoc} */
    public void setUserLocale(Locale userLocale) {
        this.userLocale = userLocale;
    }

    /** {@inheritDoc} */
    public TimeZone getUserTimeZone() {
        return userTimeZone;
    }

    /** {@inheritDoc} */
    public void setUserTimeZone(TimeZone userTimeZone) {
        this.userTimeZone = userTimeZone;
    }
}
