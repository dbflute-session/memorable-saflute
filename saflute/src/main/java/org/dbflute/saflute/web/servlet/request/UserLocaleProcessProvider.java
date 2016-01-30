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

import java.util.Locale;

import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;

/**
 * The provider of user locale process for current request.
 * @author jflute
 */
public interface UserLocaleProcessProvider {

    /**
     * Does it accept cookie locale? (prevails over session)
     * @return The determination, true or false.
     */
    boolean isAcceptCookieLocale();

    /**
     * Find business locale. (prevails over cookie)
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @param requestManager The manager of request to find your locale. (NotNull)
     * @return The found locale by your business rule. (NullAllowed: if null, not found)
     */
    Locale findBusinessLocale(ActionExecuteMeta executeMeta, RequestManager requestManager);

    /**
     * Get the requested locale. (for when not found in session or cookie)
     * @param requestManager The manager of request to find your time-zone. (NotNull)
     * @return The locale as default by your business rule. (NullAllowed: if null, requested client locale)
     */
    Locale getRequestedLocale(RequestManager requestManager);

    /**
     * Get the fall-back locale. (for no locale everywhere)
     * @return The fixed instance of locale, might be cached. (NullAllowed: if null, server locale)
     */
    Locale getFallbackLocale();
}
