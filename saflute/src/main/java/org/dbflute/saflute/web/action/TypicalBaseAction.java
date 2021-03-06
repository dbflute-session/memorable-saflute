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
package org.dbflute.saflute.web.action;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMessages;
import org.dbflute.bhv.proposal.callback.ExecutedSqlCounter;
import org.dbflute.bhv.proposal.callback.TraceableSqlAdditionalInfoProvider;
import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.exception.EntityAlreadyExistsException;
import org.dbflute.exception.EntityAlreadyUpdatedException;
import org.dbflute.hook.AccessContext;
import org.dbflute.hook.CallbackContext;
import org.dbflute.hook.SqlFireHook;
import org.dbflute.hook.SqlStringFilter;
import org.dbflute.saflute.core.exception.ApplicationBaseException;
import org.dbflute.saflute.core.exception.ExceptionTranslator;
import org.dbflute.saflute.core.magic.ThreadCacheContext;
import org.dbflute.saflute.core.time.TimeManager;
import org.dbflute.saflute.db.dbflute.accesscontext.AccessContextArranger;
import org.dbflute.saflute.db.dbflute.accesscontext.AccessContextResource;
import org.dbflute.saflute.db.dbflute.accesscontext.PreparedAccessContext;
import org.dbflute.saflute.db.dbflute.callbackcontext.RomanticTraceableSqlFireHook;
import org.dbflute.saflute.db.dbflute.callbackcontext.RomanticTraceableSqlStringFilter;
import org.dbflute.saflute.web.action.api.ApiManager;
import org.dbflute.saflute.web.action.api.ApiResult;
import org.dbflute.saflute.web.action.callback.ActionCallback;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.dbflute.saflute.web.action.exception.ForcedIllegalTransitionApplicationException;
import org.dbflute.saflute.web.action.exception.ForcedRequest404NotFoundException;
import org.dbflute.saflute.web.action.exception.GetParameterNotFoundException;
import org.dbflute.saflute.web.action.exception.MessageKeyApplicationException;
import org.dbflute.saflute.web.action.login.LoginHandlingResource;
import org.dbflute.saflute.web.action.login.LoginManager;
import org.dbflute.saflute.web.action.login.UserBean;
import org.dbflute.saflute.web.action.login.exception.LoginFailureException;
import org.dbflute.saflute.web.action.login.exception.LoginTimeoutException;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.session.SessionManager;
import org.dbflute.saflute.web.servlet.taglib.MyErrorsTag;
import org.dbflute.saflute.web.servlet.taglib.MyHtmlMessagesTag;
import org.dbflute.util.DfTypeUtil;

/**
 * The typical base action for your project. <br>
 * You should extend this class when making your project-base action. <br>
 * And you can add methods for all applications.
 * @author jflute
 */
public abstract class TypicalBaseAction extends RootAction implements ActionCallback {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(TypicalBaseAction.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The manager of time. (NotNull) */
    @Resource
    protected TimeManager timeManager;

    /** The manager of session. (NotNull) */
    @Resource
    protected SessionManager sessionManager;

    /** The manager of login. (NotNull) */
    @Resource
    protected LoginManager loginManager;

    /** The translator of exception. (NotNull) */
    @Resource
    protected ExceptionTranslator exceptionTranslator;

    /** The manager of API. (NotNull) */
    @Resource
    protected ApiManager apiManager;

    // ===================================================================================
    //                                                                               Login
    //                                                                               =====
    /**
     * Get the bean of login user on session. <br>
     * It also returns empty bean without updating session even if no login.
     * @return The found or new-created empty bean. (NotNull)
     */
    protected abstract UserBean getUserBean();

    /**
     * Get the bean of login user on session with exiting check.
     * @return The found bean in session. (NotNull: if not login, throws exception)
     * @throws LoginTimeoutException When the login user is not found on session.
     */
    protected UserBean getUserBeanChecked() {
        final UserBean userBean = getUserBean();
        if (!userBean.isLogin()) {
            String msg = "Not found the user bean on the session for any login user.";
            throw new LoginTimeoutException(msg);
        }
        return userBean;
    }

    // ===================================================================================
    //                                                                             Message
    //                                                                             =======
    /**
     * Create the action messages basically for session global errors or messages.
     * @return The new-created action messages provided from Struts. (NotNull)
     */
    protected ActionMessages createActionMessages() { // should be overridden as type-safe properties
        return new ActionMessages();
    }

    // -----------------------------------------------------
    //                                                Errors
    //                                                ------
    /**
     * Save message as (global) action errors. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyErrorsTag}.
     * @param messageKey The message key to be saved. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    protected void saveErrors(String messageKey, Object... args) {
        sessionManager.saveErrors(messageKey, args);
    }

    /**
     * Save message as (global) action errors. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyErrorsTag}.
     * @param errors The action message for errors. (NotNull, EmptyAllowed: removes existing errors)
     */
    protected void saveErrors(ActionMessages errors) {
        sessionManager.saveErrors(errors);
    }

    /**
     * Add message as (global) action errors to rear of existing messages. <br>
     * This message will be deleted immediately after display if you use {@link MyErrorsTag}.
     * @param messageKey The message key to be added. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    protected void addErrors(String messageKey, Object... args) {
        sessionManager.addErrors(messageKey, args);
    }

    /**
     * Does it have messages as (global or specified property) action errors at least one?
     * @return The determination, true or false.
     */
    protected boolean hasErrors() {
        return sessionManager.hasErrors();
    }

    /**
     * Get action message from (global) action errors.
     * @return The object for action message. (NullAllowed: if no errors in session)
     */
    protected ActionMessages getErrors() {
        return sessionManager.getErrors();
    }

    /**
     * Clear (global) action errors from session.
     */
    protected void clearErrors() {
        sessionManager.clearErrors();
    }

    // -----------------------------------------------------
    //                                              Messages
    //                                              --------
    /**
     * Save message as (global) action messages. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyHtmlMessagesTag}.
     * @param messageKey The message key to be saved. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    protected void saveMessages(String messageKey, Object... args) {
        sessionManager.saveMessages(messageKey, args);
    }

    /**
     * Save message as (global) action messages. (after deleting existing messages) <br>
     * This message will be deleted immediately after display if you use {@link MyHtmlMessagesTag}.
     * @param messages The action message for messages. (NotNull, EmptyAllowed: removes existing messages)
     */
    protected void saveMessages(ActionMessages messages) {
        sessionManager.saveMessages(messages);
    }

    /**
     * Add message as (global) action messages to rear of existing messages. <br>
     * This message will be deleted immediately after display if you use {@link MyHtmlMessagesTag}.
     * @param messageKey The message key to be added. (NotNull)
     * @param args The varying array of arguments for the message. (NullAllowed, EmptyAllowed)
     */
    protected void addMessages(String messageKey, Object... args) {
        sessionManager.addMessages(messageKey, args);
    }

    /**
     * Does it have messages as (global or specified property) action messages at least one?
     * @return The determination, true or false.
     */
    protected boolean hasMessages() {
        return sessionManager.hasMessages();
    }

    /**
     * Get action message from (global) action errors.
     * @return The object for action message. (NullAllowed: if no messages in session)
     */
    protected ActionMessages getMessages() {
        return sessionManager.getMessages();
    }

    /**
     * Clear (global) action messages from session.
     */
    protected void clearMessages() {
        sessionManager.clearMessages();
    }

    // ===================================================================================
    //                                                                            Callback
    //                                                                            ========
    // [typical callback process]
    // read the source code for the details
    // (because of no comment here)
    // -----------------------------------------------------
    //                                                Before
    //                                                ------
    @Override
    public String godHandActionPrologue(final ActionExecuteMeta executeMeta) { // fixed process
        arrangeThreadCacheContextBasicItem(executeMeta);
        arrangePreparedAccessContext(executeMeta);
        arrangeCallbackContext(executeMeta); // should be after access-context (using access context's info)

        // should be after access-context (may have update)
        final String redirectTo = handleLoginRequiredCheck(executeMeta);
        if (redirectTo != null) {
            return redirectTo;
        }
        arrangeThreadCacheContextLoginItem(executeMeta);
        return null;
    }

    protected void arrangeThreadCacheContextBasicItem(ActionExecuteMeta executeMeta) {
        if (ThreadCacheContext.exists()) { // basically true, just in case
            ThreadCacheContext.registerRequestPath(requestManager.getRoutingOriginRequestPathAndQuery());
            ThreadCacheContext.registerEntryMethod(executeMeta.getActionMethod());
        }
    }

    protected void arrangeThreadCacheContextLoginItem(ActionExecuteMeta executeMeta) {
        if (ThreadCacheContext.exists()) { // basically true, just in case
            ThreadCacheContext.registerUserBean(getUserBean()); // basically for asynchronous
        }
    }

    @Override
    public String godHandBefore(ActionExecuteMeta executeMeta) { // you can override
        return null;
    }

    @Override
    public String callbackBefore(ActionExecuteMeta executeMeta) { // you can override
        return null;
    }

    // -----------------------------------------------------
    //                                            on Success
    //                                            ----------
    @Override
    public String godHandSuccessMonologue(ActionExecuteMeta executeMeta) {
        final String redirectTo = handleLoginPerformRedirect(executeMeta);
        if (redirectTo != null) {
            return redirectTo;
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            on Failure
    //                                            ----------
    @Override
    public String godHandExceptionMonologue(ActionExecuteMeta executeMeta) { // fixed process
        return handleActionException(executeMeta);
    }

    protected String handleActionException(ActionExecuteMeta executeMeta) {
        final RuntimeException cause = executeMeta.getFailureCause();
        RuntimeException translated = null;
        try {
            translateException(cause);
        } catch (RuntimeException e) {
            translated = e;
        }
        final RuntimeException handlingEx = translated != null ? translated : cause;
        final String nextPath = handleApplicationException(executeMeta, handlingEx);
        if (nextPath != null) {
            return nextPath;
        }
        if (translated != null) {
            throw translated;
        }
        return null;
    }

    protected void translateException(RuntimeException cause) {
        exceptionTranslator.translateException(cause);
    }

    // -----------------------------------------------------
    //                                               Finally
    //                                               -------
    @Override
    public void callbackFinally(ActionExecuteMeta executeMeta) { // you can override
    }

    @Override
    public void godHandFinally(ActionExecuteMeta executeMeta) { // you can override
    }

    @Override
    public void godHandActionEpilogue(ActionExecuteMeta executeMeta) { // fixed process
        if (executeMeta.isForwardToJsp()) {
            arrangeNoCacheResponseWhenJsp(executeMeta);
        }
        handleSqlCount(executeMeta);
        clearCallbackContext();
        clearPreparedAccessContext();
    }

    protected void arrangeNoCacheResponseWhenJsp(ActionExecuteMeta executeMeta) {
        responseManager.addNoCache();
    }

    // -----------------------------------------------------
    //                                            Adjustment
    //                                            ----------
    @Override
    protected String movedPermanently(String redirectUrl) {
        // e.g. godHandBefore() needs dummy to stop execution
        super.movedPermanently(redirectUrl);
        return responseResolved();
    }

    /**
     * Return as response resolved. <br>
     * Basically used in action callback or action execute. <br>
     * You should use this to stop execution in callback.
     * <pre>
     * ... // resolve response by other way
     * return responseResolved(); // stop execution after here
     * </pre>
     * @return The dummy value that means resolved. (NotNull)
     */
    protected String responseResolved() {
        return ActionCallback.RESPONSE_RESOLVED_DUMMY_FORWARD;
    }

    // ===================================================================================
    //                                                                      Access Context
    //                                                                      ==============
    /**
     * Arrange prepared access context for DBFlute, which is used for common columns setup. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     * @param executeMeta The meta of action execute. (NotNull)
     */
    protected void arrangePreparedAccessContext(ActionExecuteMeta executeMeta) { // called by callback
        final AccessContextArranger arranger = createAccessContextArranger();
        final AccessContextResource resource = createAccessContextResource(executeMeta);
        final AccessContext accessContext = arranger.arrangePreparedAccessContext(resource);
        PreparedAccessContext.setAccessContextOnThread(accessContext);
    }

    /**
     * Create the arranger of access context.
     * @return The instance of arranger. (NotNull)
     */
    protected abstract AccessContextArranger createAccessContextArranger();

    /**
     * Create the resource of access context.
     * @param executeMeta The meta of action execute. (NotNull)
     * @return The new-created resource of access context. (NotNull)
     */
    protected AccessContextResource createAccessContextResource(ActionExecuteMeta executeMeta) {
        final Method method = executeMeta.getActionMethod();
        final String classTitle = DfTypeUtil.toClassTitle(method.getDeclaringClass());
        return new AccessContextResource(classTitle, method);
    }

    /**
     * Clear prepared access context. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     */
    protected void clearPreparedAccessContext() { // called by callback
        PreparedAccessContext.clearAccessContextOnThread();
    }

    // ===================================================================================
    //                                                                    Callback Context
    //                                                                    ================
    /**
     * Arrange callback context for DBFlute, which is used for several purpose. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     * @param executeMeta The meta of action execute. (NotNull)
     */
    protected void arrangeCallbackContext(final ActionExecuteMeta executeMeta) {
        final SqlFireHook sqlFireHook = createSqlFireHook(executeMeta);
        CallbackContext.setSqlFireHookOnThread(sqlFireHook);
        final SqlStringFilter filter = createSqlStringFilter(executeMeta);
        CallbackContext.setSqlStringFilterOnThread(filter);
    }

    /**
     * Create the filter of SQL string for DBFlute.
     * @param executeMeta The meta of action execute. (NotNull)
     * @return The hook of SQL fire. (NullAllowed: if null, no hook)
     */
    protected SqlFireHook createSqlFireHook(ActionExecuteMeta executeMeta) {
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
    protected SqlStringFilter createSqlStringFilter(final ActionExecuteMeta executeMeta) {
        final Method actionMethod = executeMeta.getActionMethod();
        return newRomanticTraceableSqlStringFilter(actionMethod, new TraceableSqlAdditionalInfoProvider() {
            @Override
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
        return "{" + bean.getUserType() + ", " + bean.getDomainType() + "}";
        // it doesn't contain user ID for SQL cache in DBMS
    }

    /**
     * Handle count of SQL execution in the request.
     * @param executeMeta The meta of action execute. (NotNull)
     */
    protected void handleSqlCount(final ActionExecuteMeta executeMeta) {
        final CallbackContext context = CallbackContext.getCallbackContextOnThread();
        if (context == null) {
            return;
        }
        final SqlStringFilter filter = context.getSqlStringFilter();
        if (filter == null || !(filter instanceof ExecutedSqlCounter)) {
            return;
        }
        final ExecutedSqlCounter counter = ((ExecutedSqlCounter) filter);
        final int limitCountOfSql = getLimitCountOfSql(executeMeta);
        if (limitCountOfSql >= 0 && counter.getTotalCountOfSql() > limitCountOfSql) {
            handleTooManySqlExecution(executeMeta, counter);
        }
        final String exp = counter.toLineDisp();
        requestManager.setAttribute(RequestManager.KEY_DBFLUTE_SQL_COUNT, exp); // logged by logging filter
    }

    /**
     * Handle too many SQL executions.
     * @param executeMeta The meta of action execute. (NotNull)
     * @param sqlCounter The counter object for SQL executions. (NotNull)
     */
    protected void handleTooManySqlExecution(final ActionExecuteMeta executeMeta, final ExecutedSqlCounter sqlCounter) {
        final String actionDisp = buildActionDisp(executeMeta);
        LOG.warn("*Too many SQL executions: " + sqlCounter.getTotalCountOfSql() + " in " + actionDisp);
    }

    protected String buildActionDisp(ActionExecuteMeta executeMeta) {
        final Method method = executeMeta.getActionMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.getSimpleName() + "." + method.getName() + "()";
    }

    /**
     * Get the limit count of SQL execution. <br>
     * You can override if you need.
     * @param executeMeta The meta of action execute. (NotNull)
     * @return The max count allowed for SQL executions. (MinusAllowed: if minus, no check)
     */
    protected int getLimitCountOfSql(ActionExecuteMeta executeMeta) {
        return 30; // as default
    }

    /**
     * Clear callback context. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     */
    protected void clearCallbackContext() {
        CallbackContext.clearSqlStringFilterOnThread();
        CallbackContext.clearSqlFireHookOnThread();
    }

    // ===================================================================================
    //                                                                      Login Handling
    //                                                                      ==============
    /**
     * Handle the login required check for the requested action.
     * @param executeMeta The meta of action execute to determine required action. (NotNull)
     * @return The forward path, basically for login-redirect. (NullAllowed)
     */
    protected String handleLoginRequiredCheck(ActionExecuteMeta executeMeta) {
        final LoginHandlingResource resource = createLogingHandlingResource(executeMeta);
        final String forwardTo = loginManager.checkLoginRequired(resource);
        if (forwardTo != null && executeMeta.isApiAction()) {
            return dispatchApiLoginRequiredFailure(executeMeta);
        }
        return forwardTo;
    }

    protected String dispatchApiLoginRequiredFailure(ActionExecuteMeta executeMeta) {
        final ActionMessages errors = getErrors(); // basically no errors
        final ApiResult result = apiManager.prepareLoginRequiredFailureResult(errors, executeMeta);
        apiManager.writeJsonResponse(result);
        return apiManager.forwardToApiResolvedDummy();
    }

    /**
     * Handle the perform-login redirect. (redirect to requested action if it needs)
     * @param executeMeta The meta of action execute to determine perform-login action. (NotNull)
     * @return The forward path, basically for login redirect. (NullAllowed)
     */
    protected String handleLoginPerformRedirect(ActionExecuteMeta executeMeta) {
        final LoginHandlingResource resource = createLogingHandlingResource(executeMeta);
        final String forwardTo = loginManager.redirectToRequestedActionIfNeeds(resource);
        if (forwardTo != null && executeMeta.isApiAction()) {
            return dispatchApiLoginPerformRedirect(executeMeta);
        }
        return forwardTo;
    }

    protected String dispatchApiLoginPerformRedirect(ActionExecuteMeta executeMeta) {
        final ActionMessages errors = getErrors(); // basically no errors
        final ApiResult result = apiManager.prepareLoginPerformRedirectResult(errors, executeMeta);
        apiManager.writeJsonResponse(result);
        return apiManager.forwardToApiResolvedDummy();
    }

    protected LoginHandlingResource createLogingHandlingResource(ActionExecuteMeta executeMeta) {
        final Class<? extends TypicalBaseAction> actionClass = getClass();
        final Method actionMethod = executeMeta.getActionMethod();
        final RuntimeException failureCause = executeMeta.getFailureCause();
        final ActionMessages validationErrors = executeMeta.getValidationErrors();
        return new LoginHandlingResource(actionClass, actionMethod, failureCause, validationErrors);
    }

    // ===================================================================================
    //                                                               Application Exception
    //                                                               =====================
    /**
     * Handle the application exception thrown by (basically) action execute. <br>
     * Though this is same as global-exceptions settings of Struts,
     * There is more flexibility than the function so you can set it here. <br>
     * This is called by callback process so you should NOT call this directly in your action.
     * @param executeMeta The meta of action execute. (NotNull)
     * @param cause The exception thrown by (basically) action execute, might be translated. (NotNull)
     * @return The forward path. (NullAllowed: if not null, it goes to the path)
     */
    protected String handleApplicationException(ActionExecuteMeta executeMeta, RuntimeException cause) { // called by callback
        final String forwardTo = doHandleApplicationException(executeMeta, cause);
        if (forwardTo != null && executeMeta.isApiAction()) {
            return dispatchApiApplicationException(executeMeta, cause);
        }
        return forwardTo;
    }

    protected String doHandleApplicationException(ActionExecuteMeta executeMeta, RuntimeException cause) {
        String forwardTo = null;
        if (cause instanceof ApplicationBaseException) {
            final ApplicationBaseException appEx = (ApplicationBaseException) cause;
            if (appEx instanceof LoginFailureException) {
                forwardTo = handleLoginFailureException((LoginFailureException) appEx);
            } else if (appEx instanceof LoginTimeoutException) {
                forwardTo = handleLoginTimeoutException((LoginTimeoutException) appEx);
            } else if (appEx instanceof GetParameterNotFoundException) {
                forwardTo = handleGetParameterNotFoundException((GetParameterNotFoundException) appEx);
            } else if (appEx instanceof MessageKeyApplicationException) {
                forwardTo = handleErrorsApplicationException((MessageKeyApplicationException) appEx);
            } else {
                forwardTo = handleSpecialApplicationException(appEx);
            }
            if (forwardTo == null) {
                forwardTo = handleUnknownApplicationException(appEx);
            }
            reflectEmbeddedApplicationMessagesIfExists(appEx); // override existing messages if exists
        } else {
            if (cause instanceof EntityAlreadyDeletedException) {
                forwardTo = handleEntityAlreadyDeletedException((EntityAlreadyDeletedException) cause);
            } else if (cause instanceof EntityAlreadyUpdatedException) {
                forwardTo = handleEntityAlreadyUpdatedException((EntityAlreadyUpdatedException) cause);
            } else if (cause instanceof EntityAlreadyExistsException) {
                forwardTo = handleEntityAlreadyExistsException((EntityAlreadyExistsException) cause);
            }
        }
        if (forwardTo != null) {
            showApplicationExceptionHandling(cause, forwardTo);
        }
        return forwardTo;
    }

    protected void showApplicationExceptionHandling(RuntimeException cause, String forwardTo) {
        if (LOG.isInfoEnabled()) {
            // not show forwardTo because of forwarding log later
            final StringBuilder sb = new StringBuilder();
            sb.append("...Handling application exception:");
            sb.append("\n_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
            sb.append("\n[Application Exception]");
            sb.append("\n").append(cause.getMessage());
            final ActionMessages errors = getErrors();
            if (errors != null) {
                sb.append("\n").append(errors.toString());
            }
            buildApplicationExceptionStackTrace(cause, sb);
            sb.append("\n_/_/_/_/_/_/_/_/_/_/");
            LOG.info(sb.toString());
        }
    }

    protected void buildApplicationExceptionStackTrace(RuntimeException cause, StringBuilder sb) {
        final StackTraceElement[] stackTrace = cause.getStackTrace();
        if (stackTrace == null) { // just in case
            return;
        }
        int index = 0;
        for (StackTraceElement element : stackTrace) {
            if (index > 10) { // not all because it's not error
                break;
            }
            final String className = element.getClassName();
            final String fileName = element.getFileName(); // might be null
            final int lineNumber = element.getLineNumber();
            final String methodName = element.getMethodName();
            sb.append("\n at ").append(className).append(".").append(methodName);
            sb.append("(").append(fileName);
            if (lineNumber >= 0) {
                sb.append(":").append(lineNumber);
            }
            sb.append(")");
            ++index;
        }
    }

    protected void reflectEmbeddedApplicationMessagesIfExists(ApplicationBaseException appEx) {
        final String errorsKey = appEx.getErrorKey();
        if (errorsKey != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("...Saving embedded application message as action error: " + errorsKey);
            }
            saveErrors(errorsKey, appEx.getErrorArgs());
        }
    }

    protected String dispatchApiApplicationException(ActionExecuteMeta executeMeta, RuntimeException cause) {
        final ActionMessages errors = getErrors(); // might be null in minor case
        final ApiResult result = apiManager.prepareApplicationExceptionResult(errors, executeMeta, cause);
        apiManager.writeJsonResponse(result);
        return apiManager.forwardToApiResolvedDummy();
    }

    // -----------------------------------------------------
    //                                        DBFlute Entity
    //                                        --------------
    protected String handleEntityAlreadyDeletedException(EntityAlreadyDeletedException cause) {
        saveErrors(getErrorsAppAlreadyDeletedKey());
        return getErrorMessageAlreadyDeletedJsp();
    }

    protected abstract String getErrorsAppAlreadyDeletedKey();

    protected String getErrorMessageAlreadyDeletedJsp() {
        return getErrorMessageJsp(); // as default
    }

    protected String handleEntityAlreadyUpdatedException(EntityAlreadyUpdatedException cause) {
        saveErrors(getErrorsAppAlreadyUpdatedKey());
        return getErrorMessageAlreadyUpdatedJsp();
    }

    protected abstract String getErrorsAppAlreadyUpdatedKey();

    protected String getErrorMessageAlreadyUpdatedJsp() {
        return getErrorMessageJsp(); // as default
    }

    protected String handleEntityAlreadyExistsException(EntityAlreadyExistsException cause) {
        saveErrors(getErrorsAppAlreadyExistsKey());
        return getErrorMessageAlreadyExistsJsp();
    }

    protected abstract String getErrorsAppAlreadyExistsKey();

    protected String getErrorMessageAlreadyExistsJsp() {
        return getErrorMessageJsp(); // as default
    }

    // -----------------------------------------------------
    //                                         Login Failure
    //                                         -------------
    protected String handleLoginFailureException(LoginFailureException appEx) {
        // basically no way because of already checked when validation
        saveErrors(getErrorsNotLoginKey());
        return loginManager.redirectToLoginAction();
    }

    protected abstract String getErrorsNotLoginKey();

    // -----------------------------------------------------
    //                                         Login Timeout
    //                                         -------------
    protected String handleLoginTimeoutException(LoginTimeoutException appEx) {
        return loginManager.redirectToLoginAction(); // no message because of rare case
    }

    // -----------------------------------------------------
    //                                         Get Parameter
    //                                         -------------
    protected String handleGetParameterNotFoundException(GetParameterNotFoundException appEx) {
        saveErrors(getErrorsAppIllegalTransitionKey());
        return getErrorMessageJsp();
    }

    protected abstract String getErrorsAppIllegalTransitionKey();

    protected abstract String getErrorMessageJsp();

    // -----------------------------------------------------
    //                                           Message Key
    //                                           -----------
    protected String handleErrorsApplicationException(MessageKeyApplicationException appEx) {
        // no save here because of saved later
        //saveErrors(appEx.getErrors());
        return getErrorMessageJsp();
    }

    // -----------------------------------------------------
    //                                               Special
    //                                               -------
    protected String handleSpecialApplicationException(ApplicationBaseException appEx) { // you can override
        return null;
    }

    // -----------------------------------------------------
    //                                               Unknown
    //                                               -------
    protected String handleUnknownApplicationException(ApplicationBaseException appEx) {
        return loginManager.redirectToLoginAction(); // basically no way
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    /**
     * Assert the condition is true or it throws illegal transition forcedly. <br>
     * You can use this in your action process against strange request parameters.
     * @param condition Your determination, true or false. (false: illegal transition)
     */
    protected void assertTrueOrForcedIllegalTransition(boolean condition) {
        if (!condition) {
            throw new ForcedIllegalTransitionApplicationException(getErrorsAppIllegalTransitionKey());
        }
    }

    /**
     * Assert the condition is true or it throws 404 not found forcedly. <br>
     * You can use this in your action process against invalid URL parameters.
     * @param condition Your determination, true or false. (false: 404 not found)
     */
    protected void assertTrueOrForcedRequest404NotFound(boolean condition) {
        if (!condition) {
            String msg = "from Forced 404 NotFound assertion"; // debug message
            throw new ForcedRequest404NotFoundException(msg);
        }
    }
}
