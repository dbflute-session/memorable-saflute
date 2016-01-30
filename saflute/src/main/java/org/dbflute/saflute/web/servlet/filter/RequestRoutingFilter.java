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
package org.dbflute.saflute.web.servlet.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.ActionPathHandler;
import org.dbflute.saflute.web.action.ActionResolver;
import org.dbflute.saflute.web.action.OptionalActionDirection;
import org.dbflute.saflute.web.action.processor.ActionAdjustmentProvider;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.filter.RoutingFilter;
import org.seasar.struts.util.S2ExecuteConfigUtil;

/**
 * @author jflute
 */
public class RequestRoutingFilter extends RoutingFilter {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(RequestRoutingFilter.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /**
     * The cache of assistant director, which can be lazy-loaded when you get it.
     * Don't use these variables directly, you should use the getter.
     * (NotNull: after lazy-load)
     * */
    protected FwAssistantDirector cachedAssistantDirector;

    // ===================================================================================
    //                                                                              Filter
    //                                                                              ======
    @Override
    public void doFilter(ServletRequest servReq, ServletResponse servRes, FilterChain chain) throws IOException,
            ServletException {
        final HttpServletRequest httpReq = (HttpServletRequest) servReq;
        final HttpServletResponse httpRes = (HttpServletResponse) servRes;
        final String requestPath = extractActionRequestPath(httpReq);
        if (!processDirectAccess(httpReq, httpRes, chain, requestPath)) {
            return;
        }
        if (!isRoutingTarget(httpReq, requestPath)) { // e.g. foo.jsp, foo.do, foo.js, foo.css
            chain.doFilter(httpReq, httpRes);
            return;
        }
        // no extension here (may be SAStruts URL)
        final ActionResolver resolver = ContainerUtil.getComponent(ActionResolver.class);
        try {
            final String contextPath = extractContextPath(httpReq);
            final ActionPathHandler handler = createActionPathHandler(httpReq, httpRes, contextPath);
            if (resolver.handleActionPath(requestPath, handler)) {
                return;
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof ServletException) {
                throw (ServletException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else { // no way
                throw new IllegalStateException(e);
            }
        }
        // no routing here
        showExpectedRouting(requestPath, resolver);
        chain.doFilter(servReq, servRes);
    }

    protected String extractContextPath(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        if (contextPath.equals("/")) {
            contextPath = "";
        }
        return contextPath;
    }

    protected boolean isRoutingTarget(HttpServletRequest request, String requestPath) {
        final ActionAdjustmentProvider adjustmentProvider = assistActionAdjustmentProvider();
        if (adjustmentProvider.isForcedRoutingTarget(request, requestPath)) { // you can adjust it
            return true;
        }
        return !isExtensionUrlPossible(request, requestPath); // default determination
    }

    protected ActionAdjustmentProvider assistActionAdjustmentProvider() {
        final OptionalActionDirection direction = getAssistantDirector().assistOptionalActionDirection();
        return direction.assistActionAdjustmentProvider();
    }

    protected boolean isExtensionUrlPossible(HttpServletRequest request, String requestPath) {
        // *added condition 'endsWith()' to allow /member/1.2.3/
        // (you can receive 'urlPattern' that contains dot '.')
        //
        // true  : e.g. foo.jsp, foo.do, foo.js, foo.css, /member/1.2.3
        // false : e.g. /member/list/, /member/list, /member/1.2.3/
        return requestPath.indexOf('.') >= 0 && !requestPath.endsWith("/");
    }

    protected ActionPathHandler createActionPathHandler(final HttpServletRequest httpReq,
            final HttpServletResponse httpRes, final String contextPath) {
        return new ActionPathHandler() {
            public boolean handleActionPath(String requestPath, String actionPath, String paramPath,
                    S2ExecuteConfig configByParam) throws Exception {
                return forwardToStruts(httpReq, httpRes, contextPath, requestPath, actionPath, paramPath, configByParam);
            }
        };
    }

    protected void showExpectedRouting(String requestPath, ActionResolver resolver) { // for debug
        if (LOG.isDebugEnabled()) {
            if (!requestPath.contains(".")) { // e.g. routing target can be adjusted so may be .jpg
                LOG.debug(resolver.prepareExpectedRoutingMessage(requestPath));
            }
        }
    }

    // ===================================================================================
    //                                                                           To Struts
    //                                                                           =========
    protected boolean forwardToStruts(HttpServletRequest httpReq, HttpServletResponse httpRes, String contextPath,
            String requestPath, String actionPath, String paramPath, S2ExecuteConfig executeConfig) throws IOException,
            ServletException {
        if (executeConfig == null) {
            if (needsSlashRedirect(httpReq, requestPath, executeConfig)) {
                redirectWithSlash(httpReq, httpRes, contextPath, requestPath);
                return true;
            } else if (S2ExecuteConfigUtil.findExecuteConfig(actionPath, httpReq) != null) {
                forward(httpReq, httpRes, actionPath, null, null);
                return true;
            }
        } else {
            forward(httpReq, httpRes, actionPath, paramPath, executeConfig);
            return true;
        }
        // possible? (unknown but it keeps the way of SAStruts RoutingFilter here)
        return false;
    }

    protected boolean needsSlashRedirect(HttpServletRequest request, String requestPath, S2ExecuteConfig executeConfig) {
        if (isForcedSuppressRedirectWithSlash(request, requestPath, executeConfig)) {
            return false;
        }
        return "GET".equalsIgnoreCase(request.getMethod()) && !requestPath.endsWith("/"); // default determination
    }

    protected boolean isForcedSuppressRedirectWithSlash(HttpServletRequest request, String requestPath,
            S2ExecuteConfig executeConfig) {
        final ActionAdjustmentProvider adjustmentProvider = assistActionAdjustmentProvider();
        return adjustmentProvider.isForcedSuppressRedirectWithSlash(request, requestPath, executeConfig);
    }

    protected void redirectWithSlash(HttpServletRequest httpReq, HttpServletResponse httpRes, String contextPath,
            String requestPath) throws IOException {
        String queryString = "";
        if (httpReq.getQueryString() != null) {
            queryString = "?" + httpReq.getQueryString();
        }
        final String redirectUrl = contextPath + requestPath + "/" + queryString;
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Redirecting (with slash) to: " + redirectUrl);
        }
        httpRes.sendRedirect(redirectUrl);
    }

    @Override
    protected void forward(HttpServletRequest request, HttpServletResponse response, String actionPath,
            String paramPath, S2ExecuteConfig executeConfig) throws IOException, ServletException {
        saveRequestInfo(request, response);
        String forwardPath = actionPath + ".do";
        if (executeConfig != null) {
            // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // paramPath is encoded here in spite of no decoded
            // (needs to keep escape characters in Framework to suppress malfunction)
            // however next request decodes them when request.getParameterValues()
            // and URL parameters are decoded when population for action form
            //
            // e.g. /foo/aaa%252fbbb%2fccc/ is requested
            // routing to -> /foo.do&bar=aaa%25252fbbb%252fccc
            // request.getParameterValues("bar") -> aaa%252fbbb%2fccc
            // set value to form -> aaa%2fbbb/ccc *only one-time decoding
            // - - - - - - - - - -/
            forwardPath = forwardPath + executeConfig.getQueryString(paramPath);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("...Routing to: " + forwardPath);
        }
        request.getRequestDispatcher(forwardPath).forward(request, response);
    }

    protected void saveRequestInfo(HttpServletRequest request, HttpServletResponse response) {
        final String path = buildRoutingOriginRequestPath(request);
        request.setAttribute(RequestManager.KEY_ROUTING_ORIGIN_REQUEST_PATH, path);
        final String pathAndQuery = buildRoutingOriginRequestPathAndQuery(request);
        request.setAttribute(RequestManager.KEY_ROUTING_ORIGIN_REQUEST_PATH_AND_QUERY, pathAndQuery);
    }

    protected String buildRoutingOriginRequestPath(HttpServletRequest request) {
        return extractActionRequestPath(request);
    }

    protected String buildRoutingOriginRequestPathAndQuery(HttpServletRequest request) {
        final String requestPath = buildRoutingOriginRequestPath(request);
        return doBuildRoutingOriginRequestPathAndQuery(request, requestPath);
    }

    protected String doBuildRoutingOriginRequestPathAndQuery(HttpServletRequest request, String basePath) {
        final String queryString = request.getQueryString();
        final StringBuilder sb = new StringBuilder();
        sb.append(basePath);
        if (queryString != null && queryString.trim().length() > 0) {
            sb.append("?").append(queryString);
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                        Request Path
    //                                                                        ============
    protected String extractActionRequestPath(HttpServletRequest request) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
        // request specification:
        //   requestURI  : /dockside/member/list/foo%2fbar/
        //   servletPath : /member/list/foo/bar/
        //
        // so uses requestURI but it needs to remove context path
        //  -> /member/list/foo%2fbar/
        // = = = = = = = = = =/
        final RequestManager requestManager = ContainerUtil.getComponent(RequestManager.class);
        return requestManager.getRequestPath();
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected FwAssistantDirector getAssistantDirector() {
        if (cachedAssistantDirector != null) {
            return cachedAssistantDirector;
        }
        synchronized (this) {
            if (cachedAssistantDirector != null) {
                return cachedAssistantDirector;
            }
            cachedAssistantDirector = ContainerUtil.getComponent(FwAssistantDirector.class);
        }
        return cachedAssistantDirector;
    }
}
