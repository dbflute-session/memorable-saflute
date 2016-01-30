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

import javax.annotation.Resource;

import org.dbflute.saflute.core.json.JsonManager;
import org.dbflute.saflute.web.action.exception.GetParameterNotFoundException;
import org.dbflute.saflute.web.action.response.JsonResponse;
import org.dbflute.saflute.web.action.response.StreamResponse;
import org.dbflute.saflute.web.action.response.XmlResponse;
import org.dbflute.saflute.web.servlet.request.RequestManager;
import org.dbflute.saflute.web.servlet.request.ResponseManager;

/**
 * @author jflute
 */
public class RootAction {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final UrlChain EMPTY_URL_CHAIN = new UrlChain(null);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The resolver of action, e.g. it can convert action type to action path. (NotNull) */
    @Resource
    protected ActionResolver actionResolver;

    /** The manager of request. (NotNull) */
    @Resource
    protected RequestManager requestManager;

    /** The manager of response. (NotNull) */
    @Resource
    protected ResponseManager responseManager;

    /** The manager of JSON. (NotNull) */
    @Resource
    protected JsonManager jsonManager;

    // ===================================================================================
    //                                                                          Transition
    //                                                                          ==========
    // -----------------------------------------------------
    //                                              Redirect
    //                                              --------
    /**
     * Redirect to the action (index method).
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/</span>
     * return redirect(MemberEditAction.class);
     *
     * <span style="color: #3F7E5E">// e.g. /member/</span>
     * return redirect(MemberIndexAction.class);
     * </pre>
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @return The URL string for redirect. (NotNull)
     */
    protected String redirect(Class<?> actionType) {
        assertArgumentNotNull("actionType", actionType);
        return redirectWith(actionType, EMPTY_URL_CHAIN);
    }

    /**
     * Redirect to the action (index method) by the IDs on URL.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/</span>
     * return redirectById(MemberEditAction.class, 3);
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/197/</span>
     * return redirectById(MemberEditAction.class, 3, 197);
     *
     * <span style="color: #3F7E5E">// e.g. /member/3/</span>
     * return redirectById(MemberIndexAction.class, 3);
     * </pre>
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @param ids The varying array for IDs. (NotNull)
     * @return The URL string for redirect. (NotNull)
     */
    protected String redirectById(Class<?> actionType, Number... ids) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("ids", ids);
        final Object[] objAry = (Object[]) ids; // to suppress warning
        return redirectWith(actionType, moreUrl(objAry));
    }

    /**
     * Redirect to the action (index method) by the parameters on GET.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/?foo=3</span>
     * return redirectByParam(MemberEditAction.class, "foo", 3);
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/?foo=3&amp;bar=qux</span>
     * return redirectByParam(MemberEditAction.class, "foo", 3, "bar", "qux");
     *
     * <span style="color: #3F7E5E">// e.g. /member/?foo=3</span>
     * return redirectByParam(MemberIndexAction.class, "foo", 3);
     * </pre>
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @param params The varying array for the parameters on GET. (NotNull)
     * @return The URL string for redirect. (NotNull)
     */
    protected String redirectByParam(Class<?> actionType, Object... params) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("params", params);
        return redirectWith(actionType, params(params));
    }

    /**
     * Redirect to the action with the more URL parts and the the parameters on GET.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/ *same as {@link #redirectById()}</span>
     * return redirectWith(MemberEditAction.class, <span style="color: #FD4747">moreUrl</span>(memberId));
     * 
     * <span style="color: #3F7E5E">// e.g. /member/edit/?memberId=3 *same as {@link #redirectByParam()}</span>
     * return redirectWith(MemberEditAction.class, <span style="color: #FD4747">params</span>("memberId", memberId));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/</span>
     * return redirectWith(MemberIndexAction.class, <span style="color: #FD4747">moreUrl</span>("edit", memberId));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/#profile</span>
     * return redirectWith(MemberEditAction.class, <span style="color: #FD4747">moreUrl</span>(memberId).<span style="color: #FD4747">hash</span>("profile"));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/?memberId=3#profile</span>
     * return redirectWith(MemberEditAction.class, <span style="color: #FD4747">params</span>("memberId", memberId).<span style="color: #FD4747">hash</span>("profile"));
     * </pre>
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @param moreUrl_or_params The chain of URL. (NotNull)
     * @return The URL string for redirect containing GET parameters. (NotNull)
     */
    protected String redirectWith(Class<?> actionType, UrlChain moreUrl_or_params) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("moreUrl_or_params", moreUrl_or_params);
        return doRedirect(actionType, moreUrl_or_params);
    }

    /**
     * Do redirect the action with the more URL parts and the the parameters on GET. <br>
     * This method is to other redirect methods so normally you don't use directly from your action.
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @param chain The chain of URL to build additional info on URL. (NotNull)
     * @return The URL string for redirect containing GET parameters. (NotNull)
     */
    protected String doRedirect(Class<?> actionType, UrlChain chain) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("chain", chain);
        return toActionUrl(actionType, true, chain);
    }

    // -----------------------------------------------------
    //                                               Forward
    //                                               -------
    /**
     * Forward to the action (index method).
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/</span>
     * return forward(MemberEditAction.class);
     *
     * <span style="color: #3F7E5E">// e.g. /member/</span>
     * return forward(MemberIndexAction.class);
     * </pre>
     * @param actionType The class type of action that it forwards to. (NotNull)
     * @return The URL string for forward. (NotNull)
     */
    protected String forward(Class<?> actionType) {
        assertArgumentNotNull("actionType", actionType);
        return forwardWith(actionType, EMPTY_URL_CHAIN);
    }

    /**
     * Forward to the action (index method) by the IDs on URL.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/</span>
     * return forwardById(MemberEditAction.class, 3);
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/197/</span>
     * return forwardById(MemberEditAction.class, 3, 197);
     *
     * <span style="color: #3F7E5E">// e.g. /member/3/</span>
     * return forwardById(MemberIndexAction.class, 3);
     * </pre>
     * @param actionType The class type of action that it forwards to. (NotNull)
     * @param ids The varying array for IDs. (NotNull)
     * @return The URL string for forward. (NotNull)
     */
    protected String forwardById(Class<?> actionType, Number... ids) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("ids", ids);
        final Object[] objAry = (Object[]) ids; // to suppress warning
        return forwardWith(actionType, moreUrl(objAry));
    }

    /**
     * Forward to the action (index method) by the parameters on GET.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/?foo=3</span>
     * return forwardByParam(MemberEditAction.class, "foo", 3);
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/?foo=3&amp;bar=qux</span>
     * return forwardByParam(MemberEditAction.class, "foo", 3, "bar", "qux");
     *
     * <span style="color: #3F7E5E">// e.g. /member/?foo=3</span>
     * return forwardByParam(MemberIndexAction.class, "foo", 3);
     * </pre>
     * @param actionType The class type of action that it forwards to. (NotNull)
     * @param params The varying array for the parameters on GET. (NotNull)
     * @return The URL string for forward. (NotNull)
     */
    protected String forwardByParam(Class<?> actionType, Object... params) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("params", params);
        return forwardWith(actionType, params(params));
    }

    /**
     * Forward to the action with the more URL parts and the the parameters on GET.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/ *same as {@link #forwardById()}</span>
     * return forwardWith(MemberEditAction.class, <span style="color: #FD4747">moreUrl</span>(memberId));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/?memberId=3 *same as {@link #forwardByParam()}</span>
     * return forwardWith(MemberEditAction.class, <span style="color: #FD4747">params</span>("memberId", memberId));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/</span>
     * return forwardWith(MemberIndexAction.class, <span style="color: #FD4747">moreUrl</span>("edit", memberId));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/3/#profile</span>
     * return forwardWith(MemberEditAction.class, <span style="color: #FD4747">moreUrl</span>(memberId).<span style="color: #FD4747">hash</span>("profile"));
     *
     * <span style="color: #3F7E5E">// e.g. /member/edit/?memberId=3#profile</span>
     * return forwardWith(MemberEditAction.class, <span style="color: #FD4747">params</span>("memberId", memberId).<span style="color: #FD4747">hash</span>("profile"));
     * </pre>
     * @param actionType The class type of action that it forwards to. (NotNull)
     * @param moreUrl_or_params The chain of URL. (NotNull)
     * @return The URL string for forward containing GET parameters. (NotNull)
     */
    protected String forwardWith(Class<?> actionType, UrlChain moreUrl_or_params) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("moreUrl_or_params", moreUrl_or_params);
        return doForward(actionType, moreUrl_or_params);
    }

    /**
     * Do forward the action with the more URL parts and the the parameters on GET. <br>
     * This method is to other forward methods so normally you don't use directly from your action.
     * @param actionType The class type of action that it forwards to. (NotNull)
     * @param chain The chain of URL to build additional info on URL. (NotNull)
     * @return The URL string for forward containing GET parameters. (NotNull)
     */
    protected String doForward(Class<?> actionType, UrlChain chain) {
        assertArgumentNotNull("actionType", actionType);
        assertArgumentNotNull("chain", chain);
        return toActionUrl(actionType, false, chain);
    }

    // -----------------------------------------------------
    //                                          Chain Method
    //                                          ------------
    /**
     * Set up more URL parts as URL chain. <br>
     * The name and specification of this method is synchronized with {@link UrlChain#moreUrl()}.
     * @param urlParts The varying array of URL parts. (NotNull)
     * @return The created instance of URL chain. (NotNull)
     */
    protected UrlChain moreUrl(Object... urlParts) {
        assertArgumentNotNull("urlParts", urlParts);
        return createUrlChain().moreUrl(urlParts);
    }

    /**
     * Set up parameters on GET as URL chain. <br>
     * The name and specification of this method is synchronized with {@link UrlChain#params()}.
     * @param paramsOnGet The varying array of parameters on GET. (NotNull)
     * @return The created instance of URL chain. (NotNull)
     */
    protected UrlChain params(Object... paramsOnGet) {
        assertArgumentNotNull("paramsOnGet", paramsOnGet);
        return createUrlChain().params(paramsOnGet);
    }

    /**
     * Set up hash on URL as URL chain. <br>
     * The name and specification of this method is synchronized with {@link UrlChain#hash()}.
     * @param hashOnUrl The value of hash on URL. (NotNull)
     * @return The created instance of URL chain. (NotNull)
     */
    public UrlChain hash(Object hashOnUrl) {
        assertArgumentNotNull("hashOnUrl", hashOnUrl);
        return createUrlChain().hash(hashOnUrl);
    }

    protected UrlChain createUrlChain() {
        return new UrlChain(this);
    }

    // -----------------------------------------------------
    //                                     Adjustment Method
    //                                     -----------------
    /**
     * Handle the redirect URL to be MOVED_PERMANENTLY (301 redirect). <br>
     * Remove redirect mark and add header elements as 301 and return null.
     * <pre>
     * <span style="color: #3F7E5E">// e.g. /member/edit/</span>
     * return <span style="color: #FD4747">movedPermanently</span>(redirect(MemberEditAction.class));
     * </pre>
     * @param redirectUrl The redirect URL with redirect mark for SAStruts. (NotNull)
     * @return The returned URL for execute method of SAStruts. (NullAllowed)
     */
    protected String movedPermanently(String redirectUrl) {
        final String plainPath = actionResolver.removeRedirectMark(redirectUrl);
        responseManager.redirect301(plainPath); // set up headers for 301
        return null; // if 301, returns null with headers in SAStruts (might be overridden for action callback)
    }

    // -----------------------------------------------------
    //                                          URL Handling
    //                                          ------------
    /**
     * Convert to URL string to move the action. <br>
     * This method is to build URL string by manually so normally you don't use directly from your action.
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @param redirect Do you redirect to the action?
     * @param chain The chain of URL to build additional info on URL. (NotNull)
     * @return The URL string to move to the action. (NotNull)
     */
    protected String toActionUrl(Class<?> actionType, boolean redirect, UrlChain chain) {
        return actionResolver.toActionUrl(actionType, redirect, chain);
    }

    /**
     * Resolve the action URL from the class type of the action. <br>
     * This method is to build URL string by manually so normally you don't use directly from your action.
     * @param actionType The class type of action that it redirects to. (NotNull)
     * @return The basic URL string to move to the action. (NotNull)
     */
    protected String resolveActionPath(Class<?> actionType) {
        return actionResolver.resolveActionPath(actionType);
    }

    // ===================================================================================
    //                                                                            Response
    //                                                                            ========
    /**
     * Return response as JSON.
     * <pre>
     * public void index() {
     *     ...
     *     return asJson(bean);
     * }
     * </pre>
     * @param bean The bean object converted to JSON string. (NotNull)
     * @return The new-created bean for JSON response. (NotNull)
     */
    protected JsonResponse asJson(Object bean) {
        assertArgumentNotNull("bean", bean);
        return newJsonResponse(bean);
    }

    /**
     * New-create JSON response object.
     * @param bean The bean object converted to JSON string. (NotNull)
     * @return The new-created bean for JSON response. (NotNull)
     */
    protected JsonResponse newJsonResponse(Object bean) {
        assertArgumentNotNull("bean", bean);
        return new JsonResponse(bean);
    }

    /**
     * Return response as stream.
     * <pre>
     * e.g. simple (content-type is octet-stream or found by extension mapping)
     *  return asStream("classificationDefinitionMap.dfprop").stream(ins);
     * 
     * e.g. specify content-type
     *  return asStream("jflute.jpg").contentTypeJpeg().stream(ins);
     * </pre>
     * @param fileName The file name as data of the stream. (NotNull)
     * @return The new-created bean for XML response. (NotNull)
     */
    protected StreamResponse asStream(String fileName) {
        assertArgumentNotNull("fileName", fileName);
        return newStreamResponse(fileName);
    }

    /**
     * New-create stream response object.
     * @param fileName The file name as data of the stream. (NotNull)
     * @return The new-created bean for XML response. (NotNull)
     */
    protected StreamResponse newStreamResponse(String fileName) {
        return new StreamResponse(fileName);
    }

    /**
     * Return response as XML.
     * @param xmlStr The string of XML. (NotNull)
     * @return The new-created bean for XML response. (NotNull)
     */
    protected XmlResponse asXml(String xmlStr) {
        assertArgumentNotNull("xmlStr", xmlStr);
        return newXmlResponse(xmlStr);
    }

    /**
     * New-create XML response object.
     * @param xmlStr The string of XML. (NotNull)
     * @return The new-created bean for XML response. (NotNull)
     */
    protected XmlResponse newXmlResponse(String xmlStr) {
        return new XmlResponse(xmlStr);
    }

    // ===================================================================================
    //                                                                                JSON
    //                                                                                ====
    /**
     * Write JSON data to response as JavaScript content type.
     * @param source The source object to encode. (NotNull)
     * @deprecated use asJson()
     */
    protected void writeJsonResponse(Object source) {
        assertArgumentNotNull("source", source);
        responseManager.writeAsJson(convertToJson(source));
    }

    /**
     * Write JSONP data to response as JavaScript content type.
     * @param callback The function name of JSONP callback. (NotNull)
     * @param source The source object to encode. (NotNull)
     * @deprecated use asJson()
     */
    protected void writeJsonpResponse(String callback, Object source) {
        assertArgumentNotNull("callback", callback);
        assertArgumentNotNull("source", source);
        responseManager.writeAsJavaScript(callback + "(" + convertToJson(source) + ")");
    }

    /**
     * Parse the JSON string to bean.
     * @param <BEAN> The type of bean.
     * @param json The string of JSON to be parsed. (NotNull)
     * @param beanType The type of bean that has the JSON values. (NotNull)
     * @return The new-created bean that has the JSON values. (NotNull)
     */
    protected <BEAN> BEAN parseJson(String json, Class<BEAN> beanType) {
        assertArgumentNotNull("json", json);
        assertArgumentNotNull("beanType", beanType);
        return jsonManager.parseJson(json, beanType);
    }

    /**
     * Convert the source object to JSON string.
     * @param bean The instance of bean to encode. (NotNull)
     * @return The encoded JSON string. (NotNull)
     */
    protected String convertToJson(Object bean) {
        assertArgumentNotNull("bean", bean);
        return jsonManager.convertToJson(bean);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    /**
     * Assert that the object is not null.
     * @param variableName The name of assert-target variable. (NotNull)
     * @param value The value of argument. (NotNull)
     * @throws IllegalArgumentException When the value is null.
     */
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Assert that the get parameter exists.
     * @param param The value of parameter. (NotNull)
     * @throws GetParameterNotFoundException When the parameter is null or (trimmed) empty.
     */
    protected void assertGetParameterExists(Object param) {
        if (param == null) {
            String msg = "The get parameter should not be null.";
            throw new GetParameterNotFoundException(msg);
        }
        if (param instanceof String && ((String) param).trim().length() == 0) {
            String msg = "The get parameter should not be empty: [" + param + "]";
            throw new GetParameterNotFoundException(msg);
        }
    }
}
