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

import java.util.Locale;
import java.util.TimeZone;

/**
 * @author jflute
 */
public interface I18nable {

    /**
     * Get the user locale.
     * @return The locale for the user. (NullAllowed)
     */
    Locale getUserLocale();

    /**
     * Set the user locale.
     * @param userLocale The locale for the user. (NullAllowed)
     */
    void setUserLocale(Locale userLocale);

    /**
     * Get the user time-zone.
     * @return The time-zone for the user. (NullAllowed)
     */
    TimeZone getUserTimeZone();

    /**
     * Set the user time-zone.
     * @param userTimeZone The time-zone for the user. (NullAllowed)
     */
    void setUserTimeZone(TimeZone userTimeZone);
}
