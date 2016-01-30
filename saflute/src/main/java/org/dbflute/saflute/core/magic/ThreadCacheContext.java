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
package org.dbflute.saflute.core.magic;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.web.action.processor.ActionRequestProcessor;
import org.dbflute.saflute.web.task.interceptor.BatchTaskInterceptor;
import org.dbflute.util.DfCollectionUtil;

/**
 * The context of thread cache. <br>
 * You can cache your favorite objects in the thread to reduce performance cost.
 * However you should use this only when it works even if no cache.
 * I mean, your logic should not depend on the existence of this cache. <br>
 * This cache is cleared when action execute ends (in the request processor),
 * and is cleared when task execute of s2chronos ends.
 * So you must set {@link ActionRequestProcessor} in your web.xml,
 * and set {@link BatchTaskInterceptor} to your task classes in your customizer.dicon.
 * @author jflute
 */
public class ThreadCacheContext {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // -----------------------------------------------------
    //                                             Core Item
    //                                             ---------
    public static final String FW_REQUEST_PATH = "fw:requestPath";
    public static final String FW_ENTRY_METHOD = "fw:entryMethod";
    public static final String FW_USER_BEAN = "fw:userBean";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The thread-local for this. */
    private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>();

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    /**
     * Initialize this cache to be available. <br>
     * You can use this cache after this initialization. <br>
     * Don't forget to clear the cache in finally clause like this:
     * <pre>
     * try {
     *     {@link ThreadCacheContext#initialize()};
     *     ...
     * } finally {
     *     {@link ThreadCacheContext#clear()};
     * }
     * </pre>
     */
    public static void initialize() {
        clear();
        threadLocal.set(new HashMap<String, Object>());
    }

    // ===================================================================================
    //                                                                      Cache Handling
    //                                                                      ==============
    /**
     * Get the value of the object by the key.
     * @param key The key of the object. (NotNull)
     * @return The value of the object. (NullAllowed)
     */
    public static Object getObject(String key) {
        if (!exists()) {
            throwThreadCacheNotInitializedException(key);
        }
        return threadLocal.get().get(key);
    }

    /**
     * Set the value of the object.
     * @param key The key of the object. (NotNull)
     * @param value The value of the object. (NullAllowed)
     */
    public static void setObject(String key, Object value) {
        if (!exists()) {
            throwThreadCacheNotInitializedException(key);
        }
        threadLocal.get().put(key, value);
    }

    /**
     * Remove the value of the object from the cache.
     * @param key The key of the object. (NotNull)
     * @return The removed value. (NullAllowed)
     */
    public static Object removeObject(String key) {
        if (!exists()) {
            throwThreadCacheNotInitializedException(key);
        }
        return threadLocal.get().remove(key);
    }

    /**
     * Determine the object as boolean.
     * @param key The key of the object. (NotNull)
     * @return The determination, true or false. (true if the object exists and true)
     */
    public static boolean determineObject(String key) {
        if (!exists()) {
            throwThreadCacheNotInitializedException(key);
        }
        final Object obj = threadLocal.get().get(key);
        return obj != null && (boolean) obj;
    }

    public static boolean exists() {
        return (threadLocal.get() != null);
    }

    public static void clear() {
        threadLocal.set(null);
    }

    protected static void throwThreadCacheNotInitializedException(String key) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The thread cache was not initialized yet.");
        br.addItem("Advice");
        br.addElement("You need to initialize thread cache context before you use it.");
        br.addElement("However you have got to be available if you have correct settings of the framework.");
        br.addElement("(For example, the request processor or batch interceptor initialize it)");
        br.addElement("Check the framework settings.");
        br.addItem("Specified Key");
        br.addElement(key);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                           Framework
    //                                                                           =========
    public static Map<String, Object> getReadOnlyCacheMap() { // for framework
        if (!exists()) {
            return DfCollectionUtil.emptyMap();
        }
        return Collections.unmodifiableMap(threadLocal.get());
    }

    // -----------------------------------------------------
    //                                             Core Item
    //                                             ---------
    public static String findRequestPath() {
        return exists() ? (String) getObject(FW_REQUEST_PATH) : null;
    }

    public static void registerRequestPath(String requestPath) {
        setObject(FW_REQUEST_PATH, requestPath);
    }

    public static Method findEntryMethod() {
        return exists() ? (Method) getObject(FW_ENTRY_METHOD) : null;
    }

    public static void registerEntryMethod(Method entryMethod) {
        setObject(FW_ENTRY_METHOD, entryMethod);
    }

    public static Object findUserBean() { // object not to depend on web
        return exists() ? getObject(FW_USER_BEAN) : null;
    }

    public static void registerUserBean(Object userBean) {
        setObject(FW_USER_BEAN, userBean);
    }
}
