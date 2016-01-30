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
package org.dbflute.saflute.web.action.processor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.dbflute.saflute.core.json.JsonManager;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.api.ApiManager;
import org.dbflute.saflute.web.action.api.ApiResult;
import org.dbflute.saflute.web.action.callback.ActionCallback;
import org.dbflute.saflute.web.action.callback.ActionExecuteMeta;
import org.dbflute.saflute.web.action.response.ActionResponse;
import org.dbflute.saflute.web.action.response.ActionResponseHandler;
import org.dbflute.saflute.web.action.response.ApiResponse;
import org.dbflute.saflute.web.action.response.JsonResponse;
import org.dbflute.saflute.web.action.response.StreamResponse;
import org.dbflute.saflute.web.action.response.XmlResponse;
import org.dbflute.saflute.web.servlet.filter.RequestLoggingFilter;
import org.dbflute.saflute.web.servlet.filter.RequestLoggingFilter.Request500Handler;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.request.ResponseDownloadResource;
import org.dbflute.saflute.web.servlet.request.ResponseManager;
import org.dbflute.saflute.web.servlet.session.SessionManager;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.util.MethodUtil;
import org.seasar.struts.action.ActionWrapper;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.config.S2ValidationConfig;
import org.seasar.struts.enums.SaveType;

/**
 * The wrapper of action, which can be God hand. <br>
 * This class is new-created per request.
 * @author jflute
 */
public class GodHandableActionWrapper extends ActionWrapper {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(GodHandableActionWrapper.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The handler of action response, that provides your original handling. (NullAllowed: it's option) */
    protected ActionResponseHandler responseHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public GodHandableActionWrapper(S2ActionMapping actionMapping) {
        super(actionMapping);
    }

    // ===================================================================================
    //                                                                    Callback Process
    //                                                                    ================
    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final ActionForward forward = super.execute(mapping, form, request, response);
        if (forward != null && isResolvedResponse(forward)) {
            return null; // response already finished so no forward
        }
        return forward;
    }

    protected boolean isResolvedResponse(final ActionForward forward) {
        final String path = forward.getPath(); // null check just in case
        return isApiResolvedDummyForward(path) || isResponseResolvedDummyForward(path);
    }

    protected boolean isApiResolvedDummyForward(final String path) {
        return path != null && path.endsWith(ApiManager.API_RESOLVED_DUMMY_FORWARD); // ends with just in case
    }

    protected boolean isResponseResolvedDummyForward(final String path) {
        return path != null && path.endsWith(ActionCallback.RESPONSE_RESOLVED_DUMMY_FORWARD); // ends with just in case
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ActionForward execute(HttpServletRequest request, S2ExecuteConfig config) {
        ActionCallback callback = null;
        if (action instanceof ActionCallback) {
            callback = (ActionCallback) action;
        }
        final ActionExecuteMeta meta = new ActionExecuteMeta(config);
        prepareRequest500Handling(meta);
        processLocale(meta);
        if (LOG.isDebugEnabled() && callback != null) {
            LOG.debug("#flow ...Calling back #before for " + buildActionName(meta));
        }
        try {
            final ActionForward beforeForward = processCallbackBefore(request, config, callback, meta);
            if (beforeForward != null) {
                return handleResponseResolvedPossible(beforeForward);
            }
        } catch (RuntimeException e) {
            return tellExceptionMonologue(request, config, callback, meta, e);
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("#flow ...Beginning #action " + buildActionDisp(meta));
            }
            final ActionForward forward = doActuallyExecute(request, config, callback, meta);
            meta.setExecuteForward(forward);
            final ActionForward successForward = processCallbackOnSuccess(request, config, callback, meta);
            if (successForward != null) {
                meta.setExecuteForward(successForward);
                return handleResponseResolvedPossible(successForward);
            }
            return forward;
        } catch (RuntimeException e) {
            return tellExceptionMonologue(request, config, callback, meta, e);
        } finally {
            if (LOG.isDebugEnabled() && callback != null) {
                final String failureMark = meta.hasFailureCause() ? " with failure" : "";
                LOG.debug("#flow ...Calling back #finally" + failureMark + " for " + buildActionName(meta));
            }
            processCallbackFinally(callback, meta);
            if (LOG.isDebugEnabled()) {
                final ActionForward forward = meta.getExecuteForward();
                if (forward != null) {
                    final String ing = forward.getRedirect() ? "Redirecting" : "Forwarding";
                    final String path = forward.getPath(); // basically not null but just in case
                    final String tag = path != null && path.endsWith(".jsp") ? "#jsp " : "";
                    LOG.debug("#flow ..." + ing + " to " + tag + path);
                }
            }
        }
    }

    protected void prepareRequest500Handling(final ActionExecuteMeta meta) {
        RequestLoggingFilter.setRequest500HandlerOnThread(new Request500Handler() {
            public void handle(HttpServletRequest request, HttpServletResponse response, Throwable cause) {
                dispatchApiSystemException(meta, request, response, cause);
            }
        });
    }

    protected void dispatchApiSystemException(ActionExecuteMeta meta, HttpServletRequest request, HttpServletResponse response,
            Throwable cause) {
        if (meta.isApiAction() && !response.isCommitted()) {
            final ApiManager apiManager = getApiManager();
            final ApiResult result = apiManager.prepareSystemExceptionResult(response, meta, cause);
            if (result != null) { // because of not required
                apiManager.writeJsonResponse(result);
            }
        }
    }

    protected void processLocale(ActionExecuteMeta meta) { // moved from request processor
        // though basically same as Struts (saving locale to session),
        // you can customize the process e.g. accept cookie locale
        final RequestManager manager = getRequestManager();
        manager.resolveUserLocale(meta);
        manager.resolveUserTimeZone(meta);
    }

    protected String buildActionDisp(ActionExecuteMeta meta) {
        final Method method = meta.getActionMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.getSimpleName() + "." + method.getName() + "()";
    }

    protected String buildActionName(ActionExecuteMeta meta) {
        final Method method = meta.getActionMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.getSimpleName();
    }

    protected ActionForward handleResponseResolvedPossible(ActionForward forward) {
        if (isResolvedResponse(forward)) { // forward needs not-null here
            return null; // response might be resolved
        }
        return forward;
    }

    // -----------------------------------------------------
    //                                                Before
    //                                                ------
    protected ActionForward processCallbackBefore(HttpServletRequest request, S2ExecuteConfig executeConfig, ActionCallback callback,
            ActionExecuteMeta meta) {
        if (callback == null) {
            return null;
        }
        ActionForward forwardTo = godHandActionPrologue(request, executeConfig, callback, meta);
        if (forwardTo != null) {
            return forwardTo;
        }
        forwardTo = godHandBefore(request, executeConfig, callback, meta);
        if (forwardTo != null) {
            return forwardTo;
        }
        return callbackBefore(request, executeConfig, callback, meta);
    }

    protected ActionForward godHandActionPrologue(HttpServletRequest request, S2ExecuteConfig executeConfig, ActionCallback callback,
            ActionExecuteMeta executeMeta) {
        final String prologuePath = callback.godHandActionPrologue(executeMeta);
        if (prologuePath != null) {
            return createForward(request, executeConfig, prologuePath);
        }
        return null;
    }

    protected ActionForward godHandBefore(HttpServletRequest request, S2ExecuteConfig executeConfig, ActionCallback callback,
            ActionExecuteMeta executeMeta) {
        final String beforePath = callback.godHandBefore(executeMeta);
        if (beforePath != null) {
            return createForward(request, executeConfig, beforePath);
        }
        return null;
    }

    protected ActionForward callbackBefore(HttpServletRequest request, S2ExecuteConfig executeConfig, ActionCallback callback,
            ActionExecuteMeta executeMeta) {
        final String beforePath = callback.callbackBefore(executeMeta);
        if (beforePath != null) {
            return createForward(request, executeConfig, beforePath);
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            on Success
    //                                            ----------
    protected ActionForward processCallbackOnSuccess(HttpServletRequest request, S2ExecuteConfig config, ActionCallback callback,
            ActionExecuteMeta meta) {
        if (callback == null) {
            return null;
        }
        // only monologue for now
        return godHandSuccessMonologue(request, config, callback, meta);
    }

    protected ActionForward godHandSuccessMonologue(HttpServletRequest request, S2ExecuteConfig config, ActionCallback callback,
            ActionExecuteMeta meta) {
        final String monologuePath = callback.godHandSuccessMonologue(meta);
        if (monologuePath != null) {
            return createForward(request, config, monologuePath);
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            on Failure
    //                                            ----------
    protected ActionForward tellExceptionMonologue(HttpServletRequest request, S2ExecuteConfig config, ActionCallback callback,
            final ActionExecuteMeta meta, RuntimeException e) {
        meta.setFailureCause(e);
        final ActionForward failureForward = processCallbackOnFailure(request, config, callback, meta);
        if (failureForward != null) {
            meta.setExecuteForward(failureForward);
            return handleResponseResolvedPossible(failureForward);
        }
        throw e;
    }

    protected ActionForward processCallbackOnFailure(HttpServletRequest request, S2ExecuteConfig config, ActionCallback callback,
            ActionExecuteMeta meta) {
        if (callback == null) {
            return null;
        }
        // only monologue for now
        return godHandExceptionMonologue(request, config, callback, meta);
    }

    protected ActionForward godHandExceptionMonologue(HttpServletRequest request, S2ExecuteConfig config, ActionCallback callback,
            ActionExecuteMeta meta) {
        final String monologuePath = callback.godHandExceptionMonologue(meta);
        if (monologuePath != null) {
            return createForward(request, config, monologuePath);
        }
        return null;
    }

    // -----------------------------------------------------
    //                                               Finally
    //                                               -------
    protected void processCallbackFinally(ActionCallback callback, ActionExecuteMeta meta) {
        if (callback == null) {
            return;
        }
        try {
            callback.callbackFinally(meta);
        } finally {
            try {
                callback.godHandFinally(meta);
            } finally {
                callback.godHandActionEpilogue(meta);
            }
        }
    }

    // ===================================================================================
    //                                                                    Actually Execute
    //                                                                    ================
    protected ActionForward doActuallyExecute(HttpServletRequest request, S2ExecuteConfig config, ActionCallback callback,
            ActionExecuteMeta meta) {
        final List<S2ValidationConfig> validationConfigs = config.getValidationConfigs();
        if (validationConfigs != null) {
            final ActionMessages errors = new ActionMessages();
            for (S2ValidationConfig cfg : validationConfigs) {
                if (cfg.isValidator()) {
                    final ActionMessages byValidatorErrors = validateUsingValidator(request, config);
                    if (byValidatorErrors != null && !byValidatorErrors.isEmpty()) {
                        errors.add(byValidatorErrors);
                        if (config.isStopOnValidationError()) {
                            return handleErrors(errors, request, config, meta);
                        }
                    }
                } else {
                    Object target = actionForm;
                    final Class<?> componentClass = actionMapping.getComponentDef().getComponentClass();
                    if (cfg.getValidateMethod().getDeclaringClass().isAssignableFrom(componentClass)) {
                        target = action;
                    }
                    final ActionMessages byMethodErrors = invokeValidationMethod(cfg, target);
                    if (byMethodErrors != null && !byMethodErrors.isEmpty()) {
                        errors.add(byMethodErrors);
                        if (config.isStopOnValidationError()) {
                            return handleErrors(errors, request, config, meta);
                        }
                    }
                }
            }
            if (!errors.isEmpty()) {
                return handleErrors(errors, request, config, meta);
            }
            final String validationSuccess = callback.hookValidationSuccessActually();
            if (validationSuccess != null) {
                return createForward(request, config, validationSuccess);
            }
        }
        final String beforeAction = callback.hookBetweenValidationAndAction();
        if (beforeAction != null) {
            return createForward(request, config, beforeAction);
        }
        final String next = invokeActionExecute(config);
        removeActionFormIfNeeds(request, config);
        return createForward(request, config, next);
    }

    // -----------------------------------------------------
    //                                   Validation Handling
    //                                   -------------------
    protected ActionMessages invokeValidationMethod(S2ValidationConfig cfg, Object target) {
        final Method method = cfg.getValidateMethod();
        if (LOG.isDebugEnabled()) {
            final String className = method.getDeclaringClass().getSimpleName();
            LOG.debug("...Calling validation: " + className + "." + method.getName() + "()");
        }
        return (ActionMessages) MethodUtil.invoke(method, target, null);
    }

    protected ActionForward handleErrors(ActionMessages errors, HttpServletRequest request, S2ExecuteConfig config, ActionExecuteMeta meta) {
        if (meta != null) {
            meta.setValidationErrors(errors); // keep to determine it for e.g. login-redirect
        }
        return doHandleErrors(errors, request, config, meta);
    }

    protected ActionForward doHandleErrors(ActionMessages errors, HttpServletRequest request, S2ExecuteConfig executeConfig,
            ActionExecuteMeta meta) {
        if (executeConfig.getSaveErrors() == SaveType.REQUEST) {
            final RequestManager requestManager = getRequestManager();
            requestManager.saveErrors(errors);
        } else {
            final SessionManager sessionManager = getSessionManager();
            sessionManager.saveErrors(errors);
        }
        if (meta.isApiAction()) {
            return dispatchApiValidationError(errors, meta);
        }
        return actionMapping.createForward(executeConfig.resolveInput(actionMapping));
    }

    protected ActionForward dispatchApiValidationError(ActionMessages errors, ActionExecuteMeta meta) {
        final ApiManager apiManager = getApiManager();
        final ApiResult result = apiManager.prepareValidationErrorResult(errors, meta);
        apiManager.writeJsonResponse(result);
        return null;
    }

    // -----------------------------------------------------
    //                                       Action Handling
    //                                       ---------------
    protected String invokeActionExecute(S2ExecuteConfig executeConfig) {
        final Method executeMethod = executeConfig.getMethod();
        final Object result = MethodUtil.invoke(executeMethod, action, null);
        if (result instanceof ActionResponse) {
            if (responseHandler != null) {
                final String handled = responseHandler.handle((ActionResponse) result);
                if (handled != null) {
                    return isApiResolvedDummyForward(handled) ? null : handled;
                }
            }
            // this needs original action customizer in your customizer.dicon
            if (result instanceof JsonResponse) {
                return handleJsonResponse((JsonResponse) result, executeConfig);
            } else if (result instanceof XmlResponse) {
                return handleXmlResponse((XmlResponse) result, executeConfig);
            } else if (result instanceof StreamResponse) {
                return handleStreamResponse((StreamResponse) result, executeConfig);
            } else {
                final Class<?> type = result.getClass();
                String msg = "Unknown action response type: " + type + ", " + result;
                throw new IllegalStateException(msg);
            }
        } else { // normally here
            return (String) result;
        }
    }

    protected String handleJsonResponse(JsonResponse jsonResponse, S2ExecuteConfig executeConfig) {
        if (jsonResponse.isSkipResponse()) {
            return null;
        }
        // this needs original action customizer in your customizer.dicon
        final JsonManager jsonManager = getJsonManager();
        final String json = jsonManager.convertToJson(jsonResponse.getJsonObj());
        final ResponseManager responseManager = getResponseManager();
        setupApiResponseHeader(responseManager, jsonResponse);
        final String callback = jsonResponse.getCallback();
        if (callback != null) { // JSONP (needs JavaScript)
            final String script = callback + "(" + json + ")";
            responseManager.writeAsJavaScript(script);
        } else {
            // responseManager might have debug logging so no logging here
            if (jsonResponse.isForcedlyJavaScript()) {
                responseManager.writeAsJavaScript(json);
            } else { // as JSON (default)
                responseManager.writeAsJson(json);
            }
        }
        return null;
    }

    protected String handleXmlResponse(XmlResponse xmlResponse, S2ExecuteConfig executeConfig) {
        if (xmlResponse.isSkipResponse()) {
            return null;
        }
        final String xmlStr = xmlResponse.getXmlStr();
        final String encoding = xmlResponse.getEncoding();
        final ResponseManager responseManager = getResponseManager();
        setupApiResponseHeader(responseManager, xmlResponse);
        responseManager.writeAsXml(xmlStr, encoding);
        return null;
    }

    protected void setupApiResponseHeader(ResponseManager responseManager, ApiResponse apiResponse) {
        final Map<String, String> headerMap = apiResponse.getHeaderMap();
        if (!headerMap.isEmpty()) {
            final HttpServletResponse response = responseManager.getResponse();
            for (Entry<String, String> entry : headerMap.entrySet()) {
                response.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    protected String handleStreamResponse(StreamResponse streamResponse, S2ExecuteConfig executeConfig) {
        if (streamResponse.isSkipResponse()) {
            return null;
        }
        final ResponseDownloadResource resource = streamResponse.toDownloadResource();
        final ResponseManager responseManager = getResponseManager();
        responseManager.download(resource);
        return null;
    }

    protected void removeActionFormIfNeeds(HttpServletRequest request, S2ExecuteConfig executeConfig) {
        if (executeConfig.isRemoveActionForm() && !hasRequestErrors()) {
            final ComponentDef componentDef = actionMapping.getActionFormComponentDef();
            final String componentName = componentDef.getComponentName();
            if (componentDef.getInstanceDef().equals(InstanceDefFactory.SESSION)) {
                getSessionManager().remove(componentName);
            } else {
                getRequestManager().remove(componentName);
            }
            getRequestManager().remove(actionMapping.getAttribute());
        }
    }

    protected ActionForward createForward(HttpServletRequest request, S2ExecuteConfig executeConfig, String nextPath) {
        final boolean redirect = executeConfig.isRedirect() && !hasRequestErrors();
        return actionMapping.createForward(nextPath, redirect);
    }

    protected boolean hasRequestErrors() {
        return getRequestManager().hasErrors();
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected RequestManager getRequestManager() {
        return getComponent(RequestManager.class);
    }

    protected ResponseManager getResponseManager() {
        return getComponent(ResponseManager.class);
    }

    protected SessionManager getSessionManager() {
        return getComponent(SessionManager.class);
    }

    protected ApiManager getApiManager() {
        return getComponent(ApiManager.class);
    }

    protected JsonManager getJsonManager() {
        return getComponent(JsonManager.class);
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return ContainerUtil.getComponent(type);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setActionResopnseHandler(ActionResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }
}
