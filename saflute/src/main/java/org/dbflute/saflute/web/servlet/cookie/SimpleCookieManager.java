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
package org.dbflute.saflute.web.servlet.cookie;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.direction.exception.FwRequiredAssistNotFoundException;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;
import org.dbflute.saflute.web.servlet.cookie.exception.CookieCipherDecryptFailureException;
import org.seasar.struts.util.RequestUtil;
import org.seasar.struts.util.ResponseUtil;

/**
 * @author jflute
 */
public class SimpleCookieManager implements CookieManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(SimpleCookieManager.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The cipher for cookie's value. (NotNull) */
    @Resource
    protected CookieCipher cookieCipher;

    /** The default path when no specified expire. (NotNull: after initialization) */
    protected String defaultPath;

    /** The default expire (max age) when no specified expire. (NotNull: after initialization) */
    protected Integer defaultExpire;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalServletDirection direction = assistOptionalServletDirection();
        defaultPath = direction.assistCookieResourceProvider().provideDefaultPath();
        if (defaultPath == null) {
            final String msg = "No assist for the default path of cookie.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        defaultExpire = direction.assistCookieResourceProvider().provideDefaultExpire();
        if (defaultExpire == null) {
            final String msg = "No assist for the default expire of cookie.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        showBootLogging();
    }

    protected OptionalServletDirection assistOptionalServletDirection() {
        return assistantDirector.assistOptionalServletDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Cookie Manager]");
            LOG.info(" cookieCipher: " + cookieCipher);
            LOG.info(" defaultExpire: " + defaultExpire);
        }
    }

    // ===================================================================================
    //                                                                     Cookie Handling
    //                                                                     ===============
    // -----------------------------------------------------
    //                                                   Set
    //                                                   ---
    @Override
    public void setCookie(final String key, final String value) {
        assertKeyNotNull(key);
        assertValueNotNull(key, value);
        setCookie(key, value, getDefaultExpire());
    }

    @Override
    public void setCookie(final String key, final String value, final int expire) {
        assertKeyNotNull(key);
        assertValueNotNull(key, value);
        doSetCookie(key, value, getDefaultPath(), expire);
    }

    @Override
    public void setCookieCiphered(final String key, final String value) {
        assertKeyNotNull(key);
        assertValueNotNull(key, value);
        setCookieCiphered(key, value, getDefaultExpire());
    }

    @Override
    public void setCookieCiphered(final String key, final String value, final int expire) {
        assertKeyNotNull(key);
        assertValueNotNull(key, value);
        assertExpirePositive(expire);
        final String encrypted = cookieCipher.encrypt(value);
        doSetCookie(key, encrypted, getDefaultPath(), expire);
    }

    protected void doSetCookie(final String key, final String value, final String path, final int expire) {
        final Cookie cookie = new Cookie(key, value);
        cookie.setPath(path);
        cookie.setMaxAge(expire);
        setCookieDirectly(cookie);
    }

    @Override
    public void setCookieDirectly(final Cookie cookie) {
        assertCookieNotNull(cookie);
        getResponse().addCookie(cookie);
    }

    @Override
    public void setCookieDirectlyCiphered(final Cookie cookie) {
        assertCookieNotNull(cookie);
        final String value = cookie.getValue();
        if (value != null) {
            cookie.setValue(cookieCipher.encrypt(value));
        }
        setCookieDirectly(cookie);
    }

    // -----------------------------------------------------
    //                                                   Get
    //                                                   ---
    public Cookie getCookie(String key) {
        assertKeyNotNull(key);
        final Cookie[] cookies = getRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    return createSnapshotCookie(cookie);
                }
            }
        }
        return null;
    }

    protected Cookie createSnapshotCookie(Cookie src) {
        // not use close() to avoid dependency to ServletContainer
        final Cookie snapshot = new Cookie(src.getName(), src.getValue());
        snapshot.setPath(src.getPath());
        snapshot.setMaxAge(src.getMaxAge());
        final String domain = src.getDomain();
        if (domain != null) { // the setter has filter process
            snapshot.setDomain(domain);
        }
        snapshot.setSecure(src.getSecure());
        final String comment = src.getComment();
        if (comment != null) { // just in case
            snapshot.setComment(comment);
        }
        snapshot.setVersion(src.getVersion());
        return snapshot;
    }

    public Cookie getCookieCiphered(String key) {
        assertKeyNotNull(key);
        final Cookie cookie = getCookie(key);
        if (cookie != null) {
            final String value = cookie.getValue();
            if (value != null) {
                try {
                    cookie.setValue(cookieCipher.decrypt(value));
                } catch (CookieCipherDecryptFailureException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("...Ignoring decrypt failure to avoid hack cookie: " + value);
                    }
                    return null; // treated as not found
                }
            }
        }
        return cookie;
    }

    // -----------------------------------------------------
    //                                                Remove
    //                                                ------
    @Override
    public void removeCookie(final String key) {
        assertKeyNotNull(key);
        removeCookie(key, getDefaultPath());
    }

    @Override
    public void removeCookie(final String key, final String path) {
        assertKeyNotNull(key);
        assertPathNotNull(path);
        final Cookie cookie = new Cookie(key, "");
        cookie.setPath(path);
        cookie.setMaxAge(0);
        setCookieDirectly(cookie);
    }

    // -----------------------------------------------------
    //                                               Default
    //                                               -------
    protected String getDefaultPath() {
        if (defaultPath == null) {
            final String msg = "Not found the default path of cookie.";
            throw new IllegalStateException(msg);
        }
        return defaultPath;
    }

    protected Integer getDefaultExpire() {
        if (defaultExpire == null) {
            final String msg = "Not found the default expire of cookie.";
            throw new IllegalStateException(msg);
        }
        return defaultExpire;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertValueNotNull(final String key, final String value) {
        if (value == null) {
            final String msg = "The argument 'value' should not be null: key=" + key;
            throw new FwRequiredAssistNotFoundException(msg);
        }
    }

    protected void assertKeyNotNull(final String key) {
        if (key == null) {
            final String msg = "The argument 'key' should not be null.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
    }

    protected void assertExpirePositive(final int expire) {
        if (expire <= 0) {
            final String msg = "The argument 'expire' should not be zero and minus.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
    }

    protected void assertPathNotNull(final String path) {
        if (path == null) {
            final String msg = "The argument 'path' should not be null.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
    }

    protected void assertCookieNotNull(final Cookie cookie) {
        if (cookie == null) {
            final String msg = "The argument 'cookie' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected HttpServletRequest getRequest() {
        return RequestUtil.getRequest();
    }

    protected HttpServletResponse getResponse() {
        return ResponseUtil.getResponse();
    }
}
