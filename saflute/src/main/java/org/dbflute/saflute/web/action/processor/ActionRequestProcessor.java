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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.upload.MultipartRequestHandler;
import org.apache.struts.util.RequestUtils;
import org.dbflute.hook.AccessContext;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.json.JsonManager;
import org.dbflute.saflute.core.magic.ThreadCacheContext;
import org.dbflute.saflute.core.magic.TransactionTimeContext;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.db.dbflute.accesscontext.PreparedAccessContext;
import org.dbflute.saflute.web.action.OptionalActionDirection;
import org.dbflute.saflute.web.action.api.JsonParameter;
import org.dbflute.saflute.web.action.processor.exception.ActionCreateFailureException;
import org.dbflute.saflute.web.action.response.ActionResponseHandler;
import org.dbflute.saflute.web.servlet.filter.RequestLoggingFilter;
import org.dbflute.util.DfReflectionUtil;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.IllegalPropertyRuntimeException;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.ModifierUtil;
import org.seasar.struts.action.ActionFormWrapper;
import org.seasar.struts.action.ActionWrapper;
import org.seasar.struts.action.S2RequestProcessor;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.util.RequestUtil;
import org.seasar.struts.util.S2ExecuteConfigUtil;

/**
 * The processor of action request. <br>
 * This extends Seasar's processor e.g. to create the original action wrapper.
 * @author jflute
 */
public class ActionRequestProcessor extends S2RequestProcessor {

    // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // *Override for small adjustment (almost copied from Struts or SAStruts)
    // - - - - - - - - - -/

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log LOG = LogFactory.getLog(ActionRequestProcessor.class);
    protected static final String CACHE_KEY_EXECUTE_METHOD = "requestProcessor.executeMethod";
    protected static final String CACHE_KEY_DECODED_PROPERTY_MAP = "requestProcessor.decodedPropertyMap";
    protected static final String CACHE_KEY_URL_PARAM_NAMES_CACHED_SET = "requestProcessor.urlParamNames.cachedSet";
    protected static final String CACHE_KEY_URL_PARAM_NAMES_UNIQUE_METHOD = "requestProcessor.urlParamNames.uniqueMethod";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /**
     * The cache of assistant director, which can be lazy-loaded when you get it.
     * Don't use these variables directly, you should use the getter.
     * (NotNull: after lazy-load)
     */
    protected FwAssistantDirector cachedAssistantDirector;

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // initializing and clearing thread cache here
        // so you can use thread cache in your action execute (contains other action process)
        // *moved from action perform to here for framework use
        final boolean exists = ThreadCacheContext.exists();
        try {
            // it inherits existing cache when nested call e.g. forward
            if (!exists) {
                ThreadCacheContext.initialize();
            }
            super.process(request, response);
        } finally {
            if (!exists) {
                ThreadCacheContext.clear();
            }
        }
    }

    // ===================================================================================
    //                                                                              Locale
    //                                                                              ======
    @Override
    protected void processLocale(HttpServletRequest request, HttpServletResponse response) {
        // moved to action wrapper (do nothing here)
        //final RequestManager manager = getRequestManager();
        //manager.resolveUserLocale();
        //manager.resolveUserTimeZone();
    }

    // ===================================================================================
    //                                                                            Populate
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    @Override
    protected void processPopulate(HttpServletRequest request, HttpServletResponse response, ActionForm form, ActionMapping mapping)
            throws ServletException {
        // copied from super class for small adjustment of exception handling
        if (form == null) {
            return;
        }
        form.setServlet(servlet);
        final String contentType = request.getContentType();
        final String method = request.getMethod();
        form.setMultipartRequestHandler(null);
        MultipartRequestHandler multipartHandler = null;
        if (contentType != null && contentType.startsWith("multipart/form-data") && method.equalsIgnoreCase("POST")) {
            multipartHandler = getMultipartHandler(mapping.getMultipartClass());
            if (multipartHandler != null) {
                multipartHandler.setServlet(servlet);
                multipartHandler.setMapping(mapping);
                multipartHandler.handleRequest(request);
                final Boolean maxLengthExceeded = (Boolean) request.getAttribute(MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED);
                if ((maxLengthExceeded != null) && (maxLengthExceeded.booleanValue())) {
                    form.setMultipartRequestHandler(multipartHandler);
                    processExecuteConfig(request, response, mapping);
                    return;
                }
                SingletonS2ContainerFactory.getContainer().getExternalContext().setRequest(request);
            }
        }
        processExecuteConfig(request, response, mapping);
        form.reset(mapping, request);
        final Map<String, Object> params = getAllParameters(request, multipartHandler);
        final S2ActionMapping actionMapping = (S2ActionMapping) mapping;
        for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
            final String name = i.next();
            try {
                setProperty(actionMapping.getActionForm(), name, params.get(name));
            } catch (Throwable t) {
                handleIllegalPropertyPopulateException(actionMapping, name, t); // adjustment here
            }
        }
    }

    protected void handleIllegalPropertyPopulateException(S2ActionMapping actionMapping, String name, Throwable t) throws ServletException {
        if (isRequest404NotFoundException(t)) { // for indexed property check
            throw new ServletException(t);
        }
        // SAStruts default here
        throw new IllegalPropertyRuntimeException(actionMapping.getActionFormBeanDesc().getBeanClass(), name, t);
    }

    protected boolean isRequest404NotFoundException(Throwable cause) {
        return cause instanceof RequestLoggingFilter.Request404NotFoundException;
    }

    @Override
    protected void processExecuteConfig(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) {
        super.processExecuteConfig(request, response, mapping);
        cacheExecuteMethod(); // used by e.g. urlParamNames handling
    }

    protected void cacheExecuteMethod() {
        final S2ExecuteConfig executeConfig = S2ExecuteConfigUtil.getExecuteConfig();
        ThreadCacheContext.setObject(CACHE_KEY_EXECUTE_METHOD, executeConfig.getMethod());
    }

    // -----------------------------------------------------
    //                                       Simple Property
    //                                       ---------------
    @Override
    protected void setSimpleProperty(Object bean, String name, Object value) { // override basically for ID parameter
        try {
            final Object filteredValue = filterPropertyValue(bean, name, value); // for escaped slash problem
            doSetSimpleProperty(bean, name, filteredValue);
        } catch (IllegalPropertyRuntimeException e) {
            if (!(e.getCause() instanceof NumberFormatException)) {
                throw e;
            }
            // here: non-number GET or URL parameter but number type property
            // suppress easy 500 error by non-number GET or URL parameter
            //  (o): /edit/123/
            //  (x): /edit/abc/ *this case
            // you can accept ID on URL parameter as Long type in ActionForm
            final Object dispValue;
            if (value instanceof Object[]) {
                dispValue = Arrays.asList((Object[]) value).toString();
            } else {
                dispValue = value;
            }
            String beanExp = bean != null ? bean.getClass().getName() : null; // null check just in case
            String msg = "The property value cannot be number: " + beanExp + ", name=" + name + ", value=" + dispValue;
            throwRequest404NotFoundException(msg);
        }
    }

    protected Object filterPropertyValue(Object bean, String name, Object value) {
        final Object realValue;
        if (value instanceof String) {
            realValue = decodeEscapedProperty(bean, name, (String) value);
        } else if (value instanceof String[]) {
            final String[] strAry = (String[]) value;
            for (int i = 0; i < strAry.length; i++) {
                strAry[i] = decodeEscapedProperty(bean, name, strAry[i]);
            }
            realValue = strAry;
        } else {
            realValue = value;
        }
        return realValue;
    }

    // -----------------------------------------------------
    //                                       Decode Property
    //                                       ---------------
    protected String decodeEscapedProperty(Object bean, String name, String value) {
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // <decoding story>
        // e.g. /foo/aaa%252fbbb%2fccc/ is requested
        // routing to -> /foo.do&bar=aaa%25252fbbb%252fccc
        // request.getParameterValues("bar") -> aaa%252fbbb%2fccc
        // set value to form -> aaa%2fbbb/ccc *only one-time decoding
        // - - - - - - - - - -/
        if (value == null || !isUrlParameterProperty(bean, name, value)) {
            return value;
        }
        // URL parameter properties only here
        // (basically no array property type so the name can be key)
        final String cacheKey = CACHE_KEY_DECODED_PROPERTY_MAP;
        @SuppressWarnings("unchecked")
        Map<String, String> decodedPropertyMap = (Map<String, String>) ThreadCacheContext.getObject(cacheKey);
        if (decodedPropertyMap == null) {
            decodedPropertyMap = new HashMap<String, String>();
            ThreadCacheContext.setObject(cacheKey, decodedPropertyMap);
        }
        final String cached = decodedPropertyMap.get(name);
        if (cached != null) {
            // GET parameters of nested forward are decoded in Tomcat
            // so it suppresses duplicate decoding here
            if (LOG.isDebugEnabled() && isDecodeDebugTarget(value)) {
                LOG.debug("Decoded property: " + name + " cached:(" + cached + ") switched:(" + value + ")");
            }
            return cached;
        }
        final String basic = decodeEscapedBasicCharacter(bean, name, value); // basic decode (NotNull)
        final String provided = decodeEscapedVariousCharacter(bean, name, basic); // provided decode (NullAllowed)
        final String result = provided != null ? provided : basic;
        if (LOG.isDebugEnabled() && isDecodeDebugTarget(value)) {
            LOG.debug("Decoded property: " + name + " (" + value + " to " + result + ")");
        }
        decodedPropertyMap.put(name, result);
        return result;
    }

    protected boolean isUrlParameterProperty(Object bean, String name, String value) {
        // URL parameter might have encoded characters
        // (requestPath used for routing is not decoded)
        final Set<String> urlParamNameSet = getUrlParamNameSet();
        return urlParamNameSet.contains(name);
    }

    @SuppressWarnings("unchecked")
    protected Set<String> getUrlParamNameSet() {
        final String executeMethodKey = CACHE_KEY_EXECUTE_METHOD;
        final String cacheSetKey = CACHE_KEY_URL_PARAM_NAMES_CACHED_SET;
        final String uniqueMethodKey = CACHE_KEY_URL_PARAM_NAMES_UNIQUE_METHOD;
        Set<String> existingSet = null;
        final Method executeMethod = (Method) ThreadCacheContext.getObject(executeMethodKey);
        final Object cachedObj = ThreadCacheContext.getObject(cacheSetKey);
        if (cachedObj != null && cachedObj instanceof Set<?>) { // found cached set
            existingSet = (Set<String>) cachedObj;
            final Method uniqueMethod = (Method) ThreadCacheContext.getObject(uniqueMethodKey);
            if (executeMethod.equals(uniqueMethod)) { // cached and same execute-method
                return existingSet;
            }
        }
        // unfortunately the configuration class does not have the getter method for urlParamNames
        // so it uses reflection to get the value of the protected field with thread cache
        // ...
        // after that, extends S2ExecuteConfig so you can get it without reflection
        // but keep this logic for now because it is very complex logic (2014/08/21)
        final List<String> extractedList = extractUrlParamNamesFromConfig();
        final Set<String> cachedSet;
        if (existingSet != null) { // existing cached set but execute-method has been changed
            cachedSet = existingSet; // merged with existing names
        } else { // first access (first cache)
            cachedSet = new HashSet<String>(extractedList);
        }
        cachedSet.addAll(extractedList);
        ThreadCacheContext.setObject(uniqueMethodKey, executeMethod);
        ThreadCacheContext.setObject(cacheSetKey, cachedSet);
        return cachedSet;
    }

    protected List<String> extractUrlParamNamesFromConfig() {
        final S2ExecuteConfig executeConfig = S2ExecuteConfigUtil.getExecuteConfig();
        final Field namesField = DfReflectionUtil.getWholeField(executeConfig.getClass(), "urlParamNames");
        @SuppressWarnings("unchecked")
        final List<String> urlParamNames = (List<String>) DfReflectionUtil.getValueForcedly(namesField, executeConfig);
        return urlParamNames;
    }

    protected boolean isDecodeDebugTarget(String value) {
        return value.contains("%") || value.contains("+"); // might be decode target
    }

    protected String decodeEscapedBasicCharacter(Object bean, String name, String value) {
        return urlDecode(value);
    }

    protected String urlDecode(String value) { // like Seasar's URLEncoderUtil
        final String encoding = RequestUtil.getRequest().getCharacterEncoding();
        try {
            return URLDecoder.decode(value, encoding);
        } catch (UnsupportedEncodingException e) {
            String msg = "Unsupported encoding: value=" + value + ", encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        }
    }

    protected String decodeEscapedVariousCharacter(Object bean, String name, String value) {
        final OptionalActionDirection direction = getAssistantDirector().assistOptionalActionDirection();
        final ActionAdjustmentProvider adjustmentProvider = direction.assistActionAdjustmentProvider();
        return adjustmentProvider.decodeUrlParameterPropertyValue(bean, name, value);
    }

    // -----------------------------------------------------
    //                             Actually Setting Property
    //                             -------------------------
    protected void doSetSimpleProperty(Object bean, String name, Object value) {
        // copied from super.setSimpleProperty() and adjust a little
        if (bean instanceof Map) {
            setMapProperty((Map<?, ?>) bean, name, value);
            return;
        }
        final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(bean.getClass());
        if (!beanDesc.hasPropertyDesc(name)) {
            return;
        }
        final PropertyDesc pd = beanDesc.getPropertyDesc(name);
        if (!pd.isWritable()) {
            return;
        }
        final Class<?> propertyType = pd.getPropertyType();
        if (propertyType.isArray()) {
            pd.setValue(bean, value);
        } else if (List.class.isAssignableFrom(propertyType)) {
            final boolean abstractProperty = ModifierUtil.isAbstract(propertyType);
            final List<String> valueList;
            if (abstractProperty) {
                valueList = new ArrayList<String>();
            } else {
                @SuppressWarnings("unchecked")
                final List<String> cast = (List<String>) ClassUtil.newInstance(propertyType);
                valueList = cast;
            }
            valueList.addAll(Arrays.asList((String[]) value));
            pd.setValue(bean, valueList);
        } else if (value == null) {
            pd.setValue(bean, null);
        } else if (value instanceof String[]) { // almost parameters are here
            final String[] values = (String[]) value;
            final String realValue = values.length > 0 ? values[0] : null;
            if (isJsonParameterProperty(pd)) {
                final Object jsonObj = parseJsonParameter(bean, name, realValue, propertyType);
                pd.setValue(bean, jsonObj);
            } else { // normally here
                pd.setValue(bean, realValue);
            }
        } else {
            pd.setValue(bean, value);
        }
    }

    // -----------------------------------------------------
    //                                        JSON Parameter
    //                                        --------------
    protected boolean isJsonParameterProperty(PropertyDesc pd) {
        final Class<JsonParameter> annoType = JsonParameter.class;
        final Field field = pd.getField();
        if (field != null && field.getAnnotation(annoType) != null) {
            return true;
        }
        if (field != null && !ModifierUtil.isPublic(field)) { // not public field
            if (pd.hasReadMethod()) {
                final Method readMethod = pd.getReadMethod();
                if (readMethod != null && readMethod.getAnnotation(annoType) != null) {
                    return true;
                }
            }
            if (pd.hasWriteMethod()) {
                final Method writeMethod = pd.getWriteMethod();
                if (writeMethod != null && writeMethod.getAnnotation(annoType) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected Object parseJsonParameter(Object bean, String name, String json, Class<?> propertyType) {
        final JsonManager jsonManager = ContainerUtil.getComponent(JsonManager.class);
        try {
            return jsonManager.parseJson(json, propertyType);
        } catch (RuntimeException e) {
            throwJsonPropertyParseFailureException(bean, name, json, propertyType, e);
            return null; // unreachable
        }
    }

    protected void throwJsonPropertyParseFailureException(Object bean, String name, String json, Class<?> propertyType, RuntimeException e) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Cannot parse json of the property:");
        sb.append("\n[JsonProperty Parse Failure]");
        sb.append("\n").append(bean.getClass().getSimpleName()).append("#").append(name);
        sb.append(" (").append(propertyType.getSimpleName()).append(")");
        sb.append("\n").append(json);
        sb.append("\n").append(e.getClass().getName()).append("\n").append(e.getMessage());
        throwRequest404NotFoundException(sb.toString());
    }

    // -----------------------------------------------------
    //                                           Parse Index
    //                                           -----------
    @Override
    protected IndexParsedResult parseIndex(String name) { // override for checking indexed property
        final IndexParsedResult parseResult;
        try {
            parseResult = super.parseIndex(name);
        } catch (NumberFormatException e) {
            throwIndexedPropertyNonNumberIndexException(name, e);
            return null; // unreachable
        }
        checkIndexedPropertySize(name, parseResult);
        return parseResult;
    }

    protected void throwIndexedPropertyNonNumberIndexException(String name, NumberFormatException e) {
        String msg = "Non number index of the indexed property: name=" + name + "\n" + e.getMessage();
        throwRequest404NotFoundException(msg);
    }

    protected void checkIndexedPropertySize(String name, IndexParsedResult parseResult) {
        final int[] indexes = parseResult.indexes;
        if (indexes.length == 0) {
            return;
        }
        final int indexedPropertySizeLimit = getIndexedPropertySizeLimit();
        for (int index : indexes) {
            if (index < 0) {
                throwIndexedPropertyMinusIndexException(name, index);
            }
            if (index > indexedPropertySizeLimit) {
                throwIndexedPropertySizeOverException(name, index);
            }
        }
    }

    protected int getIndexedPropertySizeLimit() {
        final FwAssistantDirector assistantDirector = getAssistantDirector();
        final OptionalActionDirection direction = assistantDirector.assistOptionalActionDirection();
        final ActionAdjustmentProvider provider = direction.assistActionAdjustmentProvider();
        return provider.provideIndexedPropertySizeLimit();
    }

    protected void throwIndexedPropertyMinusIndexException(String name, int index) {
        String msg = "Minus index of the indexed property: name=" + name;
        throwRequest404NotFoundException(msg);
    }

    protected void throwIndexedPropertySizeOverException(String name, int index) {
        String msg = "Too large size of the indexed property: name=" + name + ", index=" + index;
        throwRequest404NotFoundException(msg);
    }

    protected void throwRequest404NotFoundException(String msg) {
        throw new RequestLoggingFilter.Request404NotFoundException(msg);
    }

    // ===================================================================================
    //                                                                       Action Create
    //                                                                       =============
    @Override
    protected Action processActionCreate(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping)
            throws IOException {
        final Action action;
        try {
            action = createActionWrapper((S2ActionMapping) mapping);
        } catch (Exception e) {
            final String process = "actionCreate";
            final String mappingPath = mapping.getPath();
            final String msg = getInternal().getMessage(process, mappingPath);
            // *switch super's process to throw original exception caught by logging filter
            //log.error(msg, e);
            //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
            //return null;
            throw new ActionCreateFailureException(msg, e);
        }
        action.setServlet(servlet);
        return action;
    }

    protected ActionWrapper createActionWrapper(S2ActionMapping mapping) {
        final ActionMappingWrapper wrappedMapping = wrapActionMapping(mapping);
        final GodHandableActionWrapper wrapper = newGodHandableActionWrapper(wrappedMapping);
        final OptionalActionDirection direction = getAssistantDirector().assistOptionalActionDirection();
        final ActionResponseHandler responseHandler = direction.assistActionResponseHandler();
        if (responseHandler != null) {
            wrapper.setActionResopnseHandler(responseHandler);
        }
        return wrapper;
    }

    protected ActionMappingWrapper wrapActionMapping(S2ActionMapping mapping) {
        final OptionalActionDirection direction = getAssistantDirector().assistOptionalActionDirection();
        final ActionAdjustmentProvider adjustmentProvider = direction.assistActionAdjustmentProvider();
        return new ActionMappingWrapper(mapping, adjustmentProvider);
    }

    protected GodHandableActionWrapper newGodHandableActionWrapper(ActionMappingWrapper wrappedMapping) {
        return new GodHandableActionWrapper(wrappedMapping);
    }

    // ===================================================================================
    //                                                                      Action Perform
    //                                                                      ==============
    @Override
    protected ActionForward processActionPerform(HttpServletRequest request, HttpServletResponse response, Action action, ActionForm form,
            ActionMapping mapping) throws IOException, ServletException {
        try {
            checkRequiredUrlParameter(action, form, mapping);
            return super.processActionPerform(request, response, action, form, mapping);
        } finally {
            // clear various contexts just in case
            // *thread cache initializing and clearing are moved to the process() method
            TransactionTimeContext.clear();
            PreparedAccessContext.clearAccessContextOnThread();
            AccessContext.clearAccessContextOnThread();
        }
    }

    protected void checkRequiredUrlParameter(Action action, ActionForm form, ActionMapping mapping) {
        final S2ExecuteConfig plainConfig = S2ExecuteConfigUtil.getExecuteConfig();
        // extended configuration object created by romantic action customizer
        if (!(plainConfig instanceof ActionExecuteConfig)) { // just in case
            return;
        }
        final ActionExecuteConfig extendedConfig = (ActionExecuteConfig) plainConfig;
        final Set<String> requiredSet = extendedConfig.getUrlParamRequiredSet();
        if (requiredSet.isEmpty()) { // no urlPattern or no required element
            return;
        }
        // SAStruts always wraps Struts action form
        if (!(form instanceof ActionFormWrapper)) { // just in case
            return;
        }
        final ActionFormWrapper wrapper = (ActionFormWrapper) form;
        for (String name : requiredSet) {
            final Object value = wrapper.get(name); // exception if no property
            if (value != null) {
                continue;
            }
            // e.g. expected /edit/1/ but /edit/ => value is null
            String actionExp = action != null ? action.getClass().getName() : null; // null check just in case
            String msg = "The required property was not found: " + actionExp + ", name=" + name + ", value=" + value;
            throwRequest404NotFoundException(msg);
        }
    }

    // ===================================================================================
    //                                                                           Exception
    //                                                                           =========
    @Override
    protected ActionForward processException(HttpServletRequest request, HttpServletResponse response, Exception exception,
            ActionForm form, ActionMapping mapping) throws IOException, ServletException {
        // Is there a defined handler for this exception?
        final ExceptionConfig config = mapping.findException(exception.getClass());
        if (config == null) {
            // *comment out because the exception is cached by logging filter
            //log.warn(getInternal().getMessage("unhandledException", exception.getClass()));
            if (exception instanceof IOException) {
                throw (IOException) exception;
            } else if (exception instanceof ServletException) {
                throw (ServletException) exception;
            } else {
                throw new ServletException(exception);
            }
        }

        // Use the configured exception handling
        try {
            final ExceptionHandler handler = (ExceptionHandler) RequestUtils.applicationInstance(config.getHandler());
            return (handler.execute(exception, config, mapping, form, request, response));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // ===================================================================================
    //                                                                       ForwardConfig
    //                                                                       =============
    @Override
    protected void processForwardConfig(HttpServletRequest request, HttpServletResponse response, ForwardConfig forward)
            throws IOException, ServletException { // basically copied from super class
        if (forward == null) {
            return;
        }
        // show forward in action wrapper instead
        //if (log.isDebugEnabled()) {
        //  log.debug("processForwardConfig(" + forward + ")");
        //}
        final String forwardPath = forward.getPath();
        String uri = null;

        // paths not starting with / should be passed through without any processing
        // (ie. they're absolute)
        if (forwardPath.startsWith("/")) {
            uri = RequestUtils.forwardURL(request, forward, null); // get module relative uri
        } else {
            uri = forwardPath;
        }
        if (forward.getRedirect()) {
            // only prepend context path for relative uri
            if (uri.startsWith("/")) {
                uri = request.getContextPath() + uri;
            }
            response.sendRedirect(response.encodeRedirectURL(uri));
        } else {
            doForward(uri, request, response);
        }
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
