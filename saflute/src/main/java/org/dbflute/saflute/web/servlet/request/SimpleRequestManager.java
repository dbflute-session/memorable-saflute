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

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;
import org.dbflute.saflute.web.servlet.cookie.CookieManager;
import org.dbflute.saflute.web.servlet.session.SessionManager;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.seasar.struts.util.RequestUtil;
import org.seasar.struts.util.ServletContextUtil;

/**
 * @author jflute
 */
public class SimpleRequestManager implements RequestManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimpleRequestManager.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The manager of session. (NotNull: after initialization) */
    @Resource
    protected SessionManager sessionManager;;

    /** The manager of cookie. (NotNull: after initialization) */
    @Resource
    protected CookieManager cookieManager;;

    /** The provider of request user locale. (NotNull: after initialization) */
    protected UserLocaleProcessProvider localeHandler;

    /** The provider of request user time-zone. (NotNull: after initialization) */
    protected UserTimeZoneProcessProvider timeZoneProvider;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalServletDirection direction = assistOptionalServletDirection();
        localeHandler = direction.assistUserLocaleProcessProvider();
        timeZoneProvider = direction.assistUserTimeZoneProcessProvider();
        showBootLogging();
    }

    protected OptionalServletDirection assistOptionalServletDirection() {
        return assistantDirector.assistOptionalServletDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Request Manager]");
            LOG.info(" localeProvider: " + DfTypeUtil.toClassTitle(localeHandler));
            LOG.info(" timeZoneProvider: " + DfTypeUtil.toClassTitle(timeZoneProvider));
        }
    }

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    /**
     * {@inheritDoc}
     */
    public HttpServletRequest getRequest() {
        return RequestUtil.getRequest();
    }

    // ===================================================================================
    //                                                                  Parameter Handling
    //                                                                  ==================
    /**
     * {@inheritDoc}
     */
    public String getParameter(String key) {
        assertObjectNotNull("key", key);
        return getRequest().getParameter(key);
    }

    // ===================================================================================
    //                                                                  Attribute Handling
    //                                                                  ==================
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <ATTRIBUTE> ATTRIBUTE getAttribute(Class<ATTRIBUTE> type) {
        assertObjectNotNull("type", type);
        return (ATTRIBUTE) getRequest().getAttribute(type.getName());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <ATTRIBUTE> ATTRIBUTE getAttribute(String key) {
        assertObjectNotNull("key", key);
        return (ATTRIBUTE) getRequest().getAttribute(key);
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(Object value) {
        assertObjectNotNull("value", value);
        checkTypedAttributeSettingMistake(value);
        getRequest().setAttribute(value.getClass().getName(), value);
    }

    protected void checkTypedAttributeSettingMistake(Object value) {
        if (value instanceof String) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The value for typed attribute was simple string type.");
            br.addItem("Advice");
            br.addElement("The value should not be string.");
            br.addElement("Do you forget value setting for the string key?");
            br.addElement("The typed attribute setting cannot accept string");
            br.addElement("to suppress setting mistake like this:");
            br.addElement("  (x):");
            br.addElement("    requestManager.setAttribute(\"foo.bar\")");
            br.addElement("  (o):");
            br.addElement("    requestManager.setAttribute(\"foo.bar\", value)");
            br.addElement("  (o):");
            br.addElement("    requestManager.setAttribute(bean)");
            br.addItem("Specified Value");
            br.addElement(value != null ? value.getClass().getName() : null);
            br.addElement(value);
            final String msg = br.buildExceptionMessage();
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(String key, Object value) {
        assertObjectNotNull("key", key);
        assertObjectNotNull("value", value);
        getRequest().setAttribute(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Class<?> type) {
        assertObjectNotNull("type", type);
        getRequest().removeAttribute(type.getName());
    }

    public void remove(String key) {
        assertObjectNotNull("key", key);
        getRequest().removeAttribute(key);
    }

    // ===================================================================================
    //                                                                       Path Handling
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public String getRequestPath() {
        return extractActionRequestPath(getRequest());
    }

    protected String extractActionRequestPath(HttpServletRequest request) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
        // request specification:
        //   requestURI  : /dockside/member/list/foo%2fbar/
        //   servletPath : /member/list/foo/bar/
        //
        // so uses requestURI but it needs to remove context path
        //  -> /member/list/foo%2fbar/
        // = = = = = = = = = =/
        final String requestPath;
        final String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        if (contextPath != null && contextPath.trim().length() > 0 && !contextPath.equals("/")) { // e.g. /dockside
            // e.g. /dockside/member/list/ to /member/list/
            requestPath = Srl.removePrefix(requestURI, contextPath);
        } else { // no context path
            requestPath = requestURI;
        }
        return filterRequestPath(requestPath); // e.g. /member/list/foo%2fbar/
    }

    protected String filterRequestPath(String path) {
        return removeViewPrefixFromRequestPathIfNeeds(removeJSessionIDFromRequestPathIfNeeds(path));
    }

    protected String removeJSessionIDFromRequestPathIfNeeds(String path) {
        return Srl.substringFirstFrontIgnoreCase(path, ";" + getJSessionIDParameterKey());
    }

    protected String getJSessionIDParameterKey() {
        return "jsessionid";
    }

    protected String removeViewPrefixFromRequestPathIfNeeds(String path) { // from RequestUtil.getPath()
        final String viewPrefix = ServletContextUtil.getViewPrefix();
        if (viewPrefix == null) {
            return path;
        }
        if (path.startsWith(viewPrefix)) {
            return path.substring(viewPrefix.length());
        }
        return path;
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestPathAndQuery() {
        final String query = getQueryString();
        return getRequestPath() + (query != null && query.trim().length() > 0 ? "?" + query : "");
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString() {
        return getRequest().getQueryString();
    }

    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    /**
     * {@inheritDoc}
     */
    public String getRoutingOriginRequestPath() {
        final String origin = (String) getRequest().getAttribute(KEY_ROUTING_ORIGIN_REQUEST_PATH);
        return origin != null ? origin : getRequestPath();
    }

    /**
     * {@inheritDoc}
     */
    public String getRoutingOriginRequestPathAndQuery() {
        final String origin = (String) getRequest().getAttribute(KEY_ROUTING_ORIGIN_REQUEST_PATH_AND_QUERY);
        return origin != null ? origin : getRequestPathAndQuery();
    }

    // ===================================================================================
    //                                                                     Header Handling
    //                                                                     ===============
    /**
     * {@inheritDoc}
     */
    public String getHeader(String headerKey) {
        return getRequest().getHeader(headerKey);
    }

    /**
     * {@inheritDoc}
     */
    public String getHost() {
        return getHeader("Host");
    }

    /**
     * {@inheritDoc}
     */
    public String getReferer() {
        return getHeader("Referer");
    }

    /**
     * {@inheritDoc}
     */
    public String getUserAgent() {
        return getHeader("User-Agent");
    }

    // ===================================================================================
    //                                                                     Region Handling
    //                                                                     ===============
    // -----------------------------------------------------
    //                                           User Locale
    //                                           -----------
    /**
     * {@inheritDoc}
     */
    public Locale getUserLocale() {
        Locale locale = findCachedLocale();
        if (locale != null) {
            // mainly here if you call this in action process
            // because locale process is called before action
            return locale;
        }
        locale = findSessionLocale();
        if (locale != null) {
            return locale;
        }
        return getRequestedLocale();
    }

    /**
     * {@inheritDoc}
     */
    public Locale resolveUserLocale(ActionExecuteMeta executeMeta) {
        Locale locale = findCachedLocale();
        if (locale == null) {
            locale = findBusinessLocale(executeMeta);
        }
        if (locale == null) {
            locale = findCookieLocale(); // before session
        }
        if (locale == null) {
            locale = findSessionLocale(); // after cookie
        }
        if (locale == null) {
            locale = getRequestedLocale(); // not null
        }
        // not cookie here (should be saved in cookie explicitly)
        saveUserLocaleToSession(locale);
        return locale;
    }

    protected Locale findCachedLocale() {
        return (Locale) getAttribute(getReqeustUserLocaleKey());
    }

    protected Locale findBusinessLocale(ActionExecuteMeta executeMeta) {
        return localeHandler.findBusinessLocale(executeMeta, this);
    }

    protected Locale findCookieLocale() {
        if (!localeHandler.isAcceptCookieLocale()) {
            return null;
        }
        final String cookieLocaleKey = getCookieUserLocaleKey();
        final Cookie cookie = cookieManager.getCookie(cookieLocaleKey);
        if (cookie == null) {
            return null;
        }
        final String localeExp = cookie.getValue();
        if (localeExp == null || localeExp.trim().length() == 0) {
            return null;
        }
        final List<String> splitList = DfStringUtil.splitList(localeExp, "_");
        if (splitList.size() > 3) { // invalid e.g. foo_bar_qux_corge
            cookieManager.removeCookie(cookieLocaleKey);
            return null;
        }
        final String language = splitList.get(0); // always exists
        final String country = splitList.size() > 1 ? splitList.get(1) : null;
        final String variant = splitList.size() > 2 ? splitList.get(2) : null;
        try {
            return new Locale(language, country, variant);
        } catch (RuntimeException continued) { // just in case for user-side value
            if (LOG.isDebugEnabled()) {
                LOG.debug("*Cannot get locale: exp=" + localeExp + " e=" + continued.getMessage());
            }
            cookieManager.removeCookie(cookieLocaleKey);
            return null;
        }
    }

    protected Locale findSessionLocale() {
        return (Locale) sessionManager.getAttribute(getSessionUserLocaleKey());
    }

    protected Locale getRequestedLocale() {
        return getRequest().getLocale();
    }

    /**
     * {@inheritDoc}
     */
    public void saveUserLocaleToCookie(Locale locale) {
        if (!localeHandler.isAcceptCookieLocale()) {
            String msg = "Cookie locale is unavailable so nonsense: locale=" + locale;
            throw new IllegalStateException(msg);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Saving user locale to cokie: " + locale);
        }
        cookieManager.setCookie(getCookieUserLocaleKey(), locale.toString());
        setAttribute(getReqeustUserLocaleKey(), locale);
    }

    /**
     * {@inheritDoc}
     */
    public void saveUserLocaleToSession(Locale locale) {
        sessionManager.setAttribute(getSessionUserLocaleKey(), locale);
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Saving user locale to session: " + locale);
        }
        setAttribute(getReqeustUserLocaleKey(), locale);
    }

    protected String getReqeustUserLocaleKey() {
        return KEY_REQUEST_USER_LOCALE;
    }

    protected String getCookieUserLocaleKey() {
        return KEY_COOKIE_USER_LOCALE;
    }

    protected String getSessionUserLocaleKey() {
        return KEY_SESSION_USER_LOCALE;
    }

    // -----------------------------------------------------
    //                                         User TimeZone
    //                                         -------------
    /**
     * {@inheritDoc}
     */
    public TimeZone getUserTimeZone() {
        TimeZone timeZone = findCachedTimeZone();
        if (timeZone != null) {
            // mainly here if you call this in action process
            // because time-zone process is called before action
            return timeZone;
        }
        timeZone = findSessionTimeZone();
        if (timeZone != null) {
            return timeZone;
        }
        return getRequestedTimeZone();
    }

    /**
     * {@inheritDoc}
     */
    public TimeZone resolveUserTimeZone(ActionExecuteMeta executeMeta) {
        if (!timeZoneProvider.isUseTimeZoneHandling()) {
            return null;
        }
        TimeZone timeZone = findCachedTimeZone();
        if (timeZone == null) {
            timeZone = findBusinessTimeZone(executeMeta);
        }
        if (timeZone == null) {
            timeZone = findCookieTimeZone(); // before session
        }
        if (timeZone == null) {
            timeZone = findSessionTimeZone(); // after cookie
        }
        if (timeZone == null) {
            timeZone = getRequestedTimeZone(); // not null
        }
        // not cookie here (should be saved in cookie explicitly)
        saveUserTimeZoneToSession(timeZone);
        return timeZone;
    }

    protected TimeZone findCachedTimeZone() {
        return (TimeZone) getAttribute(getReqeustUserTimeZoneKey());
    }

    protected TimeZone findBusinessTimeZone(ActionExecuteMeta executeMeta) {
        return timeZoneProvider.findBusinessTimeZone(executeMeta, this);
    }

    protected TimeZone findCookieTimeZone() {
        if (!timeZoneProvider.isAcceptCookieTimeZone()) {
            return null;
        }
        final String cookieTimeZoneKey = getCookieUserTimeZoneKey();
        final Cookie cookie = cookieManager.getCookie(cookieTimeZoneKey);
        if (cookie == null) {
            return null;
        }
        final String timeZoneId = cookie.getValue();
        if (timeZoneId == null || timeZoneId.trim().length() == 0) {
            return null;
        }
        try {
            return TimeZone.getTimeZone(timeZoneId);
        } catch (RuntimeException continued) { // just in case for user-side value
            if (LOG.isDebugEnabled()) {
                LOG.debug("*Cannot get time-zone: id=" + timeZoneId + " e=" + continued.getMessage());
            }
            cookieManager.removeCookie(cookieTimeZoneKey);
            return null;
        }
    }

    protected TimeZone findSessionTimeZone() {
        return (TimeZone) sessionManager.getAttribute(getSessionUserTimeZoneKey());
    }

    protected TimeZone getRequestedTimeZone() {
        // unfortunately we cannot get time-zone from request
        // so it needs to provide the default time-zone
        return timeZoneProvider.getRequestedTimeZone(this);
    }

    /**
     * {@inheritDoc}
     */
    public void saveUserTimeZoneToCookie(TimeZone timeZone) {
        if (!timeZoneProvider.isAcceptCookieTimeZone()) {
            String msg = "Cookie time-zone is unavailable so nonsense: time-zone=" + timeZone;
            throw new IllegalStateException(msg);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Saving user time-zone to cookie: " + timeZone);
        }
        cookieManager.setCookie(getCookieUserTimeZoneKey(), timeZone.toString());
        setAttribute(getReqeustUserTimeZoneKey(), timeZone);
    }

    /**
     * {@inheritDoc}
     */
    public void saveUserTimeZoneToSession(TimeZone timeZone) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Saving user time-zone to session: " + timeZone);
        }
        sessionManager.setAttribute(getSessionUserTimeZoneKey(), timeZone);
        setAttribute(getReqeustUserTimeZoneKey(), timeZone);
    }

    protected String getReqeustUserTimeZoneKey() {
        return KEY_REQUEST_USER_TIMEZONE;
    }

    protected String getCookieUserTimeZoneKey() {
        return KEY_COOKIE_USER_TIMEZONE;
    }

    protected String getSessionUserTimeZoneKey() {
        return KEY_SESSION_USER_TIMEZONE;
    }

    // ===================================================================================
    //                                                                    Message Handling
    //                                                                    ================
    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    /**
     * {@inheritDoc}
     */
    public void saveErrors(String messageKey, Object... args) {
        assertObjectNotNull("messageKey", messageKey);
        doSaveErrors(prepareActionMessages(messageKey, args));
    }

    /**
     * {@inheritDoc}
     */
    public void saveErrors(ActionMessages errors) {
        assertObjectNotNull("errors", errors);
        doSaveErrors(errors);
    }

    protected void doSaveErrors(ActionMessages errors) {
        // you cannot use ActionMessagesUtil because it uses session directly
        // (you should use this.remove() and this.setAttribute() for external session) 
        if (errors == null || errors.isEmpty()) {
            remove(Globals.ERROR_KEY);
            return;
        }
        setAttribute(Globals.ERROR_KEY, errors);
    }

    /**
     * {@inheritDoc}
     */
    public void addErrors(String messageKey, Object... args) {
        assertObjectNotNull("messageKey", messageKey);
        doAddErrors(prepareActionMessages(messageKey, args));
    }

    protected void doAddErrors(ActionMessages errors) {
        if (errors == null) {
            return;
        }
        ActionMessages existingErrors = (ActionMessages) getErrors();
        if (existingErrors == null) {
            existingErrors = new ActionMessages();
        }
        existingErrors.add(errors);
        doSaveErrors(existingErrors);
    }

    protected ActionMessages prepareActionMessages(String messageKey, Object[] args) {
        final ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(messageKey, args));
        return messages;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasErrors() {
        final ActionMessages errors = (ActionMessages) getRequest().getAttribute(Globals.ERROR_KEY);
        return errors != null && !errors.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public ActionMessages getErrors() {
        return getAttribute(Globals.ERROR_KEY);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void assertObjectNotNull(String variableName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
