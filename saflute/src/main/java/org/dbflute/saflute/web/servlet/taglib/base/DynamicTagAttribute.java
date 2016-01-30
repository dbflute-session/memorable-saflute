package org.dbflute.saflute.web.servlet.taglib.base;

/**
 * @author jflute
 */
public class DynamicTagAttribute {

    protected final String key;
    protected final Object value;

    public DynamicTagAttribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
