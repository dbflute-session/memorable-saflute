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
package org.dbflute.saflute.core.direction;

import org.dbflute.saflute.core.direction.exception.FwRequiredAssistNotFoundException;
import org.dbflute.saflute.core.exception.ExceptionTranslationProvider;
import org.dbflute.saflute.core.json.JsonResourceProvider;
import org.dbflute.saflute.core.magic.async.ConcurrentAsyncExecutorProvider;
import org.dbflute.saflute.core.security.SecurityResourceProvider;
import org.dbflute.saflute.core.time.TimeResourceProvider;

/**
 * The direction of core components.
 * <pre>
 * [Required]
 * o {@link #domainTitle}
 * o {@link #environmentTitle}
 * o {@link #securityResourceProvider}
 * o {@link #securityResourceProvider}
 * </pre>
 * @author jflute
 */
public class OptionalCoreDirection {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    /** Is development environment here? (you should set false if unknown) */
    protected boolean developmentHere;

    /** The string for title of domain application, displayed in boot logging. (NotNull: after direction) */
    protected String domainTitle;

    /** The string for title of current environment, displayed in boot logging. (NotNull: after direction) */
    protected String environmentTitle;

    /** Is debug enabled for framework? (you should set true only when you want internal debug) */
    protected boolean frameworkDebug;

    /** The callback for boot process, called-back when your application is booting. (NullAllowed: not required) */
    protected BootProcessCallback bootProcessCallback;

    // -----------------------------------------------------
    //                                              Security
    //                                              --------
    /** The provider of security resource, e.g. cipher. (NotNull: after direction) */
    protected SecurityResourceProvider securityResourceProvider;

    // -----------------------------------------------------
    //                                                  Time
    //                                                  ----
    /** The provider of time resource, e.g. cipher. (NotNull: after direction) */
    protected TimeResourceProvider timeResourceProvider;

    // -----------------------------------------------------
    //                                                  JSON
    //                                                  ----
    /** The provider of JSON resource. (NullAllowed) */
    protected JsonResourceProvider jsonResourceProvider;

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    /** The provider of exception translation. (NullAllowed) */
    protected ExceptionTranslationProvider exceptionTranslationProvider;

    // -----------------------------------------------------
    //                                          Asynchronous
    //                                          ------------
    /** The provider of concurrent executor. (NullAllowed) */
    protected ConcurrentAsyncExecutorProvider concurrentAsyncExecutorProvider;

    // ===================================================================================
    //                                                                     Direct Property
    //                                                                     ===============
    // -----------------------------------------------------
    //                                             Framework
    //                                             ---------
    // should be directed before other directions
    public void directDevelopmentHere(boolean developmentHere) {
        this.developmentHere = developmentHere;
    }

    public void directLoggingTitle(String domainTitle, String environmentTitle) {
        this.domainTitle = domainTitle;
        this.environmentTitle = environmentTitle;
    }

    public void directFrameworkDebug(boolean frameworkDebug) {
        this.frameworkDebug = frameworkDebug;
    }

    public void directBootProcessCallback(BootProcessCallback bootProcessCallback) {
        this.bootProcessCallback = bootProcessCallback;
    }

    // -----------------------------------------------------
    //                                              Security
    //                                              --------
    public void directSecurity(SecurityResourceProvider securityResourceProvider) {
        this.securityResourceProvider = securityResourceProvider;
    }

    // -----------------------------------------------------
    //                                                  Time
    //                                                  ----
    public void directTime(TimeResourceProvider timeResourceProvider) {
        this.timeResourceProvider = timeResourceProvider;
    }

    // -----------------------------------------------------
    //                                                  JSON
    //                                                  ----
    public void directJson(JsonResourceProvider jsonResourceProvider) {
        this.jsonResourceProvider = jsonResourceProvider;
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    public void directException(ExceptionTranslationProvider exceptionTranslationProvider) {
        this.exceptionTranslationProvider = exceptionTranslationProvider;
    }

    // -----------------------------------------------------
    //                                          Asynchronous
    //                                          ------------
    public void directAsync(ConcurrentAsyncExecutorProvider concurrentAsyncExecutorProvider) {
        this.concurrentAsyncExecutorProvider = concurrentAsyncExecutorProvider;
    }

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    public boolean isDevelopmentHere() {
        return developmentHere;
    }

    public String assistDomainTitle() { // required
        if (domainTitle == null) {
            String msg = "Not found the title of domain application.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return domainTitle;
    }

    public String assistEnvironmentTitle() { // required
        if (environmentTitle == null) {
            String msg = "Not found the title of current environment.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return environmentTitle;
    }

    public boolean isFrameworkDebug() {
        return frameworkDebug;
    }

    public BootProcessCallback assistBootProcessCallback() {
        return bootProcessCallback; // not required, no process if null
    }

    // -----------------------------------------------------
    //                                              Security
    //                                              --------
    public SecurityResourceProvider assistSecurityResourceProvider() {
        if (securityResourceProvider == null) {
            String msg = "Not found the provider of security resource.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return securityResourceProvider;
    }

    // -----------------------------------------------------
    //                                                  Time
    //                                                  ----
    public TimeResourceProvider assistTimeResourceProvider() {
        if (timeResourceProvider == null) {
            String msg = "Not found the provider of time resource.";
            throw new FwRequiredAssistNotFoundException(msg);
        }
        return timeResourceProvider;
    }

    // -----------------------------------------------------
    //                                                  JSON
    //                                                  ----
    public JsonResourceProvider assistJsonResourceProvider() {
        return jsonResourceProvider; // not required, has default for compatibility
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    public ExceptionTranslationProvider assistExceptionTranslationProvider() {
        return exceptionTranslationProvider; // not required, has default for compatibility
    }

    // -----------------------------------------------------
    //                                          Asynchronous
    //                                          ------------
    public ConcurrentAsyncExecutorProvider assistConcurrentAsyncExecutorProvider() {
        return concurrentAsyncExecutorProvider; // not required, has default for compatibility
    }
}
