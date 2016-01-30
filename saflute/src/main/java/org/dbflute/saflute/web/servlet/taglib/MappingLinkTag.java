package org.dbflute.saflute.web.servlet.taglib;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import org.apache.struts.Globals;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.Constants;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.ActionPathHandler;
import org.dbflute.saflute.web.action.ActionResolver;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.taglib.base.TaglibLogic;
import org.dbflute.saflute.web.servlet.taglib.exception.LinkActionNotFoundException;
import org.seasar.framework.util.StringUtil;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.taglib.S2LinkTag;
import org.seasar.struts.util.RequestUtil;
import org.seasar.struts.util.ResponseUtil;

/**
 * @author jflute
 * @author Tanaka
 */
public class MappingLinkTag extends S2LinkTag {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The language for linked contents (NullAllowd: if NotNull, add the path to lang) */
    protected String lang;

    // ===================================================================================
    //                                                                           Calculate
    //                                                                           =========
    @Override
    protected String calculateURL() throws JspException {
        if (href == null) {
            return super.calculateURL();
        }
        if (href.indexOf(':') > -1) {
            return super.calculateURL();
        }
        String url = buildHrefUrl(href); // Not Null
        if (transaction) {
            final HttpSession session = pageContext.getSession();
            if (session != null) {
                final String token = (String) session.getAttribute(Globals.TRANSACTION_TOKEN_KEY);
                if (token != null) {
                    final String c = url != null && url.indexOf('?') >= 0 ? "&" : "?";
                    url = url + c + Constants.TOKEN_KEY + "=" + token;
                }
            }
        }
        // Copied from Struts TagUtils (and small adjustment)
        // Add anchor if requested (adding only here, duplicate if any anchor exists)
        if (anchor != null) {
            final String charEncoding = useLocalEncoding ? pageContext.getResponse().getCharacterEncoding() : "UTF-8";
            url = url + "#" + TagUtils.getInstance().encodeURL(anchor, charEncoding);
        }

        // AbsolutePath & lang is not null
        if (StringUtil.isNotBlank(lang)) {
            // absolute path only
            if (url.startsWith("/")) {
                url = "/" + lang + url; // e.g) if lang is ja, /contents/about/ => /ja/contents/about/
            } else {
                throwLangIllegalArgumentException();
            }
        }
        return url;
    }

    protected String buildHrefUrl(final String input) {
        final String contextPath = RequestUtil.getRequest().getContextPath();
        final StringBuilder sb = new StringBuilder();
        if (contextPath.length() > 1) {
            sb.append(contextPath);
        }
        if (StringUtil.isEmpty(input)) {
            final String requestPath = getRequestPath();
            final String actionPath = calculateActionPathByJspPath(requestPath);
            sb.append(actionPath);
        } else if (!input.startsWith("/")) { // add/, ../add/
            final String requestPath = getRequestPath();
            final String actionPath = calculateActionPathByJspPath(requestPath);
            sb.append(actionPath).append(appendSlashRearIfNeeds(input)); // rear slash is added automatically
        } else { // /member/list/
            resolveAbsolutePath(input, sb);
        }
        return ResponseUtil.getResponse().encodeURL(sb.toString());
    }

    protected void resolveAbsolutePath(final String input, final StringBuilder sb) {
        final int paramMarkIndex = input.indexOf('?');
        final String path = paramMarkIndex >= 0 ? input.substring(0, paramMarkIndex) : input;
        final String queryString = paramMarkIndex >= 0 ? input.substring(paramMarkIndex) : "";
        final ActionResolver resolver = getActionResolver();
        try {
            final ActionPathHandler handler = createActionPathHandler(sb, path, queryString);
            final boolean handled = resolver.handleActionPath(path, handler);
            if (!handled) {
                throwLinkActionNotFoundException(path, queryString);
            }
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                final String msg = "Failed to handle action path: " + path;
                throw new IllegalStateException(msg, e);
            }
        }
    }

    protected ActionPathHandler createActionPathHandler(final StringBuilder sb, final String path,
            final String queryString) {
        return new ActionPathHandler() {
            @Override
            public boolean handleActionPath(final String requestPath, final String actionPath, final String paramPath,
                    final S2ExecuteConfig executeConfig) throws IOException, ServletException {
                // not use actionPath because the path may have prefix
                // see the form tag class for the details
                sb.append(appendSlashRearIfNeeds(path)); // rear slash is added automatically
                sb.append(queryString);
                return true;
            }
        };
    }

    protected String appendSlashRearIfNeeds(final String str) {
        return str + (!str.endsWith("/") ? "/" : "");
    }

    protected String appendQuestionFrontIfNeeds(final String str) {
        return (!str.equals("") ? "?" : "") + str;
    }

    protected void throwLinkActionNotFoundException(final String path, final String queryString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the action for the action path of link tag.");
        br.addItem("Requested JSP Path");
        br.addElement(createTablibLogic().getRequestJspPath());
        br.addItem("Action Path");
        br.addElement(path);
        br.addItem("Query String");
        br.addElement(queryString);
        br.addItem("Defined Href");
        br.addElement(href);
        final String msg = br.buildExceptionMessage();
        throw new LinkActionNotFoundException(msg);
    }

    protected void throwLangIllegalArgumentException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("When you specify the lang, Please make relative path.");
        br.addItem("Requested JSP Path");
        br.addElement(createTablibLogic().getRequestJspPath());
        br.addItem("Defined Href");
        br.addElement(href);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg);
    }

    // ===================================================================================
    //                                                                         Action Path
    //                                                                         ===========
    protected String calculateActionPath() {
        // cannot use request path of routing origin here
        // see the form tag class for the details
        //final String routingOriginRequestPathAndQuery = getRoutingOriginRequestPathAndQuery();
        //if (routingOriginRequestPathAndQuery != null) { // first priority
        //  return routingOriginRequestPathAndQuery;
        //}
        final String requestPath = getRequestPath();
        return calculateActionPathByJspPath(requestPath);
    }

    protected String calculateActionPathByJspPath(final String requestPath) {
        final ActionResolver resolver = getActionResolver();
        return resolver.calculateActionPathByJspPath(requestPath);
    }

    // ===================================================================================
    //                                                                        Taglib Logic
    //                                                                        ============
    protected TaglibLogic createTablibLogic() {
        return new TaglibLogic();
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected RequestManager getRequestManager() {
        return ContainerUtil.getComponent(RequestManager.class);
    }

    protected ActionResolver getActionResolver() {
        return ContainerUtil.getComponent(ActionResolver.class);
    }

    // -----------------------------------------------------
    //                                    Component Behavior
    //                                    ------------------
    protected String getRequestPath() {
        final RequestManager requestManager = getRequestManager();
        final String requestPath = requestManager.getRequestPath();
        return requestPath;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getLang() {
        return this.lang;
    }

    public void setLang(final String lang) {
        this.lang = lang;
    }

    @Override
    public void setTitle(String title) { // for label use
        final TaglibLogic tablibLogic = createTablibLogic();
        super.setTitle(tablibLogic.resolveTitleResource(pageContext, title, getProperty()));
    }
}
