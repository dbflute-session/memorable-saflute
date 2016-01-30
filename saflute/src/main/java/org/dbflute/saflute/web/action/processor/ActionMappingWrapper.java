package org.dbflute.saflute.web.action.processor;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForward;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.ActionPathHandler;
import org.dbflute.saflute.web.action.ActionResolver;
import org.dbflute.saflute.web.action.api.ApiManager;
import org.dbflute.saflute.web.action.callback.ActionCallback;
import org.dbflute.util.Srl;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.container.ComponentDef;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.util.ServletContextUtil;

/**
 * @author jflute
 */
public class ActionMappingWrapper extends S2ActionMapping {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(ActionMappingWrapper.class);
    protected static final String REDIRECT = "redirect=true"; // copied from super because of private

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final S2ActionMapping original;
    protected final ActionAdjustmentProvider adjustmentProvider;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ActionMappingWrapper(S2ActionMapping original, ActionAdjustmentProvider adjustmentProvider) {
        this.original = original;
        this.adjustmentProvider = adjustmentProvider;
    }

    // ===================================================================================
    //                                                                  Forward Adjustment
    //                                                                  ==================
    // o to suppress that URL that contains dot is handled as JSP
    // o routing path of forward e.g. /member/list/ -> MemberListAction
    @Override
    public ActionForward createForward(String path, boolean redirect) { // almost copied from super
        if (path == null) {
            return null;
        }
        if (isResolvedResponse(path)) {
            // create dummy instance for action wrapper control
            // (this forward object is converted to null later)
            return createActionForward(path, redirect);
        }
        if (path.endsWith(REDIRECT)) {
            redirect = true;
            path = path.substring(0, path.length() - REDIRECT.length() - 1);
        }
        if (path.indexOf(":") < 0) {
            if (!path.startsWith("/")) {
                path = getActionPath(getComponentDef().getComponentName()) + path;
            }
            if (!redirect) { // forward here
                if (isJspForward(path)) { // e.g. JSP
                    path = filterJspPath(path);
                } else { // forward to action
                    path = createRoutingPath(path);
                }
            }
        }
        return createActionForward(path, redirect);
    }

    protected boolean isResolvedResponse(final String path) {
        return isApiResolvedDummyForward(path) || isResponseResolvedDummyForward(path);
    }

    protected boolean isApiResolvedDummyForward(final String path) {
        return path.endsWith(ApiManager.API_RESOLVED_DUMMY_FORWARD); // ends with just in case
    }

    protected boolean isResponseResolvedDummyForward(final String path) {
        return path.endsWith(ActionCallback.RESPONSE_RESOLVED_DUMMY_FORWARD); // ends with just in case
    }

    protected ActionForward createActionForward(String path, boolean redirect) {
        return new ActionForward(path, redirect);
    }

    protected boolean isJspForward(String path) {
        return path.endsWith(".jsp"); // you can only forward to JSP
    }

    protected String filterJspPath(String path) {
        final String viewPrefix = ServletContextUtil.getViewPrefix();
        if (viewPrefix != null) {
            path = viewPrefix + path; // e.g. /WEB-INF/view/...
        }
        final String filtered = adjustmentProvider.filterJspPath(path, this);
        return filtered != null ? filtered : path;
    }

    @Override
    protected String createRoutingPath(String path) {
        final ActionResolver resolver = ContainerUtil.getComponent(ActionResolver.class);
        try {
            final String delimiter = "?";
            final String requestPath;
            final String queryString; // with question mark if exists
            if (path.contains(delimiter)) {
                requestPath = Srl.substringFirstFront(path, delimiter);
                queryString = delimiter + Srl.substringFirstRear(path, delimiter);
            } else {
                requestPath = path;
                queryString = "";
            }
            final ForwardPathActionPathHandler handler = new ForwardPathActionPathHandler(queryString);
            resolver.handleActionPath(requestPath, handler);
            final String resolvedPath = handler.getResolvedPath();
            final String routingPath;
            if (resolvedPath != null) { // action found
                routingPath = resolvedPath;
            } else { // not found
                showExpectedRouting(requestPath, resolver);
                routingPath = path;
            }
            return routingPath;
        } catch (Exception e) {
            String msg = "Failed to resolve routing path to forward: " + path;
            throw new IllegalStateException(msg, e);
        }
    }

    protected class ForwardPathActionPathHandler implements ActionPathHandler {

        protected final String queryString;
        protected String resolvedPath;

        public ForwardPathActionPathHandler(String queryString) {
            this.queryString = queryString;
        }

        public boolean handleActionPath(String requestPath, String actionPath, String paramPath,
                S2ExecuteConfig configByParam) throws Exception {
            // the real query has question mark if exists
            final String realQuery = getQueryString(queryString, actionPath, paramPath, configByParam);
            resolvedPath = actionPath + ".do" + realQuery;
            return true;
        }

        public String getResolvedPath() {
            return resolvedPath;
        }
    }

    public ActionForward createForward(String path) { // called by e.g. validation error
        return createForward(path, false); // calling the wrapped method
    }

    protected void showExpectedRouting(String requestPath, ActionResolver resolver) {
        if (LOG.isDebugEnabled()) {
            if (!requestPath.contains(".")) { // except e.g. .do or .jpg
                LOG.debug(resolver.prepareExpectedRoutingMessage(requestPath));
            }
        }
    }

    // ===================================================================================
    //                                                                            Delegate
    //                                                                            ========
    public ModuleConfig getModuleConfig() {
        return original.getModuleConfig();
    }

    public ActionForward findForward(String name) {
        return original.findForward(name);
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        original.setModuleConfig(moduleConfig);
    }

    public String getAttribute() {
        return original.getAttribute();
    }

    public String[] findForwards() {
        return original.findForwards();
    }

    public ActionForward getInputForward() {
        return original.getInputForward();
    }

    public String getForward() {
        return original.getForward();
    }

    public void setForward(String forward) {
        original.setForward(forward);
    }

    public String getInclude() {
        return original.getInclude();
    }

    public void setInclude(String include) {
        original.setInclude(include);
    }

    public String getInput() {
        return original.getInput();
    }

    public void setInput(String input) {
        original.setInput(input);
    }

    public String getMultipartClass() {
        return original.getMultipartClass();
    }

    public void setMultipartClass(String multipartClass) {
        original.setMultipartClass(multipartClass);
    }

    public ComponentDef getComponentDef() {
        return original.getComponentDef();
    }

    public void setComponentDef(ComponentDef componentDef) {
        original.setComponentDef(componentDef);
    }

    public String getName() {
        return original.getName();
    }

    public void setName(String name) {
        original.setName(name);
    }

    public ComponentDef getActionFormComponentDef() {
        return original.getActionFormComponentDef();
    }

    public BeanDesc getActionBeanDesc() {
        return original.getActionBeanDesc();
    }

    public String getParameter() {
        return original.getParameter();
    }

    public BeanDesc getActionFormBeanDesc() {
        return original.getActionFormBeanDesc();
    }

    public void setParameter(String parameter) {
        original.setParameter(parameter);
    }

    public Object getAction() {
        return original.getAction();
    }

    public Object getActionForm() {
        return original.getActionForm();
    }

    public String getPropertyAsString(String name) {
        return original.getPropertyAsString(name);
    }

    public String getPath() {
        return original.getPath();
    }

    public void setPath(String path) {
        original.setPath(path);
    }

    public String getType() {
        return original.getType();
    }

    public String[] getExecuteMethodNames() {
        return original.getExecuteMethodNames();
    }

    public String getPrefix() {
        return original.getPrefix();
    }

    public S2ExecuteConfig findExecuteConfig(String paramPath) {
        return original.findExecuteConfig(paramPath);
    }

    public void setPrefix(String prefix) {
        original.setPrefix(prefix);
    }

    public S2ExecuteConfig findExecuteConfig(HttpServletRequest request) {
        return original.findExecuteConfig(request);
    }

    public String getRoles() {
        return original.getRoles();
    }

    public void setRoles(String roles) {
        original.setRoles(roles);
    }

    public S2ExecuteConfig getExecuteConfig(String name) {
        return original.getExecuteConfig(name);
    }

    public int getExecuteConfigSize() {
        return original.getExecuteConfigSize();
    }

    public void addExecuteConfig(S2ExecuteConfig executeConfig) {
        original.addExecuteConfig(executeConfig);
    }

    public String[] getRoleNames() {
        return original.getRoleNames();
    }

    public Field getActionFormField() {
        return original.getActionFormField();
    }

    public String getScope() {
        return original.getScope();
    }

    public void setScope(String scope) {
        original.setScope(scope);
    }

    public String getSuffix() {
        return original.getSuffix();
    }

    public void setSuffix(String suffix) {
        original.setSuffix(suffix);
    }

    public void setType(String type) {
        original.setType(type);
    }

    public boolean getUnknown() {
        return original.getUnknown();
    }

    public void setUnknown(boolean unknown) {
        original.setUnknown(unknown);
    }

    public boolean getValidate() {
        return original.getValidate();
    }

    public boolean getCancellable() {
        return original.getCancellable();
    }

    public void addExceptionConfig(ExceptionConfig config) {
        original.addExceptionConfig(config);
    }

    public void addForwardConfig(ForwardConfig config) {
        original.addForwardConfig(config);
    }

    public boolean equals(Object obj) {
        return original.equals(obj);
    }

    public ExceptionConfig findExceptionConfig(String type) {
        return original.findExceptionConfig(type);
    }

    public ExceptionConfig[] findExceptionConfigs() {
        return original.findExceptionConfigs();
    }

    @SuppressWarnings("rawtypes")
    public ExceptionConfig findException(Class type) {
        return original.findException(type);
    }

    public ForwardConfig findForwardConfig(String name) {
        return original.findForwardConfig(name);
    }

    public ForwardConfig[] findForwardConfigs() {
        return original.findForwardConfigs();
    }

    public void freeze() {
        original.freeze();
    }

    public int hashCode() {
        return original.hashCode();
    }

    public void setAttribute(String attribute) {
        original.setAttribute(attribute);
    }

    public void setActionFormField(Field actionFormField) {
        original.setActionFormField(actionFormField);
    }

    public void setValidate(boolean validate) {
        original.setValidate(validate);
    }

    public void setCancellable(boolean cancellable) {
        original.setCancellable(cancellable);
    }

    public void removeExceptionConfig(ExceptionConfig config) {
        original.removeExceptionConfig(config);
    }

    public void removeForwardConfig(ForwardConfig config) {
        original.removeForwardConfig(config);
    }

    public String toString() {
        return original.toString();
    }
}
