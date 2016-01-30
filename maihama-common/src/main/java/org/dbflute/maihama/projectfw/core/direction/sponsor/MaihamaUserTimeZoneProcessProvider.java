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
package org.dbflute.maihama.projectfw.core.direction.sponsor;

import java.util.TimeZone;

import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.request.UserTimeZoneProcessProvider;

/**
 * @author jflute
 */
public class MaihamaUserTimeZoneProcessProvider implements UserTimeZoneProcessProvider {

    public static final TimeZone centralTimeZone = TimeZone.getDefault(); // you can change it if you like

    /**
     * {@inheritDoc}
     */
    public boolean isUseTimeZoneHandling() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAcceptCookieTimeZone() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public TimeZone findBusinessTimeZone(ActionExecuteMeta executeMeta, RequestManager requestManager) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public TimeZone getRequestedTimeZone(RequestManager requestManager) {
        return centralTimeZone; // same as fall-back
    }

    /**
     * {@inheritDoc}
     */
    public TimeZone getFallbackTimeZone() {
        return centralTimeZone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{useTimeZoneHandling=").append(isUseTimeZoneHandling());
        sb.append(", acceptCookieTimeZone=").append(isAcceptCookieTimeZone());
        sb.append(", fallbackTimeZone=").append(getFallbackTimeZone());
        sb.append("}");
        return sb.toString();
    }
}
