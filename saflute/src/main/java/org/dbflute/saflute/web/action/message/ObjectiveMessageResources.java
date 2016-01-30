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
package org.dbflute.saflute.web.action.message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.struts.util.MessageResourcesFactory;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.saflute.core.direction.FwAssistantDirector;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.OptionalActionDirection;
import org.dbflute.saflute.web.action.message.exception.MessageLabelByLabelParameterNotFoundException;
import org.dbflute.saflute.web.action.message.exception.MessageLabelByLabelVariableInfinityLoopException;
import org.dbflute.saflute.web.action.message.exception.MessageLabelByLabelVariableInvalidKeyException;
import org.dbflute.saflute.web.action.message.exception.MessageLabelByLabelVariableNotFoundException;
import org.dbflute.saflute.web.servlet.OptionalServletDirection;
import org.dbflute.saflute.web.servlet.request.UserLocaleProcessProvider;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl.ScopeInfo;
import org.seasar.framework.message.MessageResourceBundle;
import org.seasar.framework.message.MessageResourceBundleFactory;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.struts.util.S2PropertyMessageResources;

/**
 * @author jflute
 */
public class ObjectiveMessageResources extends S2PropertyMessageResources {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    /** The key prefix for errors of message resources, which contains dot at last. */
    public static final String ERRORS_KEY_PREFIX = "errors.";

    /** The key prefix for labels, which contains dot at last. */
    public static final String LABELS_KEY_PREFIX = "labels.";

    /** The key prefix for messages of message resources, which contains dot at last. */
    public static final String MESSAGES_KEY_PREFIX = "messages.";

    /** The extension for properties, which contains dot at front. */
    public static final String PROPERTIES_EXT = ".properties";

    /** The begin mark of label variable. */
    public static final String LABEL_VARIABLE_BEGIN_MARK = "@[";

    /** The end mark of label variable. */
    public static final String LABEL_VARIABLE_END_MARK = "]";

    /** The cache map of bundle. The string key is message (bundle) name (NotNull) */
    protected static final Map<String, Map<Locale, MessageResourceBundle>> bundleCacheMap = newConcurrentHashMap();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /**
     * The cache of assistant director, which can be lazy-loaded when you get it.
     * Don't use these variables directly, you should use the getter.
     * (NotNull: after lazy-load)
     * */
    protected FwAssistantDirector cachedAssistantDirector;

    /**
     * The cache of domain message name, which can be lazy-loaded when you get it. <br>
     * Don't use these variables directly, you should use the getter.
     * e.g. admin_message (NotNull: after lazy-load)
     */
    protected String cachedDomainMessageName;

    /**
     * The cache of message name list for extends, which can be lazy-loaded when you get it.
     * Don't use these variables directly, you should use the getter.
     * e.g. list:{project_message ; common_message} (NotNull: after lazy-load)
     */
    protected List<String> cachedExtendsMessageNameList;

    /**
     * The cache of default locale when no specified, which can be lazy-loaded when you get it. <br>
     * Don't use these variables directly, you should use the getter.
     */
    protected Locale cachedDefaultLocale;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ObjectiveMessageResources(MessageResourcesFactory factory, String config) {
        super(factory, config);
    }

    // ===================================================================================
    //                                                                    Override Message
    //                                                                    ================
    @Override
    public String getMessage(Locale locale, String key) {
        prepareDisposable();
        return doGetMessage(resolveActualUsedLocale(locale), key);
    }

    protected String doGetMessage(Locale locale, String key) {
        // almost same as super's (seasar's) process
        // only changed is how to get bundle
        final MessageResourceBundle bundle = getBundle(locale);
        final String message = bundle.get(key);
        final Set<String> callerKeySet = createCallerKeySet();
        return resolveLabelVariableMessage(locale, key, message, callerKeySet); // also resolve label variables
    }

    @Override
    public String getMessage(Locale locale, String key, Object[] args) {
        prepareDisposable();
        return doGetMessage(resolveActualUsedLocale(locale), key, args);
    }

    protected String doGetMessage(Locale locale, String key, Object[] args) {
        final List<Object> resolvedList = resolveLabelParameter(locale, key, args);
        final String message = super.getMessage(locale, key, resolvedList.toArray());
        final Set<String> callerKeySet = createCallerKeySet();
        return resolveLabelVariableMessage(locale, key, message, callerKeySet);
    }

    protected void prepareDisposable() {
        if (!initialized) {
            initialize();
        }
    }

    protected Locale resolveActualUsedLocale(Locale locale) {
        return locale != null ? locale : getDefaultLocale();
    }

    protected HashSet<String> createCallerKeySet() {
        return new LinkedHashSet<String>(4); // order for exception message
    }

    // ===================================================================================
    //                                                                    Extends Handling
    //                                                                    ================
    /**
     * Get the bundle by the locale and message names from the assistant director. <br>
     * Returned bundle has merged properties for domain and extends messages like this:
     * <pre>
     * e.g. domain_message extends common_message, locale = ja
     * common-ex3                 : common_message.properties *last search
     *  |-domain-ex3              : domain_message.properties
     *    |-common-ex2            : common_message_ja_JP_xx.properties
     *      |-domain-ex2          : domain_message_ja_JP_xx.properties
     *        |-common-ex1        : common_message_ja_JP.properties
     *          |-domain-ex1      : domain_message_ja_JP.properties
     *            |-common-root   : common_message_ja.properties
     *              |-domain-root : domain_message_ja.properties *first search
     * </pre>
     * @param locale The locale of current request. (NullAllowed: when system default locale)
     * @return The found bundle that has extends hierarchy. (NotNull)
     */
    protected MessageResourceBundle getBundle(Locale locale) {
        return getBundleResolvedExtends(getDomainMessageName(), getExtendsMessageNameList(), locale);
    }

    /**
     * Resolve the extends bundle, basically called by {@link #getBundle(Locale)}. <br>
     * Returned bundle has merged properties for domain and extends messages. <br>
     * You can get your message by normal way.
     * @param domainMessageName The message name for domain. (NotNull)
     * @param extendsNameList The list of extends-message name. (NotNull, EmptyAllowed)
     * @param locale The locale of current request. (NullAllowed: when system default locale)
     * @return The found bundle that has extends hierarchy. (NotNull)
     */
    protected MessageResourceBundle getBundleResolvedExtends(String domainMessageName, List<String> extendsNameList,
            Locale locale) {
        final MessageResourceBundle domainBundle = findBundleSimply(domainMessageName, locale);
        if (extendsNameList.isEmpty()) { // no extends, no logic
            return domainBundle;
        }
        if (isAlreadyExtends(domainBundle)) { // means the bundle is cached
            return domainBundle;
        }
        synchronized (this) { // synchronize to set up
            if (isAlreadyExtends(domainBundle)) {
                return domainBundle;
            }
            // set up extends references to the domain bundle specified as argument
            // so the bundle has been resolved extends after calling
            setupExtendsReferences(domainMessageName, extendsNameList, locale, domainBundle);
        }
        return domainBundle;
    }

    /**
     * Find The bundle simply (without extends handling), that may be cached. <br>
     * Returned bundle may have language, country, variant and default language's properties
     * for the message name and locale, as hierarchy like this:
     * <pre>
     * e.g. messageName = foo_message, locale = ja
     * parent3      : foo_message.properties *last search
     *  |-parent2   : foo_message_ja_JP_xx.properties
     *    |-parent1 : foo_message_ja_JP.properties
     *      |-root  : foo_message_ja.properties *first search
     *
     * MessageResourceBundle rootBundle = findBundleSimply("foo_message", locale);
     * MessageResourceBundle parent1 = rootBundle.getParent();
     * MessageResourceBundle parent2 = parent1.getParent();
     * MessageResourceBundle parent3 = parent2.getParent();
     * ...
     * </pre>
     * @param messageName The message name for the bundle. (NotNull)
     * @param locale The locale of current request. (NotNull)
     * @return The bundle that contains properties defined at specified message name.
     */
    protected MessageResourceBundle findBundleSimply(String messageName, Locale locale) {
        final Map<Locale, MessageResourceBundle> cachedMessageMap = bundleCacheMap.get(messageName);
        if (cachedMessageMap != null) {
            final MessageResourceBundle cachedBundle = cachedMessageMap.get(locale);
            if (cachedBundle != null) {
                return cachedBundle;
            }
        }
        synchronized (bundleCacheMap) {
            Map<Locale, MessageResourceBundle> localeKeyMap = bundleCacheMap.get(messageName);
            if (localeKeyMap != null) {
                final MessageResourceBundle retryBundle = localeKeyMap.get(locale);
                if (retryBundle != null) {
                    return retryBundle;
                }
            } else {
                localeKeyMap = newConcurrentHashMap(); // concurrent just in case
                bundleCacheMap.put(messageName, localeKeyMap);
            }
            // our hope would be that it has strict cache
            // because this cache is top-resource-driven cache
            // e.g. duplicate instance of default language bundle
            final MessageResourceBundle loadedBundle = loadBundle(messageName, locale);
            localeKeyMap.put(locale, loadedBundle);
        }
        return bundleCacheMap.get(messageName).get(locale);
    }

    protected MessageResourceBundle loadBundle(String messageName, Locale locale) {
        // you should not use MessageResourceBundleFactory directly
        // because logics here use the factory as simple utilities
        // but your operation to the factory directly have influences logics here
        final MessageResourceBundle bundle = MessageResourceBundleFactory.getBundle(messageName, locale);
        MessageResourceBundleFactory.clear();
        return bundle;
    }

    /**
     * Does the domain bundle already have extends handling? <br>
     * It returns true if the bundle has a parent instance of {@link MessageResourceBundleWrapper}.
     * @param domainBundle The bundle for domain for determination. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isAlreadyExtends(MessageResourceBundle domainBundle) {
        MessageResourceBundle currentBundle = domainBundle;
        boolean found = false;
        while (true) {
            MessageResourceBundle parentBundle = currentBundle.getParent();
            if (parentBundle == null) {
                break;
            }
            if (parentBundle instanceof MessageResourceBundleWrapper) {
                found = true;
                break;
            }
            currentBundle = parentBundle;
        }
        return found;
    }

    /**
     * Set up extends references to the domain bundle. <br>
     * @param domainMessageName The message name for domain properties. (NotNull)
     * @param extendsNameList The list of message name for extends properties. The first element is first extends (NotNull)
     * @param locale The locale of current request. (NotNull)
     * @param domainBundle The bundle for domain that does not set up extends handling yet. (NotNull)
     */
    protected void setupExtendsReferences(String domainMessageName, List<String> extendsNameList, Locale locale,
            MessageResourceBundle domainBundle) {
        final TreeSet<MessageResourceBundle> hierarchySet = new TreeSet<MessageResourceBundle>();
        final MessageResourceBundle wrappedDomainBundle = wrapBundle(domainMessageName, domainBundle, null);
        hierarchySet.addAll(convertToHierarchyList(wrappedDomainBundle));
        int extendsLevel = 1;
        for (String extendsName : extendsNameList) {
            final MessageResourceBundle extendsBundle = findBundleSimply(extendsName, locale);
            final MessageResourceBundle wrappedExtendsBundle = wrapBundle(extendsName, extendsBundle, extendsLevel);
            hierarchySet.addAll(convertToHierarchyList(wrappedExtendsBundle));
            ++extendsLevel;
        }
        for (MessageResourceBundle bundle : hierarchySet) {
            bundle.setParent(null); // initialize
        }
        MessageResourceBundle previousBundle = null;
        for (MessageResourceBundle bundle : hierarchySet) {
            if (previousBundle != null) {
                previousBundle.setParent(bundle);
            }
            previousBundle = bundle;
        }
    }

    /**
     * Wrap the bundle with detail info of message resource. <br>
     * The parents also wrapped.
     * @param messageName The message name for the bundle. (NotNull)
     * @param bundle The bundle of message resource. (NotNull)
     * @param extendsLevel The level as integer for extends. e.g. first extends is 1 (NullAllowed: when domain)
     * @return The wrapper for the bundle. (NotNull)
     */
    protected MessageResourceBundleWrapper wrapBundle(String messageName, MessageResourceBundle bundle,
            Integer extendsLevel) {
        final boolean existsDefaultLangProperties = existsDefaultLangProperties(messageName);
        final List<MessageResourceBundle> bundleList = new ArrayList<MessageResourceBundle>();
        bundleList.add(bundle);
        MessageResourceBundle currentBundle = bundle;
        int parentLevel = 1;
        while (true) {
            MessageResourceBundle parentBundle = currentBundle.getParent();
            if (parentBundle == null) {
                break;
            }
            final boolean defaultLang = isDefaultLangBundle(existsDefaultLangProperties, parentBundle);
            currentBundle.setParent(createBundleWrapper(parentBundle, defaultLang, parentLevel, extendsLevel));
            currentBundle = parentBundle;
            ++parentLevel;
        }
        return createBundleWrapper(bundle, isDefaultLangBundle(existsDefaultLangProperties, bundle), null, extendsLevel);
    }

    protected MessageResourceBundleWrapper createBundleWrapper(MessageResourceBundle bundle, boolean defaultLang,
            Integer parentLevel, Integer extendsLevel) {
        return new MessageResourceBundleWrapper(bundle, defaultLang, parentLevel, extendsLevel);
    }

    protected boolean existsDefaultLangProperties(String messageName) {
        final String path = messageName + PROPERTIES_EXT; // e.g. foo_message.properties
        return ResourceUtil.getResourceNoException(path) != null;
    }

    protected boolean isDefaultLangBundle(boolean existsDefaultLangProperties, MessageResourceBundle parentBundle) {
        // default language properties does not have parent (must be last of hierarchy element)
        return existsDefaultLangProperties && parentBundle.getParent() == null;
    }

    /**
     * Convert the bundle and its parents (hierarchy) to list.
     * <pre>
     * e.g. messageName = foo_message, locale = ja
     * parent3      : foo_message.properties *last search
     *  |-parent2   : foo_message_ja_JP_xx.properties
     *    |-parent1 : foo_message_ja_JP.properties
     *      |-root  : foo_message_ja.properties *first search
     *
     *  to
     *
     * list.get(0): foo_message_ja.properties (root)
     * list.get(1): foo_message_ja_JP.properties (parent1)
     * list.get(2): foo_message_ja_JP_xx.properties (parent2)
     * list.get(3): foo_message.properties (parent3)
     * </pre>
     * @param bundle The bundle of message resource. (NotNull)
     * @return The list of bundles. (NotNull)
     */
    protected List<MessageResourceBundle> convertToHierarchyList(MessageResourceBundle bundle) {
        final List<MessageResourceBundle> bundleList = new ArrayList<MessageResourceBundle>();
        bundleList.add(bundle);
        MessageResourceBundle currentBundle = bundle;
        while (true) {
            MessageResourceBundle parentBundle = currentBundle.getParent();
            if (parentBundle == null) {
                break;
            }
            bundleList.add(parentBundle);
            currentBundle = parentBundle;
        }
        return bundleList;
    }

    // ===================================================================================
    //                                                                      Label Handling
    //                                                                      ==============
    /**
     * Resolve label parameters in the arguments.
     * @param locale The locale of current request. (NullAllowed: when system default locale)
     * @param key The key of the message, basically for exception message. (NotNull)
     * @param args The array of arguments for message. (NullAllowed: if null, returns empty list)
     * @return The list of filtered parameters resolved label arguments. (NotNull, EmptyAllowed)
     */
    protected List<Object> resolveLabelParameter(Locale locale, String key, Object[] args) {
        final MessageResourceBundle bundle = getBundle(locale);
        if (args == null || args.length == 0) {
            return DfCollectionUtil.emptyList();
        }
        final List<Object> resolvedList = new ArrayList<Object>(args.length);
        for (Object arg : args) {
            if (canBeLabelKey(arg)) {
                final String labelKey = (String) arg;
                final String label = bundle.get(labelKey);
                if (label != null) {
                    resolvedList.add(label);
                    continue;
                } else {
                    throwMessageLabelByLabelParameterNotFoundException(locale, key, labelKey);
                }
            }
            resolvedList.add(arg);
        }
        return resolvedList;
    }

    protected boolean canBeLabelKey(Object arg) {
        return arg instanceof String && ((String) arg).startsWith(LABELS_KEY_PREFIX);
    }

    protected void throwMessageLabelByLabelParameterNotFoundException(Locale locale, String key, String labelKey) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the label by the label parameter.");
        br.addItem("Locale");
        br.addElement(locale);
        br.addItem("Message Key");
        br.addElement(key);
        br.addItem("Label Parameter");
        br.addElement(labelKey);
        final String msg = br.buildExceptionMessage();
        throw new MessageLabelByLabelParameterNotFoundException(msg);
    }

    /**
     * Resolve embedded label variables on the message.
     * <pre>
     * e.g. List of Member Purchase
     *  labels.memberPurchase = Member Purchase
     *  labels.list = List
     *  labels.memberPurchase.list = @[labels.list] of @[labels.memberPurchase]
     * </pre>
     * @param locale The locale of current request. (NullAllowed: when system default locale)
     * @param key The key of the message, basically for exception message. (NotNull)
     * @param message The plain message, might have label variables. (NullAllowed: if null, returns null)
     * @param callerKeySet The set of key that calls this to suppress infinity loop. (NotNull)
     * @return The resolved message. (NullAllowed: if no message, returns null)
     */
    protected String resolveLabelVariableMessage(Locale locale, String key, String message, Set<String> callerKeySet) {
        final String beginMark = LABEL_VARIABLE_BEGIN_MARK;
        final String endMark = LABEL_VARIABLE_END_MARK;
        if (message == null || !message.contains(beginMark) || !message.contains(endMark)) {
            return message;
        }
        final List<ScopeInfo> scopeList = DfStringUtil.extractScopeList(message, beginMark, endMark);
        if (scopeList.isEmpty()) {
            return message;
        }
        callerKeySet.add(key);
        final MessageResourceBundle bundle = getBundle(locale);
        String resolved = message;
        for (ScopeInfo scopeInfo : scopeList) {
            final String labelKey = scopeInfo.getContent();
            final String labelVar = scopeInfo.getScope();
            if (!canBeLabelKey(labelKey)) {
                throwMessageLabelByLabelVariableInvalidKeyException(locale, key, resolved, labelVar);
            }
            if (callerKeySet.contains(labelKey)) { // infinity loop
                throwMessageLabelByLabelVariableInfinityLoopException(locale, labelVar, callerKeySet);
            }
            String label = bundle.get(labelKey);
            if (label != null) {
                label = resolveLabelVariableMessage(locale, labelKey, label, callerKeySet);
                resolved = DfStringUtil.replace(resolved, labelVar, label);
            } else {
                throwMessageLabelByLabelVariableNotFoundException(locale, key, resolved, labelVar);
            }
        }
        callerKeySet.remove(key);
        return resolved;
    }

    protected void throwMessageLabelByLabelVariableInvalidKeyException(Locale locale, String key, String message,
            String labelVar) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The label key of the label variable was invalid.");
        br.addItem("Advice");
        br.addElement("Label key of label variable should start with 'labels.'");
        br.addElement("like this:");
        br.addElement("  (x): abc.foo");
        br.addElement("  (x): lable.bar");
        br.addElement("  (o): labels.foo");
        br.addItem("Locale");
        br.addElement(locale);
        br.addItem("Specified Key");
        br.addElement(key);
        br.addItem("Message");
        br.addElement(message);
        br.addItem("Label Variable");
        br.addElement(labelVar);
        final String msg = br.buildExceptionMessage();
        throw new MessageLabelByLabelVariableInvalidKeyException(msg);
    }

    protected void throwMessageLabelByLabelVariableInfinityLoopException(Locale locale, String labelVar,
            Set<String> callerKeySet) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the infinity loop in the message.");
        br.addItem("Locale");
        br.addElement(locale);
        br.addItem("Infinity Label");
        br.addElement(labelVar);
        br.addItem("Variable Tree");
        br.addElement(callerKeySet);
        final String msg = br.buildExceptionMessage();
        throw new MessageLabelByLabelVariableInfinityLoopException(msg);
    }

    protected void throwMessageLabelByLabelVariableNotFoundException(Locale locale, String key, String message,
            String labelVar) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the label by the label variable.");
        br.addItem("Locale");
        br.addElement(locale);
        br.addItem("Specified Key");
        br.addElement(key);
        br.addItem("Message");
        br.addElement(message);
        br.addItem("Label Variable");
        br.addElement(labelVar);
        final String msg = br.buildExceptionMessage();
        throw new MessageLabelByLabelVariableNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                           HotDeploy
    //                                                                           =========
    @Override
    public void dispose() {
        bundleCacheMap.clear();
        super.dispose();
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

    protected String getDomainMessageName() {
        if (cachedDomainMessageName != null) {
            return cachedDomainMessageName;
        }
        synchronized (this) {
            if (cachedDomainMessageName != null) {
                return cachedDomainMessageName;
            }
            final FwAssistantDirector assistantDirector = getAssistantDirector();
            final OptionalActionDirection direction = assistantDirector.assistOptionalActionDirection();
            cachedDomainMessageName = direction.assistDomainMessageName();
        }
        return cachedDomainMessageName;
    }

    protected List<String> getExtendsMessageNameList() {
        if (cachedExtendsMessageNameList != null) {
            return cachedExtendsMessageNameList;
        }
        synchronized (this) {
            if (cachedExtendsMessageNameList != null) {
                return cachedExtendsMessageNameList;
            }
            final FwAssistantDirector assistantDirector = getAssistantDirector();
            final OptionalActionDirection direction = assistantDirector.assistOptionalActionDirection();
            cachedExtendsMessageNameList = direction.assistExtendsMessageNameList();
        }
        return cachedExtendsMessageNameList;
    }

    protected Locale getDefaultLocale() {
        if (cachedDefaultLocale != null) {
            return cachedDefaultLocale;
        }
        synchronized (this) {
            if (cachedDefaultLocale != null) {
                return cachedDefaultLocale;
            }
            final FwAssistantDirector assistantDirector = getAssistantDirector();
            final OptionalServletDirection direction = assistantDirector.assistOptionalServletDirection();
            final UserLocaleProcessProvider provider = direction.assistUserLocaleProcessProvider();
            cachedDefaultLocale = provider.getFallbackLocale(); // you can provide
            if (cachedDefaultLocale == null) {
                cachedDefaultLocale = Locale.getDefault(); // same as Struts logic
            }
        }
        return cachedDefaultLocale;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected static <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap() {
        return DfCollectionUtil.newConcurrentHashMap();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String title = DfTypeUtil.toClassTitle(this);
        sb.append(title).append(":{");
        sb.append("domain=");
        if (cachedDomainMessageName != null) {
            sb.append(cachedDomainMessageName);
        } else {
            sb.append("not initialized yet");
        }
        final Set<String> cachedSet = bundleCacheMap.keySet();
        sb.append(", cached=[");
        if (!cachedSet.isEmpty()) {
            buildCacheDisplay(sb);
        } else {
            sb.append("no cached bundle");
        }
        sb.append("]}");
        return sb.toString();
    }

    protected void buildCacheDisplay(StringBuilder sb) {
        int messageIndex = 0;
        for (Entry<String, Map<Locale, MessageResourceBundle>> entry : bundleCacheMap.entrySet()) {
            final String key = entry.getKey();
            final Map<Locale, MessageResourceBundle> localeBundleMap = entry.getValue();
            if (messageIndex > 0) {
                sb.append(", ");
            }
            sb.append(key);
            sb.append("(");
            int localeIndex = 0;
            for (Locale locale : localeBundleMap.keySet()) {
                if (localeIndex > 0) {
                    sb.append(", ");
                }
                sb.append(locale);
                ++localeIndex;
            }
            sb.append(")");
            ++messageIndex;
        }
    }
}
