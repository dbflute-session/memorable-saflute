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
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;

/**
 * The manager of request. (request facade)
 * @author jflute
 */
public interface RequestManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // -----------------------------------------------------
    //                                               Routing
    //                                               -------
    /** The attribute key for origin request path (without query) before (latest) routing. */
    String KEY_ROUTING_ORIGIN_REQUEST_PATH = "saflute.routing.origin.request.path";

    /** The attribute key for origin request path and query before (latest) routing. */
    String KEY_ROUTING_ORIGIN_REQUEST_PATH_AND_QUERY = "saflute.routing.origin.request.path.and.query";

    // -----------------------------------------------------
    //                                           User Locale
    //                                           -----------
    /** The key of user locale to cache it in request attribute. */
    String KEY_REQUEST_USER_LOCALE = "saflute.request.user.locale";

    /** The key of user locale to cache it in cookie attribute. */
    String KEY_COOKIE_USER_LOCALE = "SFLCL";

    /** The key of user locale to cache it in session attribute. (same as Struts) */
    String KEY_SESSION_USER_LOCALE = Globals.LOCALE_KEY;

    // -----------------------------------------------------
    //                                         User TimeZone
    //                                         -------------
    /** The key of user time-zone to cache it in request attribute. */
    String KEY_REQUEST_USER_TIMEZONE = "saflute.request.user.timezone";

    /** The key of user time-zone to cache it in cookie attribute. */
    String KEY_COOKIE_USER_TIMEZONE = "SFTZN";

    /** The key of user time-zone to cache it in session attribute. */
    String KEY_SESSION_USER_TIMEZONE = "saflute.cookie.user.timezone";

    // -----------------------------------------------------
    //                                     Various Attribute
    //                                     -----------------
    /** The key of SQL count by DBFlute. */
    String KEY_DBFLUTE_SQL_COUNT = "saflute.dbflute.sql.count";

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    /**
     * Get the current request.
     * @return The request object of HTTP servlet. (basically NotNull: if null, not Web application)
     */
    HttpServletRequest getRequest();

    // ===================================================================================
    //                                                                  Parameter Handling
    //                                                                  ==================
    /**
     * Get the request parameter by the key.
     * @param key The key of the parameter. (NotNull)
     * @return The value of the parameter as string. (NullAllowed: if null, means not found)
     */
    String getParameter(String key);

    // ===================================================================================
    //                                                                  Attribute Handling
    //                                                                  ==================
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * Get the attribute value of request by the value's type.
     * @param <ATTRIBUTE> The type of attribute object.
     * @param type The type of attribute saved in request. (NotNull)
     * @return The attribute object for the type. (NullAllowed: when not found)
     */
    <ATTRIBUTE> ATTRIBUTE getAttribute(Class<ATTRIBUTE> type);

    /**
     * Get the attribute value of request by the key.
     * @param <ATTRIBUTE> The type of attribute object.
     * @param key The string key of attribute saved in request. (NotNull)
     * @return The attribute object for the key. (NullAllowed: when not found)
     */
    <ATTRIBUTE> ATTRIBUTE getAttribute(String key);

    /**
     * Set the attribute value to request by the value's type. <br>
     * You should not set string object to suppress mistake. <br>
     * However you should not use this when the object might be extended. <br>
     * (Then the key is changed to sub-class type so you might have mistakes...)
     * @param value The attribute value added to request. (NotNull)
     */
    void setAttribute(Object value);

    /**
     * Set the attribute value to request by your original key.
     * @param key The key of the attribute. (NotNull)
     * @param value The attribute value added to request. (NotNull)
     */
    void setAttribute(String key, Object value);

    /**
     * Remove the attribute value by the value's type.
     * @param type The type of removed object. (NotNull)
     */
    void remove(Class<?> type);

    /**
     * Remove the attribute value by the key.
     * @param key The string key of attribute saved in request. (NotNull)
     */
    void remove(String key);

    // ===================================================================================
    //                                                                       Path Handling
    //                                                                       =============
    /**
     * Get the request path (without query) of the current request. (might be .do URL) <br>
     * e.g. /member/list.do (after routing), /member/list/ (before routing) <br>
     * Not contains context path and escaped slash remains.
     * @return The path as string. (NotNull: however, depends on Servlet container's implementation)
     */
    String getRequestPath();

    /**
     * Get the request path and query. (might be .do URL)
     * <pre>
     * e.g.
     *  /member/list.do?keyword=foo&status=FOO (after routing)
     *  /member/list/?keyword=foo&status=FOO (before routing)
     * </pre>
     * This uses {@link #getRequestPath()} and {@link HttpServletRequest#getQueryString()}.
     * @return The path and query as string. (NotNull)
     */
    String getRequestPathAndQuery();

    /**
     * Get the query string of the current request. e.g. keyword=foo&status=FOO <br>
     * This uses {@link HttpServletRequest#getQueryString()}.
     * @return The query string. (NullAllowed: when no query)
     */
    String getQueryString();

    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    /**
     * Get the origin request path (without query) before (latest) routing. <br>
     * e.g. /member/list/
     * @return The request path from context, which starts with '/'. (NotNull: plain path before routing)
     */
    String getRoutingOriginRequestPath();

    /**
     * Get the origin request path and query before (latest) routing. <br>
     * e.g. /member/list/?keyword=foo
     * @return The request path from context, which starts with '/'. (NotNull: plain path before routing)
     */
    String getRoutingOriginRequestPathAndQuery();

    // ===================================================================================
    //                                                                     Header Handling
    //                                                                     ===============
    /**
     * Get header value. (case insensitive)
     * @param headerKey The key of the header. (NotNull)
     * @return The value of specified header as string. (NullAllowed)
     */
    String getHeader(String headerKey);

    /**
     * Get 'Host' from header.
     * @return The 'Host' as string. (NullAllowed)
     */
    String getHost();

    /**
     * Get 'Referer' from header.
     * @return The 'Referer' as string. (NullAllowed)
     */
    String getReferer();

    /**
     * Get 'User-Agent' from header.
     * @return The 'User-Agent' as string. (NullAllowed)
     */
    String getUserAgent();

    // ===================================================================================
    //                                                                     Region Handling
    //                                                                     ===============
    // -----------------------------------------------------
    //                                           User Locale
    //                                           -----------
    /**
     * Get the locale for user of current request. <br>
     * Finding from e.g. cache, session, request.
     * @return The object that specifies user locale. (NotNull)
     */
    Locale getUserLocale();

    /**
     * Resolve the locale for user of current request. <br>
     * Basically this is called before action execution in request processor. <br>
     * So use {@link #getUserLocale()} if you find locale.
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The selected locale for the current request. (NotNull)
     */
    Locale resolveUserLocale(ActionExecuteMeta executeMeta);

    /**
     * Save the locale for user of current request to cookie. <br>
     * It is precondition that cookie locale can be accepted by option.
     * @param locale The saved locale to cookie. (NullAllowed: if null, remove it from cookie)
     * @throws IllegalStateException When the cookie locale cannot be accepted.
     */
    void saveUserLocaleToCookie(Locale locale);

    /**
     * Save the locale for user of current request to session. <br>
     * The session key is same as Struts so messages process can use it.
     * @param locale The saved locale to session. (NullAllowed: if null, remove it from session)
     */
    void saveUserLocaleToSession(Locale locale);

    // -----------------------------------------------------
    //                                         User TimeZone
    //                                         -------------
    /**
     * Get the time-zone for user of current request. <br>
     * Finding from e.g. cache, session, (assisted default time-zone).
     * @return The object that specifies user time-zone. (NotNull)
     */
    TimeZone getUserTimeZone();

    /**
     * Resolve the time-zone for user of current request. <br>
     * Basically this is called before action execution in request processor. <br>
     * So use {@link #getUserTimeZone()} if you find time-zone.
     * @param executeMeta The meta of action execution which you can get the calling method. (NotNull)
     * @return The object that specifies request time-zone. (NotNull)
     */
    TimeZone resolveUserTimeZone(ActionExecuteMeta executeMeta);

    /**
     * Save the time-zone for user of current request to cookie. <br>
     * It is precondition that cookie time-zone can be accepted by option.
     * @param timeZone The saved time-zone to cookie. (NullAllowed: if null, remove it from cookie)
     * @throws IllegalStateException When the cookie time-zone cannot be accepted.
     */
    void saveUserTimeZoneToCookie(TimeZone timeZone);

    /**
     * Save the time-zone for user of current request to session.
     * @param timeZone The saved time-zone to time-zone. (NullAllowed: if null, remove it from session)
     */
    void saveUserTimeZoneToSession(TimeZone timeZone);

    // ===================================================================================
    //                                                                    Message Handling
    //                                                                    ================
    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    /**
     * Save message as (global) action errors. (after deleting existing messages)
     * @param messageKey The message key to be saved. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    void saveErrors(String messageKey, Object... args);

    /**
     * Save message as (global) action errors. (after deleting existing messages)
     * @param errors The action message for errors. (NotNull)
     */
    void saveErrors(ActionMessages errors);

    /**
     * Add message as (global) action errors to rear of existing messages.
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
}
