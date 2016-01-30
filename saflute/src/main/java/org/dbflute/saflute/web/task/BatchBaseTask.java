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
package org.dbflute.saflute.web.task;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.bhv.proposal.callback.TraceableSqlAdditionalInfoProvider;
import org.dbflute.hook.AccessContext;
import org.dbflute.hook.CallbackContext;
import org.dbflute.hook.SqlFireHook;
import org.dbflute.hook.SqlStringFilter;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.exception.ExceptionTranslator;
import org.dbflute.saflute.core.magic.ThreadCacheContext;
import org.dbflute.saflute.core.magic.TransactionTimeContext;
import org.dbflute.saflute.db.dbflute.accesscontext.AccessContextArranger;
import org.dbflute.saflute.db.dbflute.accesscontext.AccessContextResource;
import org.dbflute.saflute.db.dbflute.accesscontext.PreparedAccessContext;
import org.dbflute.saflute.db.dbflute.callbackcontext.RomanticTraceableSqlFireHook;
import org.dbflute.saflute.db.dbflute.callbackcontext.RomanticTraceableSqlStringFilter;
import org.dbflute.saflute.web.action.login.UserBean;
import org.dbflute.saflute.web.task.callback.TaskCallback;
import org.dbflute.saflute.web.task.callback.TaskExecuteMeta;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;
import org.seasar.chronos.core.exception.ExecutionRuntimeException;
import org.seasar.framework.util.ClassUtil;

/**
 * @author jflute
 */
public abstract class BatchBaseTask implements TaskCallback {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(BatchBaseTask.class);
    private static final String SUFFIX_ENHANCED_CLASS = "$$EnhancedByS2AOP$$";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The translator of exception. (NotNull) */
    @Resource
    protected ExceptionTranslator exceptionTranslator;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BatchBaseTask() {
    }

    // ===================================================================================
    //                                                                               Login
    //                                                                               =====
    /**
     * Get the bean of login user on session. <br>
     * It also returns empty bean without updating session even if no login.
     * @return The found or new-created empty bean. (NotNull)
     */
    protected abstract UserBean getUserBean(); // fixedly no login here

    // ===================================================================================
    //                                                                            Callback
    //                                                                            ========
    /**
     * {@inheritDoc}
     */
    @Override
    public void godHandActionPrologue(TaskExecuteMeta executeMeta) {
        if (isLogEnabled()) {
            log("...Beginning " + buildInvokeName(executeMeta));
        }
        arrangeThreadCacheContext(executeMeta);
        arrangePreparedAccessContext(executeMeta);
        arrangeCallbackContext(executeMeta);
    }

    protected void arrangeThreadCacheContext(TaskExecuteMeta executeMeta) {
        if (ThreadCacheContext.exists()) { // basically true, just in case
            ThreadCacheContext.registerEntryMethod(executeMeta.getTaskMethod());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void godHandBefore(TaskExecuteMeta executeMeta) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callbackBefore(TaskExecuteMeta executeMeta) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String godHandSuccessMonologue(TaskExecuteMeta executeMeta) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean godHandExceptionMonologue(TaskExecuteMeta executeMeta) {
        final RuntimeException failureCause = executeMeta.getFailureCause();
        if (failureCause == null) { // basically no way (just in case)
            return false;
        }
        Throwable checkedCause = null;
        if (failureCause instanceof ExecutionRuntimeException) {
            final Throwable execNestEx = ((ExecutionRuntimeException) failureCause).getCause();
            if (execNestEx instanceof ExecutionException) {
                checkedCause = ((ExecutionException) execNestEx).getCause();
            }
        }
        if (checkedCause == null) {
            checkedCause = failureCause;
        }
        if (handleTranslatedException(checkedCause)) {
            return true;
        }
        // only error logging method is called directly for invocation in log
        LOG.error("Failed to execute the task with failure cause.", failureCause);
        return true;
    }

    protected boolean handleTranslatedException(Throwable checkedCause) {
        if (checkedCause instanceof RuntimeException) {
            try {
                translateException((RuntimeException) checkedCause);
            } catch (RuntimeException translated) {
                LOG.error("Failed to execute the task with translated cause.", translated);
                return true;
            }
        }
        return false;
    }

    protected void translateException(RuntimeException cause) {
        exceptionTranslator.translateException(cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callbackFinally(TaskExecuteMeta executeMeta) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void godHandFinally(TaskExecuteMeta executeMeta) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void godHandActionEpilogue(TaskExecuteMeta executeMeta) {
        clearCallbackContext();
        clearPreparedAccessContext();

        // clear various contexts just in case
        TransactionTimeContext.clear();
        AccessContext.clearAccessContextOnThread();

        if (isLogEnabled()) {
            log("...Ending " + buildInvokeName(executeMeta));
        }
    }

    // ===================================================================================
    //                                                                      Access Context
    //                                                                      ==============
    /**
     * Arrange prepared access context for DBFlute, which is used for common columns setup. <br>
     * This is called by start process so you should NOT call this directly in your task.
     * @param executeMeta The meta of action execute. (NotNull)
     */
    protected void arrangePreparedAccessContext(TaskExecuteMeta executeMeta) {
        final AccessContextArranger arranger = createAccessContextArranger();
        final String moduleName = buildModuleName(getClass());
        final AccessContextResource resource = createAccessContextResource(moduleName);
        final AccessContext accessContext = arranger.arrangePreparedAccessContext(resource);
        PreparedAccessContext.setAccessContextOnThread(accessContext);
    }

    protected String buildModuleName(Class<?> type) {
        return getCapitalizeClassName(type);
    }

    /**
     * Create the arranger of access context.
     * @param moduleName The module name of the current access process. (NotNull)
     * @return The instance of arranger. (NotNull)
     */
    protected AccessContextResource createAccessContextResource(String moduleName) {
        return new AccessContextResource(moduleName, null); // cannot get method object here
    }

    /**
     * Create the arranger of access context.
     * @return The instance of arranger. (NotNull)
     */
    protected abstract AccessContextArranger createAccessContextArranger();

    /**
     * Clear prepared access context. <br>
     * This is called by start process so you should NOT call this directly in your task.
     */
    protected void clearPreparedAccessContext() {
        PreparedAccessContext.clearAccessContextOnThread();
    }

    // ===================================================================================
    //                                                                    Callback Context
    //                                                                    ================
    /**
     * Arrange callback context for DBFlute, which is used for several purpose. <br>
     * This is called by callback process so you should NOT call this directly in your task.
     * @param executeMeta The meta of action execute. (NotNull)
     */
    protected void arrangeCallbackContext(TaskExecuteMeta executeMeta) {
        final SqlFireHook sqlFireHook = createSqlFireHook(executeMeta);
        CallbackContext.setSqlFireHookOnThread(sqlFireHook);
        final SqlStringFilter filter = createSqlStringFilter(executeMeta);
        CallbackContext.setSqlStringFilterOnThread(filter);
    }

    /**
     * Create the filter of SQL string for DBFlute.
     * @param executeMeta The meta of task execute. (NotNull)
     * @return The hook of SQL fire. (NullAllowed: if null, no hook)
     */
    protected SqlFireHook createSqlFireHook(TaskExecuteMeta executeMeta) {
        return newRomanticTraceableSqlFireHook();
    }

    protected RomanticTraceableSqlFireHook newRomanticTraceableSqlFireHook() {
        return new RomanticTraceableSqlFireHook();
    }

    /**
     * Create the filter of SQL string for DBFlute.
     * @param executeMeta The meta of action execute. (NotNull)
     * @return The filter of SQL string. (NullAllowed: if null, no filter)
     */
    protected SqlStringFilter createSqlStringFilter(TaskExecuteMeta executeMeta) {
        final Method taskMethod = executeMeta.getTaskMethod();
        return newRomanticTraceableSqlStringFilter(taskMethod, new TraceableSqlAdditionalInfoProvider() {
            public String provide() { // lazy because it may be auto-login later
                return buildSqlMarkingAdditionalInfo();
            }
        });
    }

    protected RomanticTraceableSqlStringFilter newRomanticTraceableSqlStringFilter(Method actionMethod,
            TraceableSqlAdditionalInfoProvider additionalInfoProvider) {
        return new RomanticTraceableSqlStringFilter(actionMethod, additionalInfoProvider);
    }

    /**
     * Build string for additional info of SQL marking.
     * @return The string expression of additional info. (NullAllowed: if null, no additional info)
     */
    protected String buildSqlMarkingAdditionalInfo() {
        final UserBean bean = getUserBean();
        return "(" + bean.getUserType() + ", " + bean.getDomainType() + ")";
        // it doesn't contain user ID for SQL cache in DBMS
    }

    /**
     * Clear callback context. <br>
     * This is called by callback process so you should NOT call this directly in your task.
     */
    protected void clearCallbackContext() {
        CallbackContext.clearSqlStringFilterOnThread();
        CallbackContext.clearSqlFireHookOnThread();
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    protected boolean isLogEnabled() {
        return isNormalExecutionInfoLogging() ? LOG.isInfoEnabled() : LOG.isDebugEnabled();
    }

    protected void log(String msg) {
        // trace logging have no strong need of invocation in log so method delegation here
        if (isNormalExecutionInfoLogging()) {
            LOG.info(msg);
        } else {
            LOG.debug(msg);
        }
    }

    protected boolean isNormalExecutionInfoLogging() {
        final OptionalTaskDirection direction = assistantDirector.assistOptionalTaskDirection();
        return direction.isNormalExecutionInfoLogging();
    }

    protected String buildInvokeName(TaskExecuteMeta executeMeta) {
        final String classTitle = substringFirstFront(toClassTitle(this), "$");
        final String methodName = executeMeta.getTaskMethod().getName();
        return classTitle + "." + methodName + "()";
    }

    protected String toClassTitle(final Object obj) {
        return DfTypeUtil.toClassTitle(obj);
    }

    protected String substringFirstFront(String str, String... delimiters) {
        return DfStringUtil.substringFirstFront(str, delimiters);
    }

    // ===================================================================================
    //                                                                     Chronos Process
    //                                                                     ===============
    /**
     * Initialize the task instance. <br>
     * This is only-one called by s2chronos (so must be public) when the task is created.
     */
    public void initialize() { // called by s2chronos (so must be public)
    }

    /**
     * Start the process of the task execution. <br>
     * This is called by s2chronos (so must be public) before the task execution. <br>
     * The thread for this method might be different with task execution (real story),
     * so you cannot use this for thread local settings, e.g. {@link AccessContext}.
     */
    public void start() { // called by s2chronos (so must be public)
        // basically do nothing, use callback process instead
    }

    /**
     * Catch the exception from the task execution. <br>
     * This is called by s2chronos (so must be public) when the exception is thrown in the task execution.
     * @param e The thrown exception from the task execution, might be {@link ExecutionRuntimeException}. (NotNull)
     */
    public void catchException(Exception e) { // called by s2chronos (so must be public)
        // basically no way because it is caught by callback process
        // however it's as final logging
        LOG.error("Enexpected exception was thrown: #" + Integer.toHexString(e.hashCode()), e);
    }

    /**
     * End the process of the task execution. <br>
     * This is called by s2chronos (so must be public) after the task execution (even if exception). <br>
     * The thread for this method might be different with task execution (real story),
     * so you cannot use this for thread local clearing, e.g. {@link AccessContext}.
     */
    public void end() { // called by s2chronos (so must be public)
        // basically do nothing, use callback process instead
    }

    /**
     * Destroy the task instance. <br>
     * This is only-one called by s2chronos (so must be public) when the task is destroyed.
     */
    public void destroy() { // called by s2chronos (so must be public)
    }

    // ===================================================================================
    //                                                                         Name Helper
    //                                                                         ===========
    protected String getCapitalizeClassName(Class<?> type) {
        final String shortClassName = ClassUtil.getShortClassName(type);
        return StringUtils.substringBefore(shortClassName, SUFFIX_ENHANCED_CLASS);
    }

    protected String getUncapitalizeClassName(Class<?> type) {
        return StringUtils.uncapitalize(getCapitalizeClassName(type));
    }
}
