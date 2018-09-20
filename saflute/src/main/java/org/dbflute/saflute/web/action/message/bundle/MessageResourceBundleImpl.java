package org.dbflute.saflute.web.action.message.bundle;

import java.util.Properties;

/**
 * @author modified by jflute (originated in Seasar)
 */
public class MessageResourceBundleImpl implements MessageResourceBundle {

    private Properties prop;

    private MessageResourceBundle parent;

    public MessageResourceBundleImpl(Properties prop) {
        this.prop = prop;
    }

    public MessageResourceBundleImpl(Properties prop, MessageResourceBundle parent) {
        this(prop);
        setParent(parent);
    }

    public String get(String key) {
        if (key == null) {
            return null;
        }
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        }
        return (parent != null) ? parent.get(key) : null;
    }

    public MessageResourceBundle getParent() {
        return parent;
    }

    public void setParent(MessageResourceBundle parent) {
        this.parent = parent;
    }
}