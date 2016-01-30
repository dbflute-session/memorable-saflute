package org.dbflute.saflute.web.action.message;

import java.util.Locale;

import org.apache.struts.util.MessageResources;
import org.dbflute.saflute.core.message.MessageResourceGateway;

/**
 * @author jflute
 */
public class StrutsMessageResourceGateway implements MessageResourceGateway {

    protected final MessageResources resources;

    public StrutsMessageResourceGateway(MessageResources resources) {
        this.resources = resources;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(Locale locale, String key) {
        return resources.getMessage(locale, key);
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage(Locale locale, String key, Object[] values) {
        return resources.getMessage(locale, key, values);
    }

    @Override
    public String toString() {
        return "{" + resources + "}";
    }
}
