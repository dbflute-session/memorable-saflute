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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.web.action.processor.ActionAdjustmentProvider;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.util.S2ExecuteConfigUtil;

/**
 * The resolver of action.
 * @author jflute
 */
public class ActionResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(ActionResolver.class);

    /** The mark of redirect for SAStruts, same as {@link S2ActionMapping}. */
    public static final String REDIRECT = "redirect=true";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The assistant directory (AD) for framework. (NotNull: after initialization) */
    @Resource
    protected FwAssistantDirector assistantDirector;

    /** The container instance of Seasar for this class (not root but you can get root). (NotNull) */
    @Resource
    protected S2Container container;

    /** The naming convention instance of Seasar. (NotNull) */
    @Resource
    protected NamingConvention namingConvention;

    /** The provider of action adjustment. (NotNull: after initialization) */
    protected ActionAdjustmentProvider actionAdjustmentProvider;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this component. <br>
     * This is basically called by DI setting file.
     */
    public synchronized void initialize() {
        final OptionalActionDirection direction = assistOptionalActionDirection();
        actionAdjustmentProvider = direction.assistActionAdjustmentProvider();
        showBootLogging();
    }

    protected OptionalActionDirection assistOptionalActionDirection() {
        return assistantDirector.assistOptionalActionDirection();
    }

    protected void showBootLogging() {
        if (LOG.isInfoEnabled()) {
            LOG.info("[Action Resolver]");
            LOG.info(" actionAdjustmentProvider: " + actionAdjustmentProvider);
        }
    }

    // ===================================================================================
    //                                                                 ActionPath Handling
    //                                                                 ===================
    public boolean handleActionPath(String requestPath, ActionPathHandler handler) throws Exception {
        final String customized = actionAdjustmentProvider.customizeActionMappingRequestPath(requestPath);
        return actuallyHandleActionPath(customized != null ? customized : requestPath, handler);
    }

    protected boolean actuallyHandleActionPath(String requestPath, ActionPathHandler handler) throws Exception {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = =
        // the example URL on comments is '/member/list/'
        // = = = = = = = = = =/
        final String[] names = StringUtil.split(requestPath, "/"); // e.g. [member, list]
        final S2Container rootContainer = container.getRoot(); // because actions are in root
        final StringBuilder pkgPathSb = new StringBuilder(50); // e.g. "" -> member_ -> member_list_
        final StringBuilder prefixSb = new StringBuilder(50); // "" -> member -> memberList
        for (int i = 0; i < names.length; i++) {
            final String currentName = names[i];
            final String currentPrefix = prefixSb.toString();
            final int currentIndex = i;
            final int previousIndex = i - 1;
            final int nextIndex = i + 1;

            // /= = = = = = = = = = = = = = = = = = = = = = = = = = =
            // [Direct Action]
            //  e.g. 1st: memberAction, 2nd: member_listAction
            // = = = = = = = = = =/
            final String directAction = pkgPathSb + currentName + "Action";
            if (rootContainer.hasComponentDef(directAction)) {
                // e.g. 1st: /member, 2nd: /member/purchase
                final String actionPath = buildActionPath(names, currentIndex);
                final String paramPath = buildParamPath(names, nextIndex);
                if (doHandleActionPath(requestPath, handler, actionPath, paramPath)) {
                    return true;
                }
            }

            // /= = = = = = = = = = = = = = = = = = = = = = = = = = =
            // [Prefix Direct Action]
            //  e.g. 1st: no way, 2nd: member_memberListAction
            // = = = = = = = = = =/
            if (currentPrefix.trim().length() > 0) {
                final String componentPrefix = buildComponentPrefix(getFixedPrefix(), currentPrefix);
                final String prefixDirectAction = pkgPathSb + componentPrefix + initCap(currentName) + "Action";
                if (rootContainer.hasComponentDef(prefixDirectAction)) {
                    // e.g. 1st: no way, 2nd: /member/memberList
                    final String actionPath = buildActionPath(names, currentIndex, componentPrefix);
                    final String paramPath = buildParamPath(names, nextIndex);
                    if (doHandleActionPath(requestPath, handler, actionPath, paramPath)) {
                        return true;
                    }
                }
            }

            // /= = = = = = = = = = = = = = = = = = = = = = = = = = =
            // [Index Action]
            // e.g. 1st: no way, 2nd: member_indexAction
            // = = = = = = = = = =/
            // root index is always true but no handling if it's no parameter
            final String indexAction = pkgPathSb + "indexAction";
            if (rootContainer.hasComponentDef(indexAction)) {
                final String actionPath = buildActionPath(names, previousIndex) + "/index";
                final String paramPath = buildParamPath(names, currentIndex);
                if (doHandleActionPath(requestPath, handler, actionPath, paramPath)) {
                    return true;
                }
            }

            // /= = = = = = = = = = = = = = = = = = = = = = = = = = =
            // [Prefix Index Action]
            // e.g. 1st: no way, 2nd: member_memberIndexAction
            // = = = = = = = = = =/
            if (currentPrefix.trim().length() > 0) {
                final String componentPrefix = buildComponentPrefix(getFixedPrefix(), currentPrefix);
                final String prefixIndexAction = pkgPathSb + componentPrefix + "IndexAction";
                if (rootContainer.hasComponentDef(prefixIndexAction)) {
                    final String actionPath = buildActionPath(names, previousIndex) + "/" + componentPrefix + "Index";
                    final String paramPath = buildParamPath(names, currentIndex);
                    if (doHandleActionPath(requestPath, handler, actionPath, paramPath)) {
                        return true;
                    }
                }
            }

            pkgPathSb.append(currentName + "_");
            prefixSb.append(i == 0 ? currentName : initCap(currentName));
        }

        // /= = = = = = = = = = = = = = = = = = = = = = = = = = =
        // [Root or Last Index Action]
        // e.g. member_list_indexAction
        // = = = = = = = = = =/
        final int lastIndex = names.length - 1;
        final String finalIndexAction = pkgPathSb + "indexAction";
        if (rootContainer.hasComponentDef(finalIndexAction)) { // when no loop
            final String actionPath = buildActionPath(names, lastIndex) + "/index";
            if (doHandleActionPath(requestPath, handler, actionPath, null)) {
                return true;
            }
        }

        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // [Last Prefix Index Action]
        // e.g. member_list_memberListIndexAction (without parameter)
        // = = = = = = = = = =/
        if (prefixSb.length() > 0) {
            final String currentPrefix = prefixSb.toString();
            final String componentPrefix = buildComponentPrefix(getFixedPrefix(), currentPrefix);
            final String finalPrefixIndexAction = pkgPathSb + componentPrefix + "IndexAction";
            if (rootContainer.hasComponentDef(finalPrefixIndexAction)) {
                final String actionPath = buildActionPath(names, lastIndex) + "/" + componentPrefix + "Index";
                if (doHandleActionPath(requestPath, handler, actionPath, null)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected String extractContextPath(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        if (contextPath.equals("/")) {
            contextPath = "";
        }
        return contextPath;
    }

    protected String buildActionPath(String[] names, int index) {
        final StringBuilder sb = new StringBuilder(30);
        for (int i = 0; i <= index; i++) {
            sb.append('/').append(names[i]);
        }
        return sb.toString();
    }

    protected String buildActionPath(String[] names, int index, String prefix) {
        StringBuilder sb = new StringBuilder(30);
        for (int i = 0; i <= index; i++) {
            sb.append('/');
            if (i == index) { // last loop
                sb.append(prefix).append(initCap(names[i]));
            } else {
                sb.append(names[i]);
            }
        }
        return sb.toString();
    }

    protected String buildParamPath(String[] names, int index) {
        final StringBuilder sb = new StringBuilder(30);
        for (int i = index; i < names.length; i++) {
            if (i != index) {
                sb.append('/');
            }
            sb.append(names[i]);
        }
        return sb.toString();
    }

    protected String getFixedPrefix() {
        return ""; // as default
    }

    protected String buildComponentPrefix(String fixedPrefix, String currentPrefix) {
        final String componentPrefix;
        if (!StringUtil.isEmpty(fixedPrefix)) {
            componentPrefix = initUncap(fixedPrefix) + initCap(currentPrefix);
        } else {
            componentPrefix = initUncap(currentPrefix);
        }
        return componentPrefix;
    }

    protected boolean doHandleActionPath(String requestPath, ActionPathHandler handler, String actionPath,
            String paramPath) throws Exception {
        final boolean emptyParam = StringUtil.isEmpty(paramPath);
        final S2ExecuteConfig configByParam = !emptyParam ? findExecuteConfig(actionPath, paramPath) : null;
        if (emptyParam || configByParam != null) { // certainly hit
            return handler.handleActionPath(requestPath, actionPath, paramPath, configByParam);
        }
        return false;
    }

    protected S2ExecuteConfig findExecuteConfig(String actionPath, String paramPath) {
        return S2ExecuteConfigUtil.findExecuteConfig(actionPath, paramPath);
    }

    // ===================================================================================
    //                                                                  ActionPath Convert
    //                                                                  ==================
    /**
     * Convert to URL string to move the action. <br>
     * This method is to build URL string by manually so normally you don't use directly from your action.
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @param redirect Do you redirect to the action?
     * @param chain The chain of URL to build additional info on URL. (NullAllowed)
     * @return The URL string to move to the action. (NotNull)
     */
    public String toActionUrl(Class<?> actionType, boolean redirect, UrlChain chain) {
        final String actionPath = resolveActionPath(actionType);
        final List<Object> getParamList = extractGetParamList(chain);
        final StringBuilder sb = new StringBuilder();
        sb.append(actionPath);
        buildUrlParts(sb, chain);
        buildGetParam(sb, actionPath, getParamList);
        buildHashOnUrl(sb, chain);
        buildRedirectMark(sb, redirect, getParamList);
        return sb.toString();
    }

    protected List<Object> extractGetParamList(UrlChain chain) {
        final Object[] paramsOnGet = chain != null ? chain.getParamsOnGet() : null;
        final List<Object> getParamList;
        if (paramsOnGet != null) {
            getParamList = DfCollectionUtil.newArrayList(paramsOnGet);
        } else {
            getParamList = DfCollectionUtil.emptyList();
        }
        return getParamList;
    }

    protected void buildUrlParts(StringBuilder sb, UrlChain chain) {
        final Object[] urlParts = chain != null ? chain.getUrlParts() : null;
        if (urlParts != null) {
            for (Object param : urlParts) {
                if (param != null) {
                    sb.append(param).append("/");
                }
            }
        }
    }

    protected void buildGetParam(StringBuilder sb, String actionPath, List<Object> getParamList) {
        int index = 0;
        for (Object param : getParamList) {
            if (index == 0) { // first loop
                sb.append("?");
            } else {
                if (index % 2 == 0) {
                    sb.append("&");
                } else if (index % 2 == 1) {
                    sb.append("=");
                } else { // no way
                    String msg = "no way: url=" + actionPath + " get-params=" + getParamList;
                    throw new IllegalStateException(msg);
                }
            }
            sb.append(param != null ? param : "");
            ++index;
        }
    }

    protected void buildHashOnUrl(StringBuilder sb, UrlChain chain) {
        final Object hash = chain != null ? chain.getHashOnUrl() : null;
        if (hash != null) {
            sb.append("#").append(hash);
        }
    }

    protected void buildRedirectMark(StringBuilder sb, boolean redirect, List<Object> getParamList) {
        if (redirect) {
            sb.append(!getParamList.isEmpty() ? "&" : "?");
            sb.append(REDIRECT);
        }
    }

    // -----------------------------------------------------
    //                                    Resolve ActionPath
    //                                    ------------------
    public String toActionUrl(Class<?> actionType, boolean redirect, Object[] urlParts, Object[] paramsOnGet) {
        final String actionPath = resolveActionPath(actionType);
        final List<Object> getParamList;
        if (paramsOnGet != null) {
            getParamList = DfCollectionUtil.newArrayList(paramsOnGet);
        } else {
            getParamList = DfCollectionUtil.newArrayList();
        }
        if (redirect) {
            getParamList.add("redirect");
            getParamList.add("true");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(actionPath);
        if (urlParts != null) {
            for (Object param : urlParts) {
                if (param != null) {
                    sb.append(param).append("/");
                }
            }
        }
        int index = 0;
        for (Object param : getParamList) {
            if (index == 0) { // first loop
                sb.append("?");
            } else {
                if (index % 2 == 0) {
                    sb.append("&");
                } else if (index % 2 == 1) {
                    sb.append("=");
                } else { // no way
                    String msg = "no way: url=" + actionPath + " get-params=" + getParamList;
                    throw new IllegalStateException(msg);
                }
            }
            sb.append(param != null ? param : "");
            ++index;
        }
        return sb.toString();
    }

    public String resolveActionPath(Class<?> actionType) {
        final String className = actionType.getName();
        final String componentName = namingConvention.fromClassNameToComponentName(className);

        // e.g. member_purchaseAction -> member/purchaseAction
        //  or member_appMemberPurchaseAction -> member/appMemberPurchaseAction
        final String delimiter = "/";
        String path = delimiter + replace(componentName, "_", delimiter);

        // remove suffix 'Action' if exists
        // e.g. member/purchaseAction -> member/purchase
        //  or member/memberPurchaseAction -> member/memberPurchase
        path = removeRearActionSuffixIfNeeds(path);

        // e.g. member/memberPurchase -> member/purchase
        path = removepActionPrefixIfNeeds(path);

        // e.g. member/purchase/index -> member/purchase
        path = removeRearIndexPathIfNeeds(path);

        if (!path.endsWith(delimiter)) {
            path = path + delimiter;
        }

        return path;
    }

    protected String removeRearActionSuffixIfNeeds(String path) {
        final String actionSuffix = namingConvention.getActionSuffix();
        if (path.endsWith(actionSuffix)) {
            return path.substring(0, path.length() - actionSuffix.length());
        }
        return path;
    }

    protected String removepActionPrefixIfNeeds(String path) {
        final String delimiter = "/";
        final String packagePath = substringLastFront(path, delimiter);
        final String pureName = substringLastRear(path, delimiter);
        final List<String> packageElementList = splitList(packagePath, delimiter);
        final StringBuilder packagePrefixSb = new StringBuilder();
        for (String element : packageElementList) {
            packagePrefixSb.append(initCap(element));
        }
        final String prefix = initUncap(getActionFixedPrefix() + packagePrefixSb);
        if (pureName.startsWith(prefix)) {
            return packagePath + delimiter + initUncap(substringFirstRear(pureName, prefix));
        }
        return path;
    }

    protected String getActionFixedPrefix() {
        return ""; // as default (no fixed prefix)
    }

    protected String removeRearIndexPathIfNeeds(String path) {
        if (path.endsWith("/index")) {
            return path.substring(0, path.length() - "/index".length());
        }
        return path;
    }

    // ===================================================================================
    //                                                              ActionPath Calculation
    //                                                              ======================
    public String calculateActionPathByJspPath(String requestPath) {
        final String lastPathElement = substringLastRear(requestPath, "/");
        if (!lastPathElement.contains(".")) { // no JSP
            return requestPath;
        }
        // basically JSP here
        final String frontPathElement = substringLastFront(requestPath, "/");
        final String fileNameNoExt = substringLastFront(lastPathElement, ".");
        final String pathBase = frontPathElement + "/";
        if (!fileNameNoExt.contains("_")) { // e.g. list.jsp
            return pathBase; // e.g. /member/ (submit name is needed in this case)
        }
        // the file name has package prefix here
        // e.g. /member/member_list.jsp or /member/list/member_purchase_list.jsp
        final List<String> wordList = splitList(fileNameNoExt, "_"); // e.g. [member, list] or [member, purchase, list]
        if (wordList.size() < 2) { // no way (just in case)
            return pathBase;
        }
        final String firstHit = resolveJspActionPath(requestPath, frontPathElement, pathBase, wordList);
        if (firstHit != null) {
            return firstHit; // e.g. /member/list/ (from /member/member_list.jsp)
        }
        final List<String> retryList = prepareJspRetryWordList(requestPath, wordList);
        if (retryList != null && !retryList.isEmpty()) { // e.g. [member, list] (from sp_member_list.jsp)
            final String retryHit = resolveJspActionPath(requestPath, frontPathElement, pathBase, retryList);
            if (retryHit != null) {
                return retryHit; // e.g. /member/list/ (from /member/sp_member_list.jsp)
            }
        }
        // e.g. /member/purchase_list.jsp
        return pathBase; // e.g. /member/ (submit name is needed in this case)
    }

    protected String resolveJspActionPath(String requestPath, String frontPathElement, String pathBase,
            List<String> wordList) {
        String previousSuffix = "";
        for (int i = 0; i < wordList.size(); i++) {
            // e.g. 1st: '' + '/' + member, 2nd: /member + '/' + purchase
            final String pathSuffix = previousSuffix + "/" + wordList.get(i);
            final boolean nextLoopLast = wordList.size() == i + 2;
            if (nextLoopLast && frontPathElement.endsWith(pathSuffix)) {
                // e.g. 1st: /member/list/, 2nd: /member/purchase/list/
                final String lastElement = wordList.get(i + 1);
                final String resolvedPath;
                if (lastElement.equals("index")) { // e.g. /member/list/member_list_index.jsp
                    resolvedPath = pathBase;
                } else {
                    resolvedPath = pathBase + lastElement + "/";
                }
                return resolvedPath;
            }
            previousSuffix = pathSuffix;
        }
        return null;
    }

    protected List<String> prepareJspRetryWordList(String requestPath, List<String> wordList) {
        return actionAdjustmentProvider.prepareJspRetryWordList(requestPath, wordList);
    }

    // ===================================================================================
    //                                                                 Redirect Adjustment
    //                                                                 ===================
    /**
     * Convert the request path (or URL) to redirect path for SAStruts. e.g. /member/list/?redirect=true
     * @param requestPath The path of request. e.g. /member/list/ (NotNull)
     * @return The request path (or URL) with redirect mark. (NotNull)
     */
    public String toRedirectPath(String requestPath) {
        final String delimiter = requestPath.contains("?") ? "&" : "?";
        return requestPath + delimiter + REDIRECT;
    }

    /**
     * Convert the request path (or URL) to SSL redirect path for SAStruts. <br>
     * e.g. https://...com/member/list/?redirect=true
     * @param requestPath The path (or URL) of request. e.g. http://...com/member/list/ (NotNull)
     * @return The request path (or URL) with redirect mark. (NotNull)
     */
    public String toSslRedirectPath(String requestPath) {
        return toRedirectPath(requestPath.replaceFirst("http:", "https:"));
    }

    /**
     * Convert the request path (or URL) to non-SSL redirect path for SAStruts. <br>
     * e.g. http://...com/member/list/?redirect=true
     * @param requestPath The path (or URL) of request. e.g. https://...com/member/list/ (NotNull)
     * @return The request path (or URL) with redirect mark. (NotNull)
     */
    public String toNonSslRedirectPath(String requestPath) {
        return toRedirectPath(requestPath.replaceFirst("https:", "http:"));
    }

    /**
     * Remove the redirect mark from redirect path for SAStruts. <br>
     * e.g. /member/list/?redirect=true -&gt; /member/list/
     * @param redirectPath The path to redirect for SAStruts. e.g. /member/list/?redirect=true (NotNull)
     * @return The plain redirect path without redirect mark. (NotNull)
     */
    public String removeRedirectMark(String redirectPath) {
        final String redirectMark = REDIRECT;
        if (redirectPath.endsWith(redirectMark)) {
            final String removed = substringLastFront(redirectPath, redirectMark);
            return removed.substring(0, removed.length() - 1); // and remove delimiter
        }
        return redirectPath;
    }

    // ===================================================================================
    //                                                                    Expected Routing
    //                                                                    ================
    public String prepareExpectedRoutingMessage(String requestPath) { // for debug
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("/= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = *No routing action:\n");
        sb.append("e.g. expected actions for ").append(requestPath).append("\n");
        final List<String> nameList = buildExpectedRoutingActionList(requestPath);
        for (String name : nameList) {
            sb.append("  web.").append(name).append("\n");
        }
        sb.append("  (and so on...)\n");
        sb.append("= = = = = = = = = =/");
        return sb.toString();
    }

    protected List<String> buildExpectedRoutingActionList(String requestPath) {
        final List<String> tokenList;
        {
            final String trimmedPath = trim(requestPath, "/"); // /member/list/ -> member/list
            final List<String> splitList = splitList(trimmedPath, "/"); // [member, list]
            tokenList = new ArrayList<String>(splitList.size()); // removed empty elements
            for (String element : splitList) {
                if (element.trim().length() == 0) {
                    continue; // e.g. /member//list/
                }
                tokenList.add(element);
            }
        }
        // e.g. / or /123/ or /123/foo/
        if (tokenList.isEmpty() || mayBeParameterToken(tokenList.get(0))) {
            final List<String> nameList = new ArrayList<String>(1);
            nameList.add("IndexAction#index()");
            return nameList;
        }
        // e.g. /foo/ or /foo/123/ or /foo/123/bar/
        //   web.FooAction#index()
        //   web.IndexAction#foo()
        //   web.foo.FooIndexAction#index()
        // e.g. /foo/bar/ or /foo/bar/123/ or /foo/bar/123/qux/
        //   foo.FooBarAction#index()
        //   foo.FooAction#bar()
        //   foo.bar.FooBarIndexAction#index()
        final StringBuilder namedActionSb = new StringBuilder();
        final StringBuilder methodActionSb = new StringBuilder();
        final StringBuilder indexActionSb = new StringBuilder();
        final StringBuilder pkgPrefix = new StringBuilder();
        for (int index = 0; index < tokenList.size(); index++) {
            final String current = tokenList.get(index);
            final boolean beforeLastLoop = index < tokenList.size() - 1;
            final String next = beforeLastLoop ? tokenList.get(index + 1) : null;
            final boolean nextParam = next != null ? mayBeParameterToken(next) : false;
            final String capElement = initCap(current);
            if (beforeLastLoop && !nextParam) { // before last action token
                namedActionSb.append(current).append(".");
                methodActionSb.append(current).append(".");
                indexActionSb.append(current).append(".");
                pkgPrefix.append(capElement);
            } else { // last action token here (last loop or next token is parameter)
                // web.FooAction#index() or foo.FooBarAction#index()
                namedActionSb.append(pkgPrefix).append(capElement).append("Action#index()");

                // web.IndexAction#foo() or foo.FooAction#bar()
                methodActionSb.append(pkgPrefix.length() > 0 ? pkgPrefix : "Index").append("Action#");
                methodActionSb.append(current).append("()");

                // web.foo.FooIndexAction#index() or foo.bar.FooBarIndexAction#index()
                pkgPrefix.append(capElement);
                indexActionSb.append(current).append(".").append(pkgPrefix).append("IndexAction#index()");
                break;
            }
        }
        final List<String> nameList = new ArrayList<String>(3);
        nameList.add(namedActionSb.toString());
        nameList.add(methodActionSb.toString());
        nameList.add(indexActionSb.toString());
        return nameList;
    }

    protected boolean mayBeParameterToken(String token) {
        if (NumberUtils.isNumber(token.substring(0, 1))) { // e.g. 123 or 4ab
            return true;
        }
        if (DfStringUtil.containsAny(token, ".", "%", "?", "&")) { // e.g. a.b or %2d or ?foo=bar or ...
            return true;
        }
        return false;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replace(String str, String fromStr, String toStr) {
        return DfStringUtil.replace(str, fromStr, toStr);
    }

    protected String substringFirstFront(String str, String... delimiters) {
        return DfStringUtil.substringFirstFront(str, delimiters);
    }

    protected String substringFirstRear(String str, String... delimiters) {
        return DfStringUtil.substringFirstRear(str, delimiters);
    }

    protected String substringLastFront(String str, String... delimiters) {
        return DfStringUtil.substringLastFront(str, delimiters);
    }

    protected String substringLastRear(String str, String... delimiters) {
        return DfStringUtil.substringLastRear(str, delimiters);
    }

    protected List<String> splitList(String str, String delimiter) {
        return DfStringUtil.splitList(str, delimiter);
    }

    protected String initCap(String str) {
        return DfStringUtil.initCap(str);
    }

    protected String initUncap(String str) {
        return DfStringUtil.initUncap(str);
    }

    protected String trim(String str, String trimStr) {
        return DfStringUtil.trim(str, trimStr);
    }
}
