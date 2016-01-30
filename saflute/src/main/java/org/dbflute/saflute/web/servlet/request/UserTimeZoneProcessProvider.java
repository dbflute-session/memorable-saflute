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
package org.dbflute.saflute.web.servlet.request;

import java.util.TimeZone;

import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;

/**
 * The provider of user time-zone process for current request.
 * @author jflute
 */
public interface UserTimeZoneProcessProvider {

    /**
     * Does it use time-zone handling? <br>
     * The time-zone handling is option (not related to Struts) so you need to choose.
     * @return The determination, true or false.
     */
    boolean isUseTimeZoneHandling();

    /**
     * Does it accept cookie time-zone? (prevails over session)
     * @return The determination, true or false.
     */
    boolean isAcceptCookieTimeZone();

    /**
     * Find business time-zone. (prevails over cookie)
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @param requestManager The manager of request to find your time-zone. (NotNull)
     * @return The found time-zone by your business rule. (NullAllowed: if null, not found)
     */
    TimeZone findBusinessTimeZone(ActionExecuteMeta executeMeta, RequestManager requestManager);

    /**
     * Get the requested time-zone. (for when not found in session or cookie) <br>
     * Unfortunately we cannot get time-zone from request, provider needs to provide the default time-zone
     * @param requestManager The manager of request to find your time-zone. (NotNull)
     * @return The time-zone as default by your business rule. (NotNull)
     */
    TimeZone getRequestedTimeZone(RequestManager requestManager);

    /**
     * Get the fall-back time-zone. (for no time-zone everywhere)
     * @return The fixed instance of time-zone, might be cached. (NullAllowed: if null, server time-zone)
     */
    TimeZone getFallbackTimeZone();
}
