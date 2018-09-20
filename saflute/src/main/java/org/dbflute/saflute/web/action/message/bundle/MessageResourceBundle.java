package org.dbflute.saflute.web.action.message.bundle;

/**
 * @author modified by jflute (originated in Seasar)
 */
public interface MessageResourceBundle {

    String get(String key);

    MessageResourceBundle getParent();

    void setParent(MessageResourceBundle parent);
}
