package org.dbflute.saflute.web.action.message.bundle;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.seasar.framework.exception.ResourceNotFoundRuntimeException;
import org.seasar.framework.util.AssertionUtil;
import org.seasar.framework.util.Disposable;
import org.seasar.framework.util.DisposableUtil;
import org.seasar.framework.util.ResourceUtil;

/**
 * @author modified by jflute (originated in Seasar)
 */
public class MessageResourceBundleFactory {

    private static final String PROPERTIES_EXT = ".properties";

    private static final Object NOT_FOUND = new Object();

    @SuppressWarnings("rawtypes")
    private static Map cache = new HashMap();

    private static boolean initialized = false;

    public static MessageResourceBundle getBundle(String baseName) {
        return getBundle(baseName, Locale.getDefault());
    }

    public static MessageResourceBundle getBundle(String baseName, Locale locale) throws ResourceNotFoundRuntimeException {
        MessageResourceBundle bundle = getNullableBundle(baseName, locale);
        if (bundle != null) {
            return bundle;
        }
        throw new ResourceNotFoundRuntimeException(baseName);
    }

    public static MessageResourceBundle getNullableBundle(String baseName) {
        return getNullableBundle(baseName, Locale.getDefault());
    }

    public static MessageResourceBundle getNullableBundle(String baseName, Locale locale) {
        AssertionUtil.assertNotNull("baseName", baseName);
        AssertionUtil.assertNotNull("locale", locale);

        String base = baseName.replace('.', '/');

        String[] bundleNames = calcurateBundleNames(base, locale);
        MessageResourceBundleFacade parentFacade = null;
        MessageResourceBundleFacade facade = null;
        int length = bundleNames.length;
        for (int i = 0; i < length; ++i) {
            facade = loadFacade(bundleNames[i] + PROPERTIES_EXT);
            if (parentFacade == null) {
                parentFacade = facade;
            } else if (facade != null) {
                facade.setParent(parentFacade);
                parentFacade = facade;
            }
        }

        if (parentFacade != null) {
            return parentFacade.getBundle();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected static MessageResourceBundleFacade loadFacade(String path) {
        synchronized (cache) {
            if (!initialized) {
                DisposableUtil.add(new Disposable() {
                    public void dispose() {
                        clear();
                        initialized = false;
                    }
                });
                initialized = true;
            }
            Object cachedFacade = cache.get(path);
            if (cachedFacade == NOT_FOUND) {
                return null;
            } else if (cachedFacade != null) {
                return (MessageResourceBundleFacade) cachedFacade;
            }
            URL url = ResourceUtil.getResourceNoException(path);
            if (url != null) {
                MessageResourceBundleFacade facade = new MessageResourceBundleFacade(url);
                cache.put(path, facade);
                return facade;
            } else {
                cache.put(path, NOT_FOUND);
            }
        }
        return null;
    }

    protected static String[] calcurateBundleNames(String baseName, Locale locale) {
        int length = 1;
        boolean l = locale.getLanguage().length() > 0;
        if (l) {
            length++;
        }
        boolean c = locale.getCountry().length() > 0;
        if (c) {
            length++;
        }
        boolean v = locale.getVariant().length() > 0;
        if (v) {
            length++;
        }
        String[] result = new String[length];
        int index = 0;
        result[index++] = baseName;

        if (!(l || c || v)) {
            return result;
        }

        StringBuffer buffer = new StringBuffer(baseName);
        buffer.append('_');
        buffer.append(locale.getLanguage());
        if (l) {
            result[index++] = new String(buffer);
        }

        if (!(c || v)) {
            return result;
        }
        buffer.append('_');
        buffer.append(locale.getCountry());
        if (c) {
            result[index++] = new String(buffer);
        }

        if (!v) {
            return result;
        }
        buffer.append('_');
        buffer.append(locale.getVariant());
        result[index++] = new String(buffer);

        return result;
    }

    public static void clear() {
        cache.clear();
    }
}
