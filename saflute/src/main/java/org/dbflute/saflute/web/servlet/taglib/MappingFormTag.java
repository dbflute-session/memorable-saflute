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
package org.dbflute.saflute.web.servlet.taglib;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.taglib.TagUtils;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.ActionPathHandler;
import org.dbflute.saflute.web.action.ActionResolver;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.taglib.exception.FormActionNotFoundException;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.taglib.S2FormTag;
import org.seasar.struts.util.RequestUtil;
import org.seasar.struts.util.RoutingUtil;

/**
 * @author jflute
 */
public class MappingFormTag extends S2FormTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(MappingFormTag.class);

    // ===================================================================================
    //                                                                             Look Up
    //                                                                             =======
    @Override
    protected void lookup() throws JspException {
        setupModuleConfig();
        setupServlet();
        setupActionForNow();

        final int paramMarkIndex = action.indexOf('?');
        final String path = paramMarkIndex >= 0 ? action.substring(0, paramMarkIndex) : action;
        final String queryString = paramMarkIndex >= 0 ? action.substring(paramMarkIndex) : "";

        final ActionResolver resolver = getActionResolver();
        try {
            final ActionPathHandler handler = createActionPathHandler(path, queryString);
            final boolean handled = resolver.handleActionPath(path, handler);
            if (!handled) {
                throwFormActionNotFoundException(path, queryString);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof JspException) {
                throw (JspException) e;
            } else {
                String msg = "Failed to handle action path: " + path;
                throw new IllegalStateException(msg, e);
            }
        }
        checkActionMappingExistence();
        setupBeanInfo();
    }

    protected void setupModuleConfig() throws JspException {
        moduleConfig = TagUtils.getInstance().getModuleConfig(pageContext);
        if (moduleConfig == null) {
            JspException e = new JspException(messages.getMessage("formTag.collections"));
            pageContext.setAttribute(Globals.EXCEPTION_KEY, e, PageContext.REQUEST_SCOPE);
            throw e;
        }
    }

    protected void setupServlet() {
        servlet = (ActionServlet) pageContext.getServletContext().getAttribute(Globals.ACTION_SERVLET_KEY);
    }

    protected void setupActionForNow() {
        if (isInternalDebug()) {
            debugInternally("...Setting up action for now");
            debugInternally("  requestPath    = " + RequestUtil.getPath());
            debugInternally("  action (first) = " + action);
        }
        if (action == null) {
            action = calculateActionPath();
        } else if (!action.startsWith("/")) {
            action = calculateActionPath() + action;
        }
        if (isInternalDebug()) {
            debugInternally("  action (calc)  = " + action);
        }
    }

    protected ActionPathHandler createActionPathHandler(final String path, final String queryString) {
        return new ActionPathHandler() {
            public boolean handleActionPath(String requestPath, String actionPath, String paramPath,
                    S2ExecuteConfig configByParam) throws IOException, ServletException {
                return processActionMapping(path, queryString, actionPath, paramPath, configByParam);
            }
        };
    }

    protected void throwFormActionNotFoundException(String path, String queryString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the action for the action path of form tag.");
        br.addItem("Action Path");
        br.addElement(path);
        br.addItem("Query String");
        br.addElement(queryString);
        final String msg = br.buildExceptionMessage();
        throw new FormActionNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                         Action Path
    //                                                                         ===========
    protected String calculateActionPath() {
        // cannot use this because URL parameter is contained in the path e.g. /member/edit/1001/
        // you should remove the parameter '1001' by additional logic when you use this
        //final String routingOriginRequestPathAndQuery = getRoutingOriginRequestPathAndQuery();
        //if (routingOriginRequestPathAndQuery != null) { // first priority
        //  return routingOriginRequestPathAndQuery;
        //}
        final String requestPath = getRequestPath();
        return calculateActionPathByJspPath(requestPath);
    }

    protected String calculateActionPathByJspPath(String requestPath) {
        final ActionResolver resolver = getActionResolver();
        return resolver.calculateActionPathByJspPath(requestPath);
    }

    // ===================================================================================
    //                                                                      Action Mapping
    //                                                                      ==============
    protected boolean processActionMapping(String path, String queryString, String actionPath, String paramPath,
            S2ExecuteConfig configByParam) {
        if (isInternalDebug()) {
            debugInternally("...Processing action mapping");
        }
        final S2ActionMapping s2mapping = (S2ActionMapping) moduleConfig.findActionConfig(actionPath);
        if (isInternalDebug()) {
            debugInternally("  actionPath  = " + actionPath);
            debugInternally("  paramPath   = " + paramPath);
            debugInternally("  queryString = " + queryString);
            debugInternally("  mapping     = " + s2mapping.getPath());
        }
        if (configByParam == null) {
            mapping = s2mapping;

            // not use actionPath because the path may have prefix
            // instead, specified or calculated path (by request) is set here
            // the path will be resolved at routing filter
            action = appendSlashRearIfNeeds(path) + appendQuestionFrontIfNeeds(queryString);

            if (isInternalDebug()) {
                debugInternally(" -> processed (empty paramPath): action=" + action);
            }
        } else {
            mapping = s2mapping;
            if (isInternalDebug()) {
                debugInternally(" -> processed (execute config): action=" + action);
            }
        }
        return true;
    }

    protected String appendSlashRearIfNeeds(String str) {
        return str + (!str.endsWith("/") ? "/" : "");
    }

    protected String appendQuestionFrontIfNeeds(String str) {
        return (!str.equals("") ? "?" : "") + str;
    }

    protected interface ActionPathBuilder {
        String build(String[] names, int index, String queryString, S2ActionMapping s2mapping);
    }

    protected ActionPathBuilder createDirectActionPathBuilder() {
        return new ActionPathBuilder() {
            public String build(String[] names, int index, String queryString, S2ActionMapping s2mapping) {
                return s2mapping.getPath() + "/" + queryString;
            }
        };
    }

    protected ActionPathBuilder createIndexActionPathBuilder() {
        return new ActionPathBuilder() {
            public String build(String[] names, int index, String queryString, S2ActionMapping s2mapping) {
                return RoutingUtil.getActionPath(names, index - 1) + queryString;
            }
        };
    }

    protected void checkActionMappingExistence() throws JspException {
        if (mapping == null) {
            JspException e = new JspException(messages.getMessage("formTag.mapping", action));
            pageContext.setAttribute(Globals.EXCEPTION_KEY, e, PageContext.REQUEST_SCOPE);
            throw e;
        }
    }

    // ===================================================================================
    //                                                                           Bean Info
    //                                                                           =========
    protected void setupBeanInfo() throws JspException {
        FormBeanConfig formBeanConfig = moduleConfig.findFormBeanConfig(mapping.getName());
        if (formBeanConfig == null) {
            JspException e = new JspException(messages.getMessage("formTag.formBean", mapping.getName(), action));
            pageContext.setAttribute(Globals.EXCEPTION_KEY, e, PageContext.REQUEST_SCOPE);
            throw e;
        }
        beanName = mapping.getAttribute();
        beanScope = mapping.getScope();
        beanType = formBeanConfig.getType();
    }

    // ===================================================================================
    //                                                                      Internal Debug
    //                                                                      ==============
    protected boolean isInternalDebug() {
        return LOG.isDebugEnabled() && isFrameworkDebugEnabled();
    }

    protected void debugInternally(String msg) {
        LOG.debug(msg);
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected FwAssistantDirector getAssistantDirector() {
        return ContainerUtil.getComponent(FwAssistantDirector.class);
    }

    protected RequestManager getRequestManager() {
        return ContainerUtil.getComponent(RequestManager.class);
    }

    protected ActionResolver getActionResolver() {
        return ContainerUtil.getComponent(ActionResolver.class);
    }

    // -----------------------------------------------------
    //                                    Component Behavior
    //                                    ------------------
    protected boolean isFrameworkDebugEnabled() {
        final FwAssistantDirector assistantDirector = getAssistantDirector();
        return assistantDirector.assistOptionalCoreDirection().isFrameworkDebug();
    }

    // cannot use this because... see the Action Path calculation logic
    //protected String getRoutingOriginRequestPathAndQuery() {
    //  final RequestManager requestManager = getRequestManager();
    //  return requestManager.getRoutingOriginRequestPathAndQuery();
    //}

    protected String getRequestPath() {
        final RequestManager requestManager = getRequestManager();
        final String requestPath = requestManager.getRequestPath();
        return requestPath;
    }
}
